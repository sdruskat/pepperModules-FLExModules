package myPackage;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
* This class parses an xml file following the model of 'FlexInterlinearReader'.
*
* @author XMLTagExtractor
**/
public class FlexInterlinearReader extends DefaultHandler2 implements FlexInterlinear {
		@Override
		public void startElement(	String uri,
				String localName,
				String qName,
				Attributes attributes)throws SAXException
		{
			if (TAG_XS_CHOICE.equals(qName)){
			}
			else if (TAG_XS_EXTENSION.equals(qName)){
			}
			else if (TAG_XS_SEQUENCE.equals(qName)){
			}
			else if (TAG_XS_ENUMERATION.equals(qName)){
			}
			else if (TAG_XS_SCHEMA.equals(qName)){
			}
			else if (TAG_XS_SELECTOR.equals(qName)){
			}
			else if (TAG_XS_UNION.equals(qName)){
			}
			else if (TAG_XS_RESTRICTION.equals(qName)){
			}
			else if (TAG_XS_ELEMENT.equals(qName)){
			}
			else if (TAG_XS_ATTRIBUTE.equals(qName)){
			}
			else if (TAG_XS_COMPLEXTYPE.equals(qName)){
			}
			else if (TAG_XS_KEY.equals(qName)){
			}
			else if (TAG_XS_ALL.equals(qName)){
			}
			else if (TAG_XS_SIMPLECONTENT.equals(qName)){
			}
			else if (TAG_XS_FIELD.equals(qName)){
			}
			else if (TAG_XS_SIMPLETYPE.equals(qName)){
			}
		}
}
