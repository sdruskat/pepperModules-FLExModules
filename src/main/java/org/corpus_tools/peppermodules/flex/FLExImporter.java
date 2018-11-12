package org.corpus_tools.peppermodules.flex;

import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

// FIXME: Similar to https://github.com/korpling/pepperModules-ModuleBox/blob/05b1b6f4f57292b7a297d72500e834ce01e0ef42/src/main/java/org/corpus_tools/peppermodules/toolboxModules/WolofImporter.java !!!

/**
 * TODO Description
 *
 * @author Stephan Druskat <mail@sdruskat.net>
 *
 */
@Component(name = "FLExImporterComponent", factory = "PepperImporterComponentFactory")
public class FLExImporter extends PepperImporterImpl implements PepperImporter{
	
	/**
	 * TODO
	 */
	public static final String LAYER_INTERLINEAR_TEXT = "interlinear-text";
	/**
	 * 
	 */
	public static final String LAYER_PHRASE = "phrase";
	/**
	 * 
	 */
	public static final String LAYER_WORD = "word";
	/**
	 * 
	 */
	public static final String LAYER_MORPH = "morphs";

	/**
	 * // TODO Add description
	 * 
	 */
	public FLExImporter() {
		super();
		setName("FLExImporter");
		setSupplierContact(URI.createURI("stephan.druskat@hu-berlin.de"));
		setSupplierHomepage(URI.createURI("http://corpus-tools.org"));
		setDesc("An importer for the XML format written by SIL FLEx (FieldWorks Language Explorer), FLExText.");
		addSupportedFormat("xml", "1.0", null);
		getDocumentEndings().add("flextext");
	}
	
	@Override
	public PepperMapper createPepperMapper(Identifier Identifier) {
		FLExMapper mapper = new FLExMapper();
		mapper.setResourceURI(getIdentifier2ResourceTable().get(Identifier));
		return (mapper);
	}

	@Override
	public Double isImportable(URI corpusPath) {
		// TODO some code to analyze the given corpus-structure
		return (null);
	}

}
