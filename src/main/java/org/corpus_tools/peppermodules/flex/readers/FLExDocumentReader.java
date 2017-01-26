package org.corpus_tools.peppermodules.flex.readers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.corpus_tools.peppermodules.flex.exceptions.DocumentSAXParseFinishedEvent;
import org.corpus_tools.peppermodules.flex.model.FLExText;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.util.SaltUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * This class parses an xml file following the model of 
 *
 * @author XMLTagExtractor
 **/
public class FLExDocumentReader extends DefaultHandler2 implements FLExText {
	
	private static final Logger logger = LoggerFactory.getLogger(FLExDocumentReader.class);
	
	private final SDocumentGraph graph;
	private boolean isDocument = false;

	private SDocument doc;
	
	private final List<SSpan> paragraphs = new ArrayList<>();

	private final List<SSpan> phrases = new ArrayList<>();

	private final List<SSpan> words = new ArrayList<>();

	private final Vector<SToken> morphemes = new Vector<>();

	private Map<String, String> activeItemAttributes = new HashMap<>();

	private String activeElement = null;

	private String activeElementValue = null;
	
	private Table<String, String, String> activeItems = HashBasedTable.create();

	private Map<String, String> activeAttributes = new HashMap<>();

	/**
	 * @param graph
	 * @param document 
	 */
	public FLExDocumentReader(SDocument document) {
		this.doc = document; 
		this.graph = document.getDocumentGraph();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (TAG_INTERLINEAR_TEXT.equals(qName)) {
			SMetaAnnotation guidAnnotation = graph.getDocument().getMetaAnnotation(TAG_INTERLINEAR_TEXT + SaltUtil.NAMESPACE_SEPERATOR + FLEX__GUID_ATTR);
			System.err.println(guidAnnotation);
			if (guidAnnotation != null && attributes.getValue(FLEX__GUID_ATTR).equals(guidAnnotation.getValue_STEXT())) {
				isDocument = true;
			}
		}
		if (isDocument) {
			if (TAG_PARAGRAPH.equals(qName)) {
				
			}
			else if (TAG_PHRASE.equals(qName)) {
//				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_WORD.equals(qName)) {
//				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_MORPH.equals(qName)) {
				activeElement = TAG_MORPH;
				for (int i = 0; i < attributes.getLength(); i++) {
					String name = attributes.getQName(i);
					String value = attributes.getValue(i);
					activeAttributes .put(name, value);
				}
//				STextualDS ds = graph.getTextualDSs().size() == 0 ? graph.createTextualDS("") : graph.getTextualDSs().get(0);
//				String tokenText = attributes.getValue(FLEX_ITEM_TYPE__TXT);
//				if (tokenText == null) {
//					// TODO Add location, file name, etc.
//					logger.warn("Encountered an empty morpheme: Ignoring it.");
//					return;
//				}
//				String oldText = ds.getText();
//				ds.setText(oldText += tokenText);
//				String text = ds.getText();
//				graph.createToken(ds, text.length() - 1 - tokenText.length(), text.length() - 1);
			}
			else if (TAG_ITEM.equals(qName)) {
				activeItems.put("", FLEX__LANG_ATTR, attributes.getValue(FLEX__LANG_ATTR));
				activeItems.put("", FLEX__TYPE_ATTR, attributes.getValue(FLEX__TYPE_ATTR));
				if (attributes.getType(FLEX__ANALYSIS_STATUS_ATTR) != null) {
					activeItems.put("", FLEX__ANALYSIS_STATUS_ATTR, attributes.getValue(FLEX__ANALYSIS_STATUS_ATTR));
				}
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		activeElementValue  = new String(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (isDocument) {
			if (TAG_PARAGRAPHS.equals(qName)) {
				// TODO MAP ALL PARAGRAPHS
				paragraphs.clear();
			}
			else if (TAG_PHRASES.equals(qName)) {
				// TODO MAP ALL PHRASES
				phrases.clear();
			}
			else if (TAG_PHRASE.equals(qName)) {
				reset();
			}
			else if (TAG_WORDS.equals(qName)) {
				// TODO MAP ALL WORDS
				words.clear();
			}
			else if (TAG_WORD.equals(qName)) {
				reset();
			}
			else if (TAG_MORPHEMES.equals(qName)) {
				// TODO MAP ALL MORPHEMES
				morphemes.clear();
			}
			else if (TAG_MORPH.equals(qName)) {
				Map<String, String> items = activeItems.row("");
				System.err.println("lang: " + items.get(FLEX__LANG_ATTR));
				System.err.println("type: " + items.get(FLEX__TYPE_ATTR));
				System.err.println("stat: " + items.get(FLEX__ANALYSIS_STATUS_ATTR));
				System.err.println("VALUE: " + items.get(PROCESSING__ACTIVE_ELEMENT_VALUE) + "------------------\n\n\n");
				reset();
			}
			else if (TAG_ITEM.equals(qName)) {
				activeItems.row("").put(PROCESSING__ACTIVE_ELEMENT_VALUE, activeElementValue);
			}
		}
		// Stop parsing if the end of the document is hit
		if (TAG_INTERLINEAR_TEXT.equals(qName)) {
			if (isDocument) {
				throw new DocumentSAXParseFinishedEvent(graph.getDocument().getName());
			}
		}
	}
	
	/**
	 * TODO: Description
	 *
	 */
	private void reset() {
		activeElementValue = null;
		activeAttributes.clear();
		activeItems.clear();
	}

	@Override
	public void fatalError(SAXParseException e) {
		logger.error("Caught a fatal error while parsing the XML file! Most likely, there will be content before the '<?xml>' element, such as text or a UTF-8 BOM. " + e.getLineNumber(), e);
	}
}
