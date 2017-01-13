package org.corpus_tools.peppermodules.flex.model;

import org.corpus_tools.peppermodules.flex.FLExImporter;

/**
 * This interface is a dictionary for files following the model of FLExText.
 * 
 * The FLExText model has the following structure, according to [FlexInterlinear.xsd](/resources/FlexInterlinear.xsd):
 * (! = required, ? = optional, () = fixed)
 * 
 * - `document`
 * 	- `interlinear-text` 1..*
 * 		- `item` 0..1
 * 		- `paragraphs` 1..1
 * 			- `paragraph` 0..*
 * 				- `phrases` 1..1
 * 					- `phrase` 0..*
 * 						- `item` 0..*
 * 						- `words` 1..1
 * 							- `scrMilestone` 0..*
 *							- `word` 0..*
 * 								- `item` 0..*
 * 								- `morphemes` 0..1
 * 									- `morph` 0..*
 * 										- `item` 0..1
 * 						- `item` 0..* (Order seems to be important here!)
 * 		- `languages` 0..1
 * 			- `language` 0..*
 * 		- `media-files` 0..*
 * 			- `media` 0..*
 * - `item` (non-nillable)
 *
 * The resulting Salt model will look like the following
 * 
 * ```
 * +---------------------------------------------------------------------------------------------+
 * | SCorpus document                                                                            |
 * +---------------------------------------------------------------------------------------+-----+
 * | SDocument interlinear-text                                                            |     |
 * |     annotations:                                                                      | ... |
 * |         item "type"_"lang":value                                                      |     |
 * +---------------------------------------------------------------------------------+-----+-----+
 * | SSpan phrase "item 'segnum'"                                                    |     |
 * |     annotations:                                                                | ... |    
 * |         item "type"_"lang":value                                                |     |
 * +---------------------------------------------------------------------------+-----+-----+
 * | SSpan word                                                                |     |
 * |     annotations:                                                          | ... |
 * |         item "type"_"lang":value                                          |     |
 * +----------------------------------+----------------------------------+-----+-----+
 * | SToken morph                     | SToken morph                     |     |
 * |     annotations:                 |     annotations:                 | ... |
 * |         item "type"_"lang":value |         item "type"_"lang":value |     |
 * +----------------------------------+----------------------------------+-----+-----------------+
 * | STextualDS "word" (compiled from word > item type="text")                                   |
 * +---------------------------------------------------------------------------------------------+
 * | STextualDS "morph" (compiled from morph > item type="text")                                  |
 * +---------------------------------------------------------------------------------------------+
 * ```
 *
 * @author XMLTagExtractor
 **/
public interface FLExText {

		/** 
		 * Constant to address the xml-element `paragraph`.
		 * 
		 * `paragraph`s are .
		 */
		public static final String TAG_PARAGRAPH= "paragraph";
		/** 
		 * Constant to address the xml-element `item`.
		 * 
		 * This corresponds to the generic element `item`,
		 * so items carry their domain info in their `type`
		 * attribute.
		 */
		public static final String TAG_ITEM= "item";
		/** 
		 * Constant to address the xml-element `languages`.
		 * 
		 * `languages` is a container for `language`s.
		 */
		public static final String TAG_LANGUAGES= "languages";
		/** 
		 * Constant to address the xml-element `document`.
		 * 
		 * This corresponds to the Salt element `SCorpus`.
		 * As all FLExText files start with this, the corpus-document
		 * containment relationships depend on the number of FLExText
		 * *Text*s exported at any one time.
		 * 
		 * Additionally, the {@link FLExImporter} also works on
		 * directories, in case of which all files contained in
		 * a directory become a sub-corpus.
		 */
		public static final String TAG_DOCUMENT= "document";
		/** 
		 * Constant to address the xml-element `words`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String TAG_WORDS= "words";
		/** 
		 * Constant to address the xml-element `language`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String TAG_LANGUAGE= "language";
		/** 
		 * Constant to address the xml-element `paragraphs`.
		 * 
		 * `paragraphs` is a container for `paragraph`s.
		 */
		public static final String TAG_PARAGRAPHS= "paragraphs";
		/** 
		 * Constant to address the xml-element `morphemes`.
		 * 
		 * `morphemes` is a container for `morph`s.
		 */
		public static final String TAG_MORPHEMES= "morphemes";
		/** 
		 * Constant to address the xml-element `interlinear-text`.
		 * 
		 * This corresponds to the Salt element `SDocument`.
		 * 
		 * `interlinear-text` can, to current knowledge, have
		 * the following child elements:
		 * 
		 * - `paragraphs`
		 * - `languages`
		 * - `item`
		 */
		public static final String TAG_INTERLINEAR_TEXT= "interlinear-text";
		/** 
		 * Constant to address the xml-element `phrase`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String TAG_PHRASE= "phrase";
		/** 
		 * Constant to address the xml-element `phrases`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String TAG_PHRASES= "phrases";
		/** 
		 * Constant to address the xml-element `morph`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String TAG_MORPH= "morph";
		/** 
		 * Constant to address the xml-element `word`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String TAG_WORD= "word";

		/** 
		 * Constant to address the xml-attribute `vernacular`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String ATT_VERNACULAR= "vernacular";
		/** Constant to address the xml-attribute `guid`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String ATT_GUID= "guid";
		/** Constant to address the xml-attribute `type`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String ATT_TYPE= "type";
		/** Constant to address the xml-attribute `lang`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String ATT_LANG= "lang";
		/** Constant to address the xml-attribute `version`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String ATT_VERSION= "version";
		/** Constant to address the xml-attribute `font`.
		 * 
		 * This corresponds to the Salt element ``.
		 */
		public static final String ATT_FONT= "font";
}
