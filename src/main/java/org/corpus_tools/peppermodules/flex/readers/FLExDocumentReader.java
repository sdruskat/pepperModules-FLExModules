/*******************************************************************************
 * Copyright (c) 2016, 2018ff. Stephan Druskat
 * Exploitation rights for this version belong exclusively to Humboldt-Universität zu Berlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Stephan Druskat - initial API and implementation
 *******************************************************************************/
package org.corpus_tools.peppermodules.flex.readers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Triple;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.peppermodules.flex.model.FLExText;
import org.corpus_tools.peppermodules.flex.properties.FLExImporterProperties;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * This class parses an xml file following the model of {@link FLExText}.
 * 
 * In the process, the FLEx XML values are mapped onto a *Salt* model
 * following these principles:
 * 
 * - `interlinear-text` elements are mapped to {@link SDocument}s.
 * - `paragraph`s, are mapped to {@link SSpan}s, which span over
 * - `phrase`s, which are mapped to {@link SSpan}s, which span over
 * - `word`s and `morph`s, both of which are mapped to {@link SToken}s,
 * which are tied to an {@link STextualDS} each.
 * - Annotations (`item`s) for each layer are tied to the
 * respective {@link SNode} ({@link SToken} or {@link SSpan})
 * - XML attributes are mapped as {@link SAnnotation} to the
 * respective {@link SNode}.
 *
 * @author Stephan Druskat <mail@sdruskat.net>
 **/
public class FLExDocumentReader extends FLExReader implements FLExText {

	private enum Element {
		DOCUMENT, INTERLINEAR_TEXT, LANGUAGE, PARAGRAPHS, PARAGRAPH, PHRASES, PHRASE, WORDS, WORD, MORPHEMES, MORPH, ITEM
	}

	private static final Logger logger = LoggerFactory.getLogger(FLExDocumentReader.class);

	private final SDocumentGraph graph;

	private final STextualDS morphDS;
	private final STextualDS wordDS;

	private boolean isItemActiveElement = false;
	private Element itemParent = null;

	private Vector<SToken> morphemes = new Vector<>();
	private Vector<SToken> words = new Vector<>();
	private Vector<SSpan> phrases = new Vector<>();
	private List<SAnnotation> languages = new ArrayList<>();
	private SSpan paragraph = null;
	private int wordLength = 0;

	private int paragraphCount = 0;

	private final Table<String, String, String> interlinearTextItems = HashBasedTable.create();
	private final Table<String, String, String> morphItems = HashBasedTable.create();
	private final Table<String, String, String> wordItems = HashBasedTable.create();
	private final Table<String, String, String> phraseItems = HashBasedTable.create();

	private int wordTimelineStart;

	private boolean wordHasMorphemes;

	private List<Triple<String, String, String>> annotationsToDrop;
	private Map<Triple<String, String, String>, String> annotationMap;

	private FLExImporterProperties fLExProperties;


