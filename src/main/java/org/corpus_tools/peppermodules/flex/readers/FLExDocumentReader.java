package org.corpus_tools.peppermodules.flex.readers;

import org.corpus_tools.peppermodules.flex.exceptions.DocumentSAXParseFinishedEvent;
import org.corpus_tools.peppermodules.flex.model.FLExText;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.util.SaltUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * This class parses an xml file following the model of FLExText.
 *
 * @author XMLTagExtractor
 **/
public class FLExDocumentReader extends DefaultHandler2 implements FLExText {
	
	private static final Logger logger = LoggerFactory.getLogger(FLExDocumentReader.class);
	
	private final SDocumentGraph graph;
	private boolean isDocument = false;

	private SDocument doc;

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
			SMetaAnnotation guidAnnotation = graph.getDocument().getMetaAnnotation(FLExText.FLEX_NAMESPACE + SaltUtil.NAMESPACE_SEPERATOR + FLExText.FLEX__GUID_ATTR);
			if (guidAnnotation != null && attributes.getValue(FLExText.FLEX__GUID_ATTR).equals(guidAnnotation.getValue_STEXT())) {
				isDocument = true;
			}
		}
		if (isDocument) {
			if (TAG_PARAGRAPHS.equals(qName)) {
				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_PARAGRAPH.equals(qName)) {
				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_PHRASES.equals(qName)) {
				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_PHRASE.equals(qName)) {
				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_WORDS.equals(qName)) {
				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_WORD.equals(qName)) {
				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_MORPHEMES.equals(qName)) {
				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_MORPH.equals(qName)) {
				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_LANGUAGES.equals(qName)) {
				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_LANGUAGE.equals(qName)) {
				System.err.println("In doc " + doc.getName());
			}
			else if (TAG_ITEM.equals(qName)) {
				System.err.println("In doc " + doc.getName());
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (TAG_INTERLINEAR_TEXT.equals(qName)) {
			if (isDocument) {
				throw new DocumentSAXParseFinishedEvent(graph.getDocument().getName());
			}
		}
	}
	
	@Override
	public void fatalError(SAXParseException e) {
		logger.error("Caught a fatal error while parsing the XML file! Most likely, there will be content before the '<?xml>' element, such as text or a UTF-8 BOM. " + e.getLineNumber(), e);
	}
}
