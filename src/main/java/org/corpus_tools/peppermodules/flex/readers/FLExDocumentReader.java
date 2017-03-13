package org.corpus_tools.peppermodules.flex.readers;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.corpus_tools.peppermodules.flex.exceptions.DocumentSAXParseFinishedEvent;
import org.corpus_tools.peppermodules.flex.model.FLExText;
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
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.util.SaltUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * This class parses an xml file following the model of 
 *
 * @author XMLTagExtractor
 **/
public class FLExDocumentReader extends FLExReader implements FLExText {
	
	private enum Element {
		DOCUMENT, INTERLINEAR_TEXT, PARAGRAPHS, PARAGRAPH, PHRASES, PHRASE, WORDS, WORD, MORPHEMES, MORPH, ITEM
	}
	
	private static final Logger logger = LoggerFactory.getLogger(FLExDocumentReader.class);
	
	private final SDocument doc;
	private final SDocumentGraph graph;
	
	private boolean isCorrectDocument = false;
	private boolean isItemActiveElement = false;
	private Element itemParent = null;
	

	private Vector<SToken> morphemes = new Vector<>();
	private Vector<SToken> words = new Vector<>();
	private Vector<SSpan> phrases = new Vector<>();
	private SSpan paragraph = null;
	private int wordLength = 0;
	
	private int paragraphCount = 0;
	
	private final Table<String, String, String> morphItems = HashBasedTable.create();
	private final Table<String, String, String> wordItems = HashBasedTable.create();
	private final Table<String, String, String> phraseItems = HashBasedTable.create();
	
	private int wordTimelineStart;

	private boolean wordHasMorphemes;

	/**
	 * @param graph
	 * @param document 
	 */
	public FLExDocumentReader(SDocument document) {
		this.doc = document; 
		this.graph = document.getDocumentGraph();
	}

