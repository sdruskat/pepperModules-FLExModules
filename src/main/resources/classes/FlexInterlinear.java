package myPackage;

/**
* This interface is a dictionary for files following the model of 'FlexInterlinear'.
*
* @author XMLTagExtractor
**/
public interface FlexInterlinear{
		/** constant to address the xml-namespace prefix'xs'. **/
		public static final String NS_XS= "xs";
		/** constant to address the xml-namespace 'http://www.w3.org/2001/XMLSchema'. **/
		public static final String NS_VALUE_XS= "http://www.w3.org/2001/XMLSchema";

		/** constant to address the xml-element 'xs:choice'. **/
		public static final String TAG_XS_CHOICE= "choice";
		/** constant to address the xml-element 'xs:extension'. **/
		public static final String TAG_XS_EXTENSION= "extension";
		/** constant to address the xml-element 'xs:sequence'. **/
		public static final String TAG_XS_SEQUENCE= "sequence";
		/** constant to address the xml-element 'xs:enumeration'. **/
		public static final String TAG_XS_ENUMERATION= "enumeration";
		/** constant to address the xml-element 'xs:schema'. **/
		public static final String TAG_XS_SCHEMA= "schema";
		/** constant to address the xml-element 'xs:selector'. **/
		public static final String TAG_XS_SELECTOR= "selector";
		/** constant to address the xml-element 'xs:union'. **/
		public static final String TAG_XS_UNION= "union";
		/** constant to address the xml-element 'xs:restriction'. **/
		public static final String TAG_XS_RESTRICTION= "restriction";
		/** constant to address the xml-element 'xs:element'. **/
		public static final String TAG_XS_ELEMENT= "element";
		/** constant to address the xml-element 'xs:attribute'. **/
		public static final String TAG_XS_ATTRIBUTE= "attribute";
		/** constant to address the xml-element 'xs:complexType'. **/
		public static final String TAG_XS_COMPLEXTYPE= "complexType";
		/** constant to address the xml-element 'xs:key'. **/
		public static final String TAG_XS_KEY= "key";
		/** constant to address the xml-element 'xs:all'. **/
		public static final String TAG_XS_ALL= "all";
		/** constant to address the xml-element 'xs:simpleContent'. **/
		public static final String TAG_XS_SIMPLECONTENT= "simpleContent";
		/** constant to address the xml-element 'xs:field'. **/
		public static final String TAG_XS_FIELD= "field";
		/** constant to address the xml-element 'xs:simpleType'. **/
		public static final String TAG_XS_SIMPLETYPE= "simpleType";

		/** constant to address the xml-attribute 'minOccurs'. **/
		public static final String ATT_MINOCCURS= "minOccurs";
		/** constant to address the xml-attribute 'use'. **/
		public static final String ATT_USE= "use";
		/** constant to address the xml-attribute 'type'. **/
		public static final String ATT_TYPE= "type";
		/** constant to address the xml-attribute 'nillable'. **/
		public static final String ATT_NILLABLE= "nillable";
		/** constant to address the xml-attribute 'xmlns'. **/
		public static final String ATT_XMLNS= "xmlns";
		/** constant to address the xml-attribute 'ref'. **/
		public static final String ATT_REF= "ref";
		/** constant to address the xml-attribute 'xpath'. **/
		public static final String ATT_XPATH= "xpath";
		/** constant to address the xml-attribute 'name'. **/
		public static final String ATT_NAME= "name";
		/** constant to address the xml-attribute 'memberTypes'. **/
		public static final String ATT_MEMBERTYPES= "memberTypes";
		/** constant to address the xml-attribute 'maxOccurs'. **/
		public static final String ATT_MAXOCCURS= "maxOccurs";
		/** constant to address the xml-attribute 'fixed'. **/
		public static final String ATT_FIXED= "fixed";
		/** constant to address the xml-attribute 'id'. **/
		public static final String ATT_ID= "id";
		/** constant to address the xml-attribute 'value'. **/
		public static final String ATT_VALUE= "value";
		/** constant to address the xml-attribute 'base'. **/
		public static final String ATT_BASE= "base";
}
