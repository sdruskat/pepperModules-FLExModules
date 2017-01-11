package org.corpus_tools.peppermodules.flex;

import org.corpus_tools.peppermodules.flex.model.FLExText;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * This class parses an xml file following the model of FLExText.
 *
 * @author XMLTagExtractor
 **/
public class FLExTextReader extends DefaultHandler2 implements FLExText {
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (TAG_PARAGRAPH.equals(qName)) {
		}
		else if (TAG_ITEM.equals(qName)) {
		}
		else if (TAG_LANGUAGES.equals(qName)) {
		}
		else if (TAG_DOCUMENT.equals(qName)) {
		}
		else if (TAG_WORDS.equals(qName)) {
		}
		else if (TAG_LANGUAGE.equals(qName)) {
		}
		else if (TAG_PARAGRAPHS.equals(qName)) {
		}
		else if (TAG_MORPHEMES.equals(qName)) {
		}
		else if (TAG_INTERLINEAR_TEXT.equals(qName)) {
		}
		else if (TAG_PHRASE.equals(qName)) {
		}
		else if (TAG_PHRASES.equals(qName)) {
		}
		else if (TAG_MORPH.equals(qName)) {
		}
		else if (TAG_WORD.equals(qName)) {
		}
	}
}
