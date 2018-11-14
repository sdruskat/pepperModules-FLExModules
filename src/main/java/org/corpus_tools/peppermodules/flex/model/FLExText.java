package org.corpus_tools.peppermodules.flex.model;

import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SAnnotation;

/**
 * This interface is a dictionary for files following the model of FLExText.
 * 
 * The FLExText model has the following structure, according to
 * [FlexInterlinear.xsd](/resources/FlexInterlinear.xsd): (! = required, ? =
 * optional, () = fixed)
 * 
 * - `document` - `interlinear-text` 1..* - `item` 0..1 - `paragraphs` 1..1 -
 * `paragraph` 0..* - `phrases` 1..1 - `phrase` 0..* - `item` 0..* - `words`
 * 1..1 - `scrMilestone` 0..* - `word` 0..* - `item` 0..* - `morphemes` 0..1 -
 * `morph` 0..* - `item` 0..* - `item` 0..* (Order seems to be important here!)
 * - `languages` 0..1 - `language` 0..* - `media-files` 0..* - `media` 0..* -
 * `item` (non-nillable)
 *
 * The resulting Salt model will look like the following
 * 
 * ```
 * +---------------------------------------------------------------------------------------------+
 * | SCorpus document |
 * +---------------------------------------------------------------------------------------+-----+
 * | SDocument interlinear-text | | | annotations: | ... | | item
 * "type"_"lang":value | |
 * +---------------------------------------------------------------------------------+-----+-----+
 * | SSpan phrase "item 'segnum'" | | | annotations: | ... | | item
 * "type"_"lang":value | |
 * +---------------------------------------------------------------------------+-----+-----+
 * | SToken word | | | annotations: | ... | | item "type"_"lang":value | |
 * +-↓-↓-↓---------------------------------------------------------------------+-↓-↓-+-----------+
 * | STimeline timeline | | [Ties together "word" tokens (above) and "morph"
 * tokens (below) | | to interlinearize on the real data source] |
 * +-↑-↑-↑------------------------------↑-↑-↑----------------------------+-↑-↑-+-----------------+
 * | SToken morph | SToken morph | | | annotations: | annotations: | ... | |
 * item "type"_"lang":value | item "type"_"lang":value | |
 * +----------------------------------+----------------------------------+-----+-----------------+
 * | STextualDS "word" (compiled from word > item type="text") |
 * +---------------------------------------------------------------------------------------------+
 * | STextualDS "morph" (compiled from morph > item type="text") |
 * +---------------------------------------------------------------------------------------------+
 * ```
 *
 * ## Salt mapping
 * 
 * 'item' elements, i.e., linguistically relevant annotations, are mapped onto
 * Salt {@link SAnnotation}s. These have a `namespace`, a `name` and a `value`.
 * 
 * During the mapping, an 'item's 'lang' is mapped onto the {@link SAnnotation}
 * `namespace`, and its 'type' is mapped onto the {@link SAnnotation} `name`.
 * 
 * The *level* of the annotation in the FLEx XML structure is represented by
 * assigning the annotation's container (i.e., the respective node) an **item
 * layer**. This can be used to retrieve the level of the annotation during
 * downstream manipulation and export.
 * 
 * ### Item layers
 * 
 * Item layers represent the levels of the FLEx XML which can have 'item'
 * elements, i.e., linguistically relevant annotations. They are (bottom to
 * top):
 * 
 * - `morph` - `word` - `phrase` - `interlinear-text`
 * 
 * 'item' elements on the `interlinear-text` level cannot be mapped to
 * annotations whose container (an {@link SDocument}) cannot be added to its
 * internal layer. Therefore, 'item's on the `interlinear-text` level are
 * annotations on the {@link SDocument}. This information can be used to
 * retrieve the level of the annotation during downstream manipulation and
 * export.
 * 
 * ### Token layers
 * 
 * As the import of FLEx XML creates two token layers in Salt (and also two
 * different data source nodes) - namely for lexical and for morphological
 * tokens - these are being added to a dedicated layer respectively. These are
 * called `lexial-data` and `morphological-data`.
 *
 * @author Stephan Druskat (<mail@sdruskat.net>)
 **/
