package org.corpus_tools.peppermodules.flex.readers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.corpus_tools.peppermodules.flex.exceptions.DocumentSAXParseFinishedEvent;
import org.corpus_tools.peppermodules.flex.model.FLExText;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
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
	
	private Table<String, String, String> items = HashBasedTable.create();

	private Map<String, String> activeAttributes = new HashMap<>();

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
			if (TAG_PHRASE.equals(qName)) {
				itemParent = Element.PHRASE;
				items.clear();
			}
			if (TAG_WORD.equals(qName)) {
				itemParent = Element.WORD;
				items.clear();
			}
			if (TAG_MORPH.equals(qName)) {
				itemParent = Element.MORPH;
				items.clear();
				SToken token = SaltFactory.createSToken();
				for (int i = 0; i < attributes.getLength(); i++) {
					if (attributes.getQName(i).equals(FLEX__TYPE_ATTR)) {
						token.createAnnotation(FLEX_NAMESPACE, attributes.getQName(i), attributes.getValue(i));
					}
					else {
						token.createMetaAnnotation(FLEX_NAMESPACE, attributes.getQName(i), attributes.getValue(i));
					}
				}
				graph.addNode(token);
				morphemes.add(token);
			}
			else if (TAG_ITEM.equals(qName)) {
				isItemActiveElement = true;
				int row = items.rowKeySet().size();
				items.put(String.valueOf(row), FLEX__LANG_ATTR, attributes.getValue(FLEX__LANG_ATTR));
				items.put(String.valueOf(row), FLEX__TYPE_ATTR, attributes.getValue(FLEX__TYPE_ATTR));
				if (attributes.getType(FLEX__ANALYSIS_STATUS_ATTR) != null) {
					items.put(String.valueOf(row), FLEX__ANALYSIS_STATUS_ATTR, attributes.getValue(FLEX__ANALYSIS_STATUS_ATTR));
				}
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
//		System.err.println("CHAR CALLED");
//		if (activeElement != null && activeElement.equals(TAG_MORPH) && isItem ) {
//			String row = Integer.toString(activeItems.rowKeySet().size() - 1);
//			activeItems.put(row, PROCESSING__ACTIVE_ELEMENT_VALUE, new String(ch, start, length));
//		}
		if (isItemActiveElement) {
			if (itemParent != null) {
				switch (itemParent) {
				case MORPH:
					items.put(String.valueOf(items.rowKeySet().size() - 1), PROCESSING__ACTIVE_ELEMENT_VALUE, new String(ch, start, length));
					return;

				case WORD:

					break;

				case PHRASE:

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
				System.err.println("X");
				isCorrectDocument = false;
			}
//			else if (TAG_PHRASE.equals(qName)) {
//				items.clear();
//			}
//			else if (TAG_WORD.equals(qName)) {
//				items.clear();
//			}
			else if (TAG_MORPH.equals(qName)) {
				STextualDS ds = graph.getTextualDSs().size() == 0 ? graph.createTextualDS("") : graph.getTextualDSs().get(0);
				Iterator<Map<String, String>> rowIterator = items.rowMap().values().iterator();
				String tokenText = null;
				txtLoop:
				while (rowIterator.hasNext()) {
					Map<String, String> row = rowIterator.next();
					if (row.get(FLEX__TYPE_ATTR).equals(FLEX_ITEM_TYPE__TXT)) {
						tokenText = row.get(PROCESSING__ACTIVE_ELEMENT_VALUE);
						break txtLoop;
					}
				}
				String oldText = ds.getText();
				int oldTextLength = oldText.length();
				ds.setText(oldText += tokenText);

				STextualRelation textRel = SaltFactory.createSTextualRelation();
				textRel.setSource(morphemes.lastElement());
				textRel.setTarget(ds);
				textRel.setStart(oldTextLength);
				textRel.setEnd(ds.getText().length());
				
				graph.addRelation(textRel);
//				String text = ds.getText();
				
				
				
				
				
//				System.err.println("ITEMS FOR THIS MORPH: ");
//				for (int i = 0; i < items.rowKeySet().size(); i++) {
//					Map<String, String> row = items.row(String.valueOf(i));
//					System.err.println("lang: " + row.get(FLEX__LANG_ATTR) + ", type: " + row.get(FLEX__TYPE_ATTR) + ", stat: " + row.get(FLEX__ANALYSIS_STATUS_ATTR));
//				}
//				System.err.println("---\n\n");
				
				
//				items.clear();
//				System.err.println("END MORPH");
//				String tokenText = null;
////				for (int i = 0; i < activeItems.rowKeySet().size(); i++) {
////					Map<String, String> items = activeItems.row(Integer.toString(i));
////					System.err.println("lang: " + items.get(FLEX__LANG_ATTR));
////					System.err.println("type: " + items.get(FLEX__TYPE_ATTR));
////					System.err.println("stat: " + items.get(FLEX__ANALYSIS_STATUS_ATTR));
////					System.err.println("VALUE: " + items.get(PROCESSING__ACTIVE_ELEMENT_VALUE) + "\n");
////				}
////				System.err.println("------------------\n\n\n");
//				STextualDS ds = graph.getTextualDSs().size() == 0 ? graph.createTextualDS("") : graph.getTextualDSs().get(0);
//				Iterator<Map<String, String>> rowIterator = activeItems.rowMap().values().iterator();
//				txtLoop:
//				while (rowIterator.hasNext()) {
//					Map<String, String> row = rowIterator.next();
//					System.err.println(row);
//					if (row.get(FLEX__TYPE_ATTR).equals(FLEX_ITEM_TYPE__TXT)) {
//						tokenText = row.get(PROCESSING__ACTIVE_ELEMENT_VALUE);
//						break txtLoop;
//					}
//				}
//				System.err.println(">>> " + tokenText);
//				if (tokenText == null) {
//					// TODO Add location, file name, etc.
//					logger.warn("Encountered an empty morpheme: Ignoring it.");
//					return;
//				}
//				String oldText = ds.getText();
//				ds.setText(oldText += tokenText);
//				String text = ds.getText();
//				graph.createToken(ds, text.length() - tokenText.length(), text.length());
//
//				reset();
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
