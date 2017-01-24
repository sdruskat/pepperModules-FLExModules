/**
 * 
 */
package org.corpus_tools.peppermodules.flex.readers;

import java.util.Stack;
import java.util.Vector;

import org.corpus_tools.peppermodules.flex.model.FLExText;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * TODO Description
 *
 * @author Stephan Druskat <mail@sdruskat.net>
 *
 */
public class FLExCorpusStructureReader extends DefaultHandler2 implements FLExText {

	private static final Logger logger = LoggerFactory.getLogger(FLExDocumentReader.class);
	private SCorpus corpus;
	Stack<SDocument> documentStack = new Stack<>();
	boolean isTitle = false;
	private Vector<SDocument> documents = new Vector<>();

	/**
	 * @param subCorpus
	 */
	public FLExCorpusStructureReader(SCorpus subCorpus) {
		this.corpus = subCorpus;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (TAG_DOCUMENT.equals(qName)) {
		}
		else if (TAG_INTERLINEAR_TEXT.equals(qName)) {
			SDocument doc = corpus.getGraph().createDocument(corpus, "");
			documentStack.push(doc);
		}
		else if (TAG_PARAGRAPHS.equals(qName)) {
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
			if (attributes.getValue("type").equals("title")) {
				isTitle = true;
			}
		}
	}
	
	@Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isTitle) {
            documentStack.peek().setName(new String(ch, start, length));
        }
    }
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (TAG_INTERLINEAR_TEXT.equals(qName)) {
			documents.add(documentStack.pop());
		}
		else if (TAG_ITEM.equals(qName)) {
			isTitle = false;
		}
	}

	@Override
	public void fatalError(SAXParseException e) {
		logger.error("Caught a fatal error while parsing the XML file! Most likely, there will be content before the '<?xml>' element, such as text or a UTF-8 BOM. " + e.getLineNumber(), e);
	}

	/**
	 * @return the documents
	 */
	public final Vector<SDocument> getDocuments() {
		return documents;
	}

}
