/**
 * 
 */
package org.corpus_tools.peppermodules.flex;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.peppermodules.flex.exceptions.DocumentSAXParseFinishedEvent;
import org.corpus_tools.peppermodules.flex.readers.FLExDocumentReader;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * TODO Description
 *
 * @author Stephan Druskat <mail@sdruskat.net>
 *
 */
public class FLExMapper extends PepperMapperImpl {
	
	// FEFF because this is the Unicode char represented by the UTF-8 byte order mark (BOM, EF BB BF).
    public static final String UTF8_BOM = "\uFEFF";

	private static final Logger logger = LoggerFactory.getLogger(FLExMapper.class);
	
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		// Test if file validates against XSD schema
		File corpusFile = new File(getResourceURI().toFileString());
		URL xsd = getClass().getClassLoader().getResource("FlexInterlinear.xsd");
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
		Schema schema = null;
		try {
			schema = sf.newSchema(xsd);
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(corpusFile));
			logger.debug(corpusFile.getAbsolutePath() + " has validated successfully against '" + xsd.getFile() + "'.");
		}
		catch (SAXException | IOException e) {
			logger.warn(corpusFile.getAbsolutePath() + " has not validated successfully against '" + xsd.getFile() + "'! Ignoring file.", e);
			return DOCUMENT_STATUS.FAILED;
		} 
		
		getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
		SDocumentGraph graph = getDocument().getDocumentGraph();
		// Graph set up
		graph.createTimeline();
		graph.addLayer(getLayer("paragraphs"));
		graph.addLayer(getLayer("phrases"));
		graph.addLayer(getLayer("words"));
		graph.addLayer(getLayer("morphemes"));
		
		// Read document
		FLExDocumentReader reader = new FLExDocumentReader(getDocument());
		try {
			this.readXMLResource(reader, getResourceURI());
		}
		catch (Exception e) {
			if (e.getCause() instanceof DocumentSAXParseFinishedEvent) {
				logger.info(e.getCause().getMessage());
			}
			else {
				logger.error("An error occurred while reading the file '{}'.", getResourceURI().path(), e);
				return DOCUMENT_STATUS.FAILED;
			}
		}
		return DOCUMENT_STATUS.COMPLETED;
	}

	/**
	 * TODO: Description
	 *
	 * @param string
	 * @return
	 */
	private SLayer getLayer(String name) {
		SLayer layer = SaltFactory.createSLayer();
		layer.setName(name);
		return layer;
	}

}