	/*
	 * FIXME Remove checks for activeElement when filling activeItems or rather,
	 * add other checks for elements when there can be items!
	 */
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (TAG_INTERLINEAR_TEXT.equals(qName)) {
			if (!isCorrectDocument) {
				SMetaAnnotation guidAnnotation = doc.getMetaAnnotation(TAG_INTERLINEAR_TEXT + SaltUtil.NAMESPACE_SEPERATOR + FLEX__GUID_ATTR);
				if (guidAnnotation != null && attributes.getValue(FLEX__GUID_ATTR).equals(guidAnnotation.getValue_STEXT())) {
					isCorrectDocument = true;
				}
			}
			else {
				throw new DocumentSAXParseFinishedEvent(doc.getName());
			}
		}
		if (isCorrectDocument) {
			if (TAG_PARAGRAPH.equals(qName)) {
				itemParent = Element.PARAGRAPH;
				phrases.clear();
				SSpan span = SaltFactory.createSSpan();
				for (int i = 0; i < attributes.getLength(); i++) {
					/* 
					 * Use Salt API here as annotation is expected to be unique 
					 * and FLExReader API needs a node ID 
					 * (which is null at this point).
					 */
					span.createAnnotation("paragraph", attributes.getQName(i), attributes.getValue(i));
				}
				paragraph = span;
			}
			else if (TAG_PHRASE.equals(qName)) {
				itemParent = Element.PHRASE;
				words.clear();
				phraseItems.clear();
				SSpan span = SaltFactory.createSSpan();
				for (int i = 0; i < attributes.getLength(); i++) {
					/* 
					 * Use Salt API here as annotation is expected to be unique 
					 * and FLExReader API needs a node ID 
					 * (which is null at this point).
					 */
					span.createAnnotation("phrase", attributes.getQName(i), attributes.getValue(i));
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
					 * Use Salt API here as annotation is expected to be unique 
					 * and FLExReader API needs a node ID 
					 * (which is null at this point).
					 */
					token.createAnnotation("word", attributes.getQName(i), attributes.getValue(i));
				}
				words.add(token);
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
					 * Use Salt API here as annotation is expected to be unique 
					 * and FLExReader API needs a node ID 
					 * (which is null at this point).
					 */
					token.createAnnotation("morpheme", attributes.getQName(i), attributes.getValue(i));
				}
				morphemes.add(token);
			}
			else if (TAG_ITEM.equals(qName)) {
				isItemActiveElement = true;
				int row;
				if (itemParent != null) {
					switch (itemParent) {
					case MORPH:
						row = morphItems.rowKeySet().size();
						morphItems.put(String.valueOf(row), FLEX__LANG_ATTR, attributes.getValue(FLEX__LANG_ATTR));
						morphItems.put(String.valueOf(row), FLEX__TYPE_ATTR, attributes.getValue(FLEX__TYPE_ATTR));
						if (attributes.getType(FLEX__ANALYSIS_STATUS_ATTR) != null) {
							morphItems.put(String.valueOf(row), FLEX__ANALYSIS_STATUS_ATTR, attributes.getValue(FLEX__ANALYSIS_STATUS_ATTR));
						}
						break;

					case WORD:
						row = wordItems.rowKeySet().size();
						wordItems.put(String.valueOf(row), FLEX__LANG_ATTR, attributes.getValue(FLEX__LANG_ATTR));
						wordItems.put(String.valueOf(row), FLEX__TYPE_ATTR, attributes.getValue(FLEX__TYPE_ATTR));
						if (attributes.getType(FLEX__ANALYSIS_STATUS_ATTR) != null) {
							wordItems.put(String.valueOf(row), FLEX__ANALYSIS_STATUS_ATTR, attributes.getValue(FLEX__ANALYSIS_STATUS_ATTR));
						}
						break;
						
					case PHRASE:
						row = phraseItems.rowKeySet().size();
						phraseItems.put(String.valueOf(row), FLEX__LANG_ATTR, attributes.getValue(FLEX__LANG_ATTR));
						phraseItems.put(String.valueOf(row), FLEX__TYPE_ATTR, attributes.getValue(FLEX__TYPE_ATTR));
						if (attributes.getType(FLEX__ANALYSIS_STATUS_ATTR) != null) {
							phraseItems.put(String.valueOf(row), FLEX__ANALYSIS_STATUS_ATTR, attributes.getValue(FLEX__ANALYSIS_STATUS_ATTR));
						}
						break;

					default:
						break;
					}
				}
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (isItemActiveElement) {
			if (itemParent != null) {
				switch (itemParent) {
				case MORPH:
					morphItems.put(String.valueOf(morphItems.rowKeySet().size() - 1), PROCESSING__ACTIVE_ELEMENT_VALUE, new String(ch, start, length));
					return;

				case WORD:
					wordItems.put(String.valueOf(wordItems.rowKeySet().size() - 1), PROCESSING__ACTIVE_ELEMENT_VALUE, new String(ch, start, length));
					return;

				case PHRASE:
					phraseItems.put(String.valueOf(phraseItems.rowKeySet().size() - 1), PROCESSING__ACTIVE_ELEMENT_VALUE, new String(ch, start, length));
					break;

				default:
					System.err.println("CHARS CANNOT BELONG TO ITEM");
					break;
				}
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (isCorrectDocument) {
			// Stop parsing if the end of the document is hit
			if (TAG_INTERLINEAR_TEXT.equals(qName)) {
				isCorrectDocument = false;
			}
			else if (TAG_PARAGRAPH.equals(qName)) {
				SSpan span = paragraph;
				graph.addNode(span);
				createAnnotation(span, "paragraph", "seqnum", Integer.toString(++paragraphCount));
				for (SSpan phrase : phrases) {
					for (SToken token : graph.getOverlappedTokens(phrase)) {
						SSpanningRelation spanRel = SaltFactory.createSSpanningRelation();
						spanRel.setSource(span);
						spanRel.setTarget(token);
						graph.addRelation(spanRel);
					}
				}
				graph.getLayerByName("paragraphs").get(0).addNode(span);
			}
			else if (TAG_PHRASE.equals(qName)) {
				SSpan span = phrases.lastElement();
				Iterator<Map<String, String>> rowIterator = phraseItems.rowMap().values().iterator();
				graph.addNode(span);
				while (rowIterator.hasNext()) {
					Map<String, String> row = rowIterator.next();
					createAnnotation(span, "phrase_" + row.get(FLEX__LANG_ATTR), row.get(FLEX__TYPE_ATTR), row.get(PROCESSING__ACTIVE_ELEMENT_VALUE));
				}
				
				for (SToken word : words) {
					SSpanningRelation spanRel = SaltFactory.createSSpanningRelation();
					spanRel.setSource(span);
					spanRel.setTarget(word);
					graph.addRelation(spanRel);
				}
				graph.getLayerByName("phrases").get(0).addNode(span);

			}
			else if (TAG_WORD.equals(qName)) {
				STimeline timeline = graph.getTimeline();
				
				STextualDS ds = graph.getTextualDSs().size() == 1 ? graph.createTextualDS("") : graph.getTextualDSs().get(1);
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
					createAnnotation(token, "word_" + row.get(FLEX__LANG_ATTR), row.get(FLEX__TYPE_ATTR), row.get(PROCESSING__ACTIVE_ELEMENT_VALUE));
				}
				String oldText = ds.getText();
				int oldTextLength = oldText.length();
				ds.setText(oldText += tokenText);
				
				STextualRelation textRel = SaltFactory.createSTextualRelation();
				textRel.setSource(token);
				textRel.setTarget(ds);
				textRel.setStart(oldTextLength);
				textRel.setEnd(ds.getText().length());
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
				graph.getLayerByName("words").get(0).addNode(token);
			}
			else if (TAG_MORPH.equals(qName)) {
				STimeline timeline = graph.getTimeline();
				int timelineEnd = timeline.getEnd() == null ? 0 : timeline.getEnd();

				STextualDS ds = graph.getTextualDSs().size() == 0 ? graph.createTextualDS("") : graph.getTextualDSs().get(0);
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
						createAnnotation(token, "morpheme_" + row.get(FLEX__LANG_ATTR), row.get(FLEX__TYPE_ATTR), row.get(PROCESSING__ACTIVE_ELEMENT_VALUE));
					}
				}
				// Empty element content *can* occur
				if (tokenText == null || tokenText.length() == 0) {
					/* 
					 * FIXME Once ANNISExporter has been fixed to allow the string
					 * "NULL" on import (i.e., escapes "NULL" in the database),
					 * change this to tokenText = "NULL"
					 */
					tokenText = "NULL";
				}
				String oldText = ds.getText();
				int oldTextLength = oldText.length();
				ds.setText(oldText += tokenText);

				STextualRelation textRel = SaltFactory.createSTextualRelation();
				textRel.setSource(token);
				textRel.setTarget(ds);
				textRel.setStart(oldTextLength);
				textRel.setEnd(ds.getText().length());
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
				graph.getLayerByName("morphemes").get(0).addNode(token);
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
		
	}
	


//	/**
//	 * TODO: Description
//	 *
//	 */
//	private void reset() {
//		activeElement = null;
//		activeElementValue = null;
//		activeAttributes.clear();
//		activeItems.clear();
//	}

	@Override
	public void fatalError(SAXParseException e) {
		logger.error("Caught a fatal error while parsing the XML file! Most likely, there will be content before the '<?xml>' element, such as text or a UTF-8 BOM. " + e.getLineNumber(), e);
	}
}