public interface FLExText {

	/**
	 * TODO
	 */
	public static final String ITEM_LAYER_MORPH = "morph";
	/**
	 * TODO
	 */
	public static final String ITEM_LAYER_WORD = "word";
	/**
	 * TODO
	 */
	public static final String ITEM_LAYER_PHRASE = "phrase";
	/**
	 * 
	 */
	public static final String TOKEN_LAYER_LEXICAL = "lexical-data";
	/**
	 * 
	 */
	public static final String TOKEN_LAYER_MORPHOLOGICAL = "morphological-data";

	/**
	 * Constant to address the xml-element `paragraph`.
	 * 
	 * `paragraph`s are TODO.
	 */
	public static final String TAG_PARAGRAPH = "paragraph";
	/**
	 * Constant to address the xml-element `item`.
	 * 
	 * This corresponds to the generic element `item`, so items carry their
	 * domain info in their `type` attribute.
	 */
	public static final String TAG_ITEM = "item";
	/**
	 * Constant to address the xml-element `languages`.
	 * 
	 * `languages` is a container for `language`s.
	 */
	public static final String TAG_LANGUAGES = "languages";

	/**
	 * TODO
	 */
	public static final String TAG_SEQNUM = "seqnum";
	/**
	 * Constant to address the xml-element `words`.
	 * 
	 * This corresponds to the Salt element ``.
	 */
	public static final String TAG_WORDS = "words";
	/**
	 * Constant to address the xml-element `language`.
	 * 
	 * This corresponds to the Salt element ``.
	 */
	public static final String TAG_LANGUAGE = "language";

	/**
	 * Constant to address the xml-element `morphemes`.
	 * 
	 * `morphemes` is a container for `morph`s.
	 */
	public static final String TAG_MORPHEMES = "morphemes";
	/**
	 * Constant to address the xml-element `interlinear-text`.
	 * 
	 * This corresponds to the Salt element `SDocument`.
	 * 
	 * `interlinear-text` can, to current knowledge, have the following child
	 * elements:
	 * 
	 * - `paragraphs` - `languages` - `item`
	 */
	public static final String TAG_INTERLINEAR_TEXT = "interlinear-text";
	/**
	 * Constant to address the xml-element `phrase`.
	 * 
	 * This corresponds to the Salt element ``.
	 */
	public static final String TAG_PHRASE = "phrase";

	/**
	 * Constant to address the xml-element `morph`.
	 * 
	 * This corresponds to the Salt element ``.
	 */
	public static final String TAG_MORPH = "morph";
	/**
	 * Constant to address the xml-element `word`.
	 * 
	 * This corresponds to the Salt element ``.
	 */
	public static final String TAG_WORD = "word";

	public static final String FLEX__TYPE_ATTR = "type";
	public static final String FLEX__LANG_ATTR = "lang";
	public static final String FLEX__ANALYSIS_STATUS_ATTR = "analysisStatus";

	public static final String FLEX_LANGUAGE__ENCODING_ATTR = "encoding";
	public static final String FLEX_LANGUAGE__FONT_ATTR = "font";
	public static final String FLEX_LANGUAGE__VERNACULAR_ATTR = "vernacular";

	// Item types
	public static final String FLEX_ITEM_TYPE__TXT = "txt";

	public static final String FLEX_ITEM_TYPE__PUNCT = "punct";

	// Processing constants
	public static final String PROCESSING__KEY_VALUE_SEPARATOR = "=";

	public static final String PROCESSING__ACTIVE_ELEMENT_VALUE = "activeElementValue";
	public static final String PROCESSING__UNDERSCORE = "_";
}
