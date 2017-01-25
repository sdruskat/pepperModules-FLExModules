package org.corpus_tools.peppermodules.flex.readers;

import org.corpus_tools.peppermodules.flex.FLExImporter;
import org.corpus_tools.peppermodules.flex.exceptions.DocumentSAXParseFinishedEvent;
import org.corpus_tools.peppermodules.flex.model.FLExText;
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

	/**
	 * @param graph
	 */
	public FLExDocumentReader(SDocumentGraph graph) {
		this.graph = graph;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (TAG_DOCUMENT.equals(qName)) {
		}
		else if (TAG_INTERLINEAR_TEXT.equals(qName)) {
			SMetaAnnotation guidAnnotation = graph.getDocument().getMetaAnnotation(FLExImporter.FLEX_NAMESPACE + SaltUtil.NAMESPACE_SEPERATOR + FLExImporter.FLEX_DOCUMENT_GUID);
			if (guidAnnotation != null && attributes.getValue(FLExImporter.FLEX_DOCUMENT_GUID).equals(guidAnnotation.getValue_STEXT())) {
				isDocument = true;
			}
		}
		if (isDocument) {
			if (TAG_PARAGRAPHS.equals(qName)) {
			}
			else if (TAG_PARAGRAPH.equals(qName)) {
			}
			else if (TAG_PHRASES.equals(qName)) {
			}
			else if (TAG_PHRASE.equals(qName)) {
			}
			else if (TAG_WORDS.equals(qName)) {
			}
			else if (TAG_WORD.equals(qName)) {
			}
			else if (TAG_MORPHEMES.equals(qName)) {
			}
			else if (TAG_MORPH.equals(qName)) {
			}
			else if (TAG_LANGUAGES.equals(qName)) {
			}
			else if (TAG_LANGUAGE.equals(qName)) {
			}
			else if (TAG_ITEM.equals(qName)) {
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