	/**
	 * @param document The document to be read into
	 * @param pepperModuleProperties The properties to be applied to the conversion process
	 */
	public FLExDocumentReader(SDocument document, PepperModuleProperties pepperModuleProperties) {
		super(pepperModuleProperties);
		this.graph = document.getDocumentGraph();
		morphDS = graph.createTextualDS("");
		morphDS.setName("morphological-data");
		wordDS = graph.createTextualDS("");
		wordDS.setName("lexical-data");
		if (properties instanceof FLExImporterProperties) {
			fLExProperties = (FLExImporterProperties) properties;
			annotationsToDrop = fLExProperties.getAnnotationsToDrop();
			annotationMap = fLExProperties.getAnnotationMap();
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (TAG_INTERLINEAR_TEXT.equals(qName)) {
			itemParent = Element.INTERLINEAR_TEXT;
			interlinearTextItems.clear();
			languages.clear();
			for (int i = 0; i < attributes.getLength(); i++) {
				/*
				 * Use Salt API here as annotation is expected to be unique and
				 * FLExReader API needs a node ID (which is null at this point).
				 */
				String name = attributes.getQName(i);
				if (fLExProperties.getAnnotationMap() != null && mapAnnotation(TAG_INTERLINEAR_TEXT, name)) {
					name = getNewAnnotationName(TAG_INTERLINEAR_TEXT, name);
				}
				graph.getDocument().createAnnotation(TAG_INTERLINEAR_TEXT, name, attributes.getValue(i));
			}
		}
		else if (TAG_LANGUAGE.equals(qName)) {
			if (annotationsToDrop != null && annotationsToDrop.contains(Triple.of(null, null, "languages"))) {
				return;
			}
			itemParent = Element.LANGUAGE;
			SAnnotation annotation = SaltFactory.createSAnnotation();
			annotation.setNamespace(TAG_LANGUAGES);
			annotation.setName(attributes.getValue(FLEX__LANG_ATTR));
			annotation.setValue(FLEX_LANGUAGE__ENCODING_ATTR + "=" + attributes.getValue(FLEX_LANGUAGE__ENCODING_ATTR) + "," 
					+ FLEX_LANGUAGE__VERNACULAR_ATTR + "=" + attributes.getValue(FLEX_LANGUAGE__VERNACULAR_ATTR) + ","
					+ FLEX_LANGUAGE__FONT_ATTR + "=" + attributes.getValue(FLEX_LANGUAGE__FONT_ATTR));
			languages.add(annotation);
		}
		else if (TAG_PARAGRAPH.equals(qName)) {
			itemParent = Element.PARAGRAPH;
			phrases.clear();
			SSpan span = SaltFactory.createSSpan();
			for (int i = 0; i < attributes.getLength(); i++) {
				/*
				 * Use Salt API here as annotation is expected to be unique and
				 * FLExReader API needs a node ID (which is null at this point).
				 */
				String name = attributes.getQName(i);
				if (fLExProperties.getAnnotationMap() != null && mapAnnotation(TAG_PARAGRAPH, name)) {
					name = getNewAnnotationName(TAG_PARAGRAPH, name);
				}
				span.createAnnotation(TAG_PARAGRAPH, name, attributes.getValue(i));
			}
			paragraph = span;
		}
		else if (TAG_PHRASE.equals(qName)) {
			itemParent = Element.PHRASE;
			words.clear();
			morphemes.clear();
			phraseItems.clear();
			SSpan span = SaltFactory.createSSpan();
			for (int i = 0; i < attributes.getLength(); i++) {
				/*
				 * Use Salt API here as annotation is expected to be unique and
				 * FLExReader API needs a node ID (which is null at this point).
				 */
				if (!dropAnnotation("phrase", attributes.getQName(i))) {
					String name = attributes.getQName(i);
					if (fLExProperties.getAnnotationMap() != null && mapAnnotation(TAG_PHRASE, name)) {
						name = getNewAnnotationName(TAG_PHRASE, name);
					}
					span.createAnnotation(TAG_PHRASE, name, attributes.getValue(i));
				}
			}
			phrases.add(span);
		}
		else if (TAG_WORD.equals(qName)) {
			itemParent = Element.WORD;
			wordLength = 0;
			wordItems.clear();
			wordHasMorphemes = false;
			Integer timelineEnd = graph.getTimeline().getEnd();
			wordTimelineStart = timelineEnd == null ? 0 : timelineEnd;
			SToken token = SaltFactory.createSToken();
			for (int i = 0; i < attributes.getLength(); i++) {
				/*
				 * Use Salt API here as annotation is expected to be unique and
				 * FLExReader API needs a node ID (which is null at this point).
				 */
				String name = attributes.getQName(i);
				if (fLExProperties.getAnnotationMap() != null && mapAnnotation(TAG_WORD, name)) {
					name = getNewAnnotationName(TAG_WORD, name);
				}
				token.createAnnotation(TAG_WORD, name, attributes.getValue(i));
			}
			words.add(token);
			graph.getLayerByName(TOKEN_LAYER_LEXICAL).get(0).addNode(token);
		}
		else if (TAG_MORPHEMES.equals(qName)) {
			wordHasMorphemes = true;
		}
		else if (TAG_MORPH.equals(qName)) {
			itemParent = Element.MORPH;
			morphItems.clear();
			SToken token = SaltFactory.createSToken();
			for (int i = 0; i < attributes.getLength(); i++) {
				/*
				 * Use Salt API here as annotation is expected to be unique and
				 * FLExReader API needs a node ID (which is null at this point).
				 */
				String name = attributes.getQName(i);
				if (fLExProperties.getAnnotationMap() != null && mapAnnotation(TAG_MORPH, name)) {
					name = getNewAnnotationName(TAG_MORPH, name);
				}
				token.createAnnotation(TAG_MORPH, name, attributes.getValue(i));
			}
			morphemes.add(token);
			graph.getLayerByName(TOKEN_LAYER_MORPHOLOGICAL).get(0).addNode(token);
		}
		else if (TAG_ITEM.equals(qName)) {
			isItemActiveElement = true;
			int row;
			if (itemParent != null) {
				switch (itemParent) {
				case INTERLINEAR_TEXT:
					row = interlinearTextItems.rowKeySet().size();
					interlinearTextItems.put(String.valueOf(row), FLEX__LANG_ATTR, attributes.getValue(FLEX__LANG_ATTR));
					interlinearTextItems.put(String.valueOf(row), FLEX__TYPE_ATTR, attributes.getValue(FLEX__TYPE_ATTR));
					if (attributes.getType(FLEX__ANALYSIS_STATUS_ATTR) != null) {
						interlinearTextItems.put(String.valueOf(row), FLEX__ANALYSIS_STATUS_ATTR,
								attributes.getValue(FLEX__ANALYSIS_STATUS_ATTR));
					}
					break;

				case MORPH:
					row = morphItems.rowKeySet().size();
					morphItems.put(String.valueOf(row), FLEX__LANG_ATTR, attributes.getValue(FLEX__LANG_ATTR));
					morphItems.put(String.valueOf(row), FLEX__TYPE_ATTR, attributes.getValue(FLEX__TYPE_ATTR));
					if (attributes.getType(FLEX__ANALYSIS_STATUS_ATTR) != null) {
						morphItems.put(String.valueOf(row), FLEX__ANALYSIS_STATUS_ATTR,
								attributes.getValue(FLEX__ANALYSIS_STATUS_ATTR));
					}
					break;

				case WORD:
					row = wordItems.rowKeySet().size();
					wordItems.put(String.valueOf(row), FLEX__LANG_ATTR, attributes.getValue(FLEX__LANG_ATTR));
					wordItems.put(String.valueOf(row), FLEX__TYPE_ATTR, attributes.getValue(FLEX__TYPE_ATTR));
					if (attributes.getType(FLEX__ANALYSIS_STATUS_ATTR) != null) {
						wordItems.put(String.valueOf(row), FLEX__ANALYSIS_STATUS_ATTR,
								attributes.getValue(FLEX__ANALYSIS_STATUS_ATTR));
					}
					break;

				case PHRASE:
					row = phraseItems.rowKeySet().size();
					phraseItems.put(String.valueOf(row), FLEX__LANG_ATTR, attributes.getValue(FLEX__LANG_ATTR));
					phraseItems.put(String.valueOf(row), FLEX__TYPE_ATTR, attributes.getValue(FLEX__TYPE_ATTR));
					if (attributes.getType(FLEX__ANALYSIS_STATUS_ATTR) != null) {
						phraseItems.put(String.valueOf(row), FLEX__ANALYSIS_STATUS_ATTR,
								attributes.getValue(FLEX__ANALYSIS_STATUS_ATTR));
					}
					break;

				default:
					break;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (isItemActiveElement) {
			if (itemParent != null) {
				switch (itemParent) {
				case INTERLINEAR_TEXT:
					interlinearTextItems.put(String.valueOf(interlinearTextItems.rowKeySet().size() - 1), PROCESSING__ACTIVE_ELEMENT_VALUE, new String(ch, start, length));
					return;

				case MORPH:
					morphItems.put(String.valueOf(morphItems.rowKeySet().size() - 1), PROCESSING__ACTIVE_ELEMENT_VALUE,
							new String(ch, start, length));
					return;

				case WORD:
					wordItems.put(String.valueOf(wordItems.rowKeySet().size() - 1), PROCESSING__ACTIVE_ELEMENT_VALUE,
							new String(ch, start, length));
					return;

				case PHRASE:
					phraseItems.put(String.valueOf(phraseItems.rowKeySet().size() - 1),
							PROCESSING__ACTIVE_ELEMENT_VALUE, new String(ch, start, length));
					break;

				default:
					logger.error("CHARS CANNOT BELONG TO ITEM");
					break;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (TAG_INTERLINEAR_TEXT.equals(qName)) {
			// Add all FLEx 'languages' annotations
			for (SAnnotation language : languages) {
				graph.getDocument().addAnnotation(language);
			}
			Iterator<Map<String, String>> rowIterator = interlinearTextItems.rowMap().values().iterator();
			while (rowIterator.hasNext()) {
				Map<String, String> row = rowIterator.next();
				String name = row.get(FLEX__TYPE_ATTR);
				String language = row.get(FLEX__LANG_ATTR);
				if (fLExProperties.getAnnotationMap() != null && mapAnnotation(TAG_INTERLINEAR_TEXT, language, name)) {
					name = getNewAnnotationName(TAG_INTERLINEAR_TEXT, language, name);
				}
				createLanguagedAnnotation(graph.getDocument(), language, name,
						row.get(PROCESSING__ACTIVE_ELEMENT_VALUE));
			}
		}
		else if (TAG_PARAGRAPH.equals(qName)) {
			SSpan span = paragraph;
			graph.addNode(span);
			createAnnotation(span, TAG_PARAGRAPH, TAG_SEQNUM, Integer.toString(++paragraphCount));
			for (SSpan phrase : phrases) {
				for (SToken token : graph.getOverlappedTokens(phrase)) {
					SSpanningRelation spanRel = SaltFactory.createSSpanningRelation();
					spanRel.setSource(span);
					spanRel.setTarget(token);
					graph.addRelation(spanRel);
				}
			}
		}
		else if (TAG_PHRASE.equals(qName)) {
			SSpan span = phrases.lastElement();
			Iterator<Map<String, String>> rowIterator = phraseItems.rowMap().values().iterator();
			graph.addNode(span);
			while (rowIterator.hasNext()) {
				Map<String, String> row = rowIterator.next();
				if (!dropAnnotation("phrase", row.get(FLEX__LANG_ATTR), row.get(FLEX__TYPE_ATTR))) {
					String name = row.get(FLEX__TYPE_ATTR);
					String language = row.get(FLEX__LANG_ATTR);
					if (fLExProperties.getAnnotationMap() != null && mapAnnotation(TAG_PHRASE, language, name)) {
						name = getNewAnnotationName(TAG_PHRASE, language, name);
					}
					createLanguagedAnnotation(span, language, name,
							row.get(PROCESSING__ACTIVE_ELEMENT_VALUE));
				}
				graph.getLayerByName(ITEM_LAYER_PHRASE).get(0).addNode(span);
			}

			for (SToken word : words) {
				SSpanningRelation spanRel = SaltFactory.createSSpanningRelation();
				spanRel.setSource(span);
				spanRel.setTarget(word);
				graph.addRelation(spanRel);
			}
			for (SToken morpheme : morphemes) {
				SSpanningRelation spanRel = SaltFactory.createSSpanningRelation();
				spanRel.setSource(span);
				spanRel.setTarget(morpheme);
				graph.addRelation(spanRel);
			}

		}
		else if (TAG_WORD.equals(qName)) {
			STimeline timeline = graph.getTimeline();

			Iterator<Map<String, String>> rowIterator = wordItems.rowMap().values().iterator();
			SToken token = words.lastElement();
			graph.addNode(token);
			String tokenText = null;
			String type = null;
			while (rowIterator.hasNext()) {
				Map<String, String> row = rowIterator.next();
				type = row.get(FLEX__TYPE_ATTR);
				if (type.equals(FLEX_ITEM_TYPE__TXT) || type.equals(FLEX_ITEM_TYPE__PUNCT)) {
					tokenText = row.get(PROCESSING__ACTIVE_ELEMENT_VALUE);
				}
				if (!dropAnnotation("word", row.get(FLEX__LANG_ATTR), row.get(FLEX__TYPE_ATTR))) {
					String name = row.get(FLEX__TYPE_ATTR);
					String language = row.get(FLEX__LANG_ATTR);
					if (fLExProperties.getAnnotationMap() != null && mapAnnotation(TAG_WORD, language, name)) {
						name = getNewAnnotationName(TAG_WORD, language, name);
					}
					createLanguagedAnnotation(token, language, name,
							row.get(PROCESSING__ACTIVE_ELEMENT_VALUE));
				}
				graph.getLayerByName(ITEM_LAYER_WORD).get(0).addNode(token);
			}
			String oldText = wordDS.getText();
			int oldTextLength = oldText.length();
			wordDS.setText(oldText += wordDS.getText().length() == 0 ? tokenText : " " + tokenText);

			STextualRelation textRel = SaltFactory.createSTextualRelation();
			textRel.setSource(token);
			textRel.setTarget(wordDS);
			boolean dSHasOneToken = wordDS.getText().length() == tokenText.length();
			textRel.setStart(dSHasOneToken ? oldTextLength : oldTextLength + 1);
			textRel.setEnd(wordDS.getText().length());
			graph.addRelation(textRel);

			// Word does not contain morphemes, e.g. in case of punctuation
			if (!wordHasMorphemes) {
				timeline.increasePointOfTime(tokenText.length());
				wordLength = tokenText.length();
			}
			STimelineRelation timeLineRel = SaltFactory.createSTimelineRelation();
			timeLineRel.setSource(token);
			timeLineRel.setTarget(timeline);
			timeLineRel.setStart(wordTimelineStart);
			timeLineRel.setEnd(wordTimelineStart + wordLength);
			graph.addRelation(timeLineRel);
		}
		else if (TAG_MORPH.equals(qName)) {
			STimeline timeline = graph.getTimeline();
			int timelineEnd = timeline.getEnd() == null ? 0 : timeline.getEnd();

			Iterator<Map<String, String>> rowIterator = morphItems.rowMap().values().iterator();
			SToken token = morphemes.lastElement();
			graph.addNode(token);
			String tokenText = null;
			while (rowIterator.hasNext()) {
				Map<String, String> row = rowIterator.next();
				if (row.get(FLEX__TYPE_ATTR).equals(FLEX_ITEM_TYPE__TXT)) {
					tokenText = row.get(PROCESSING__ACTIVE_ELEMENT_VALUE);
				}
				else {
					if (!dropAnnotation("morph", row.get(FLEX__LANG_ATTR), row.get(FLEX__TYPE_ATTR))) {
						String name = row.get(FLEX__TYPE_ATTR);
						String language = row.get(FLEX__LANG_ATTR);
						if (fLExProperties.getAnnotationMap() != null && mapAnnotation(TAG_MORPH, language, name)) {
							name = getNewAnnotationName(TAG_MORPH, language, name);
						}
						createLanguagedAnnotation(token, language, name,
								row.get(PROCESSING__ACTIVE_ELEMENT_VALUE));
					}
					graph.getLayerByName(ITEM_LAYER_MORPH).get(0).addNode(token);
				}
			}
			// Empty element content *can* occur
			if (tokenText == null || tokenText.length() == 0) {
				tokenText = "\u004eULL"; // Unicode 'N' + ULL
			}
			String oldText = morphDS.getText();
			int oldTextLength = oldText.length();
			morphDS.setText(oldText += tokenText);

			STextualRelation textRel = SaltFactory.createSTextualRelation();
			textRel.setSource(token);
			textRel.setTarget(morphDS);
			textRel.setStart(oldTextLength);
			textRel.setEnd(morphDS.getText().length());
			graph.addRelation(textRel);

			int timeSteps = tokenText.length();
			wordLength += timeSteps;
			timeline.increasePointOfTime(timeSteps);
			STimelineRelation timeLineRel = SaltFactory.createSTimelineRelation();
			timeLineRel.setSource(token);
			timeLineRel.setTarget(timeline);
			timeLineRel.setStart(timelineEnd);
			timeLineRel.setEnd(timelineEnd += timeSteps);
			graph.addRelation(timeLineRel);
		}
		else if (TAG_ITEM.equals(qName)) {
			isItemActiveElement = false;
		}
		else if (TAG_MORPHEMES.equals(qName)) {
			itemParent = Element.WORD;
		}
		else if (TAG_WORDS.equals(qName)) {
			itemParent = Element.PHRASE;
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	@Override
	public void fatalError(SAXParseException e) {
		logger.error(
				"Caught a fatal error while parsing the XML file! Most likely, there will be content before the '<?xml>' element, such as text or a UTF-8 BOM. "
						+ e.getLineNumber(),
				e);
	}

	private boolean dropAnnotation(String layer, String name) {
		return dropAnnotation(layer, null, name);
	}

	private boolean dropAnnotation(String layer, String language, String name) {
		return actionAnnotations(layer, language, name, annotationsToDrop);
	}

	private boolean mapAnnotation(String layer, String name) {
		return mapAnnotation(layer, null, name);
	}

	private boolean mapAnnotation(String layer, String language, String name) {
		return actionAnnotations(layer, language, name, fLExProperties.getAnnotationMap().keySet());
	}

	private boolean actionAnnotations(String layer, String language, String name,
			Collection<Triple<String, String, String>> triples) {
		for (Triple<String, String, String> triple : triples) {
			if (triple.equals(Triple.of(layer, language, name))
					|| triple.equals(Triple.of(null, language, name))
					|| triple.equals(Triple.of(layer, null, name))
					|| triple.equals(Triple.of(null, null, name))
					) {
				return true;
			}
		}
		return false;
	}

	private String getNewAnnotationName(String layer, String name) {
		return getNewAnnotationName(layer, null, name);
	}

	private String getNewAnnotationName(String layer, String language, String name) {
		for (Entry<Triple<String, String, String>, String> entry : annotationMap.entrySet()) {
			Triple<String, String, String> triple = entry.getKey();
			if (triple.equals(Triple.of(layer, language, name))
					|| triple.equals(Triple.of(null, language, name))
					|| triple.equals(Triple.of(layer, null, name))
					|| triple.equals(Triple.of(null, null, name))
					) {
				return entry.getValue();
			}
		}
		return name;
	}
}
