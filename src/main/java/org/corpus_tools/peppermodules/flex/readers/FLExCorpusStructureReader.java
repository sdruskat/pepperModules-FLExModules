/**
 * 
 */
package org.corpus_tools.peppermodules.flex.readers;

import java.util.HashMap;
import java.util.Map;
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
	private final Stack<SDocument> documentStack = new Stack<>();
	private final Vector<SDocument> documents = new Vector<>();
	private String parent = null;
	private String activeElementValue = null;
	private String docName = null;
	private boolean docNameSet = false;
	private Map<String, String> activeItemAttributes = new HashMap<>();

	/**
	 * @param subCorpus
	 */
	public FLExCorpusStructureReader(SCorpus subCorpus) {
		this.corpus = subCorpus;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		/*
		 * Deals with any attributes directly in the element,
		 * children, i.e., items, are handled in endElement.
		 */
		if (TAG_DOCUMENT.equals(qName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				String name = attributes.getQName(i);
				String value = attributes.getValue(i);
				corpus.createMetaAnnotation(FLEX_NAMESPACE, name, value);
			}
		}
		else if (TAG_INTERLINEAR_TEXT.equals(qName)) {
			parent = TAG_INTERLINEAR_TEXT;
			SDocument doc = corpus.getGraph().createDocument(corpus, "");
			for (int i = 0; i < attributes.getLength(); i++) {
				String name = attributes.getQName(i);
				String value = attributes.getValue(i);
				doc.createMetaAnnotation(FLEX_NAMESPACE, name, value);
			}
			documentStack.push(doc);
		}
		else if (TAG_PARAGRAPHS.equals(qName)) {
			/*
			 * This is the first child after items, so get
			 * the parent out of the equation...
			 */
			parent = null;
		}
		else if (TAG_ITEM.equals(qName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				String name = attributes.getQName(i);
				String value = attributes.getValue(i);
				activeItemAttributes.put(name, value);
			}
		}
	}
	
	@Override
    public void characters(char[] ch, int start, int length) throws SAXException {
		activeElementValue = new String(ch, start, length);
    }
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (TAG_INTERLINEAR_TEXT.equals(qName)) {
			if (!docNameSet && docName != null) {
				documentStack.peek().setName(docName);
				docName = null;
			}
			documents.add(documentStack.pop());
		}
		else if (TAG_ITEM.equals(qName)) {
			SDocument doc = documentStack.peek();
			if (parent != null) {
				// Note: Handle only child `item`s of TAG_INTERLINEAR_TEXT
				if (parent.equals(TAG_INTERLINEAR_TEXT)) {
					// FIXME: Bug #2
					if (activeItemAttributes.get(FLEX__TYPE_ATTR).equals(FLEX_ITEM_TYPE__TITLE)) {
						docName = activeElementValue;
						if (activeItemAttributes.get(FLEX__LANG_ATTR).equals("en")) {
							doc.setName(activeElementValue);
							docNameSet = true;
						}
					}
					doc.createMetaAnnotation(activeItemAttributes.get(FLEX__LANG_ATTR), activeItemAttributes.get(FLEX__TYPE_ATTR), activeElementValue);
				}
			}
			// Clean up after you
			activeItemAttributes.clear();
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
