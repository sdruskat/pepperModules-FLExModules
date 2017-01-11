package org.corpus_tools.peppermodules.flex;

import java.io.File;

import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME: Similar to https://github.com/korpling/pepperModules-ModuleBox/blob/05b1b6f4f57292b7a297d72500e834ce01e0ef42/src/main/java/org/corpus_tools/peppermodules/toolboxModules/WolofImporter.java !!!

/**
 * TODO Description
 *
 * @author Stephan Druskat <mail@sdruskat.net>
 *
 */
@Component(name = "FLExImporterComponent", factory = "PepperImporterComponentFactory")
public class FLExImporter extends PepperImporterImpl implements PepperImporter{
	
	private static final Logger logger = LoggerFactory.getLogger(FLExImporter.class);

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
	public void importCorpusStructure(SCorpusGraph corpusGraph) throws PepperModuleException {
		this.setCorpusGraph(corpusGraph);
		URI fileURI = getCorpusDesc().getCorpusPath();
		File corpusFile = new File(fileURI.toFileString());
		importCorpusStructure(corpusGraph, null, corpusFile);
	}
	
	private void importCorpusStructure(SCorpusGraph corpusGraph, SCorpus parent, File corpusFile) {
		URI corpusFileURI = URI.createFileURI(corpusFile != null ? corpusFile.getAbsolutePath() : null);
		String corpusFileName = corpusFile.getName();
		if (corpusFile.isDirectory()) {
			SCorpus subCorpus = corpusGraph.createCorpus(parent, corpusFileName);
			getIdentifier2ResourceTable().put(subCorpus.getIdentifier(), corpusFileURI);
			if (corpusFile != null) {
				for (File child : corpusFile.listFiles()) {
					importCorpusStructure(corpusGraph, subCorpus, child);
				}
			}
		}
		else if (corpusFile.isFile()) {
			// Create a corpus for the file
			SCorpus subCorpus = corpusGraph.createCorpus(parent, corpusFileName.substring(0, corpusFileName.lastIndexOf('.')));
			getIdentifier2ResourceTable().put(subCorpus.getIdentifier(), corpusFileURI);

			// Read file
			// Create document in file
			SDocument doc = corpusGraph.createDocument(subCorpus, corpusFileName.substring(0, corpusFileName.lastIndexOf('.')));
			getIdentifier2ResourceTable().put(doc.getIdentifier(), corpusFileURI);
		}
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
