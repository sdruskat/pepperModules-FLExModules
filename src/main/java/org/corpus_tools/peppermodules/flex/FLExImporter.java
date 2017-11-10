package org.corpus_tools.peppermodules.flex;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.peppermodules.flex.readers.FLExCorpusStructureReader;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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

			// Validate file against XSD
			URL xsd = getClass().getClassLoader().getResource("FlexInterlinear.xsd");
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
			Schema schema = null;
			try {
				schema = sf.newSchema(xsd);
				Validator validator = schema.newValidator();
				validator.validate(new StreamSource(corpusFile));
				logger.debug(corpusFile.getAbsolutePath() + " has validated successfully against '" + xsd.getFile() + "'.");
				// Every .flextext file is a *corpus*, not a *document*!
			}
			catch (SAXException | IOException e) {
				logger.warn(corpusFile.getAbsolutePath() + " has not validated successfully against '" + xsd.getFile() + "'! Ignoring file.", e);
				return;
			} 
			// Parse FLEXText file once to create documents
			FLExCorpusStructureReader reader = new FLExCorpusStructureReader(subCorpus);
			this.readXMLResource(reader, corpusFileURI);
			for (SDocument doc : reader.getDocuments()) {
				getIdentifier2ResourceTable().put(doc.getIdentifier(), corpusFileURI);
			}
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
