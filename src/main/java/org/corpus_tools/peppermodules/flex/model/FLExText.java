package org.corpus_tools.peppermodules.flex.model;

/**
* This interface is a dictionary for files following the model of FLExText.
*
* @author XMLTagExtractor
**/
public interface FLExText {

		/** constant to address the xml-element 'paragraph'. **/
		public static final String TAG_PARAGRAPH= "paragraph";
		/** constant to address the xml-element 'item'. **/
		public static final String TAG_ITEM= "item";
		/** constant to address the xml-element 'languages'. **/
		public static final String TAG_LANGUAGES= "languages";
		/** constant to address the xml-element 'document'. **/
		public static final String TAG_DOCUMENT= "document";
		/** constant to address the xml-element 'words'. **/
		public static final String TAG_WORDS= "words";
		/** constant to address the xml-element 'language'. **/
		public static final String TAG_LANGUAGE= "language";
		/** constant to address the xml-element 'paragraphs'. **/
		public static final String TAG_PARAGRAPHS= "paragraphs";
		/** constant to address the xml-element 'morphemes'. **/
		public static final String TAG_MORPHEMES= "morphemes";
		/** constant to address the xml-element 'interlinear-text'. **/
		public static final String TAG_INTERLINEAR_TEXT= "interlinear-text";
		/** constant to address the xml-element 'phrase'. **/
		public static final String TAG_PHRASE= "phrase";
		/** constant to address the xml-element 'phrases'. **/
		public static final String TAG_PHRASES= "phrases";
		/** constant to address the xml-element 'morph'. **/
		public static final String TAG_MORPH= "morph";
		/** constant to address the xml-element 'word'. **/
		public static final String TAG_WORD= "word";

		/** constant to address the xml-attribute 'vernacular'. **/
		public static final String ATT_VERNACULAR= "vernacular";
		/** constant to address the xml-attribute 'guid'. **/
		public static final String ATT_GUID= "guid";
		/** constant to address the xml-attribute 'type'. **/
		public static final String ATT_TYPE= "type";
		/** constant to address the xml-attribute 'lang'. **/
		public static final String ATT_LANG= "lang";
		/** constant to address the xml-attribute 'version'. **/
		public static final String ATT_VERSION= "version";
		/** constant to address the xml-attribute 'font'. **/
		public static final String ATT_FONT= "font";
}
