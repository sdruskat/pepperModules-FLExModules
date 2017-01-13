/**
 * 
 */
package org.corpus_tools.peppermodules.flex;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleXMLResourceException;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

/**
 * TODO Description
 *
 * @author Stephan Druskat <mail@sdruskat.net>
 *
 */
public class FLExMapper extends PepperMapperImpl {

	private static final Logger logger = LoggerFactory.getLogger(FLExMapper.class);
	
	@Override
	public DOCUMENT_STATUS mapSCorpus() {
		getCorpus().setName(getResourceURI().lastSegment());
		return (DOCUMENT_STATUS.COMPLETED);
	}

	@Override
	public DOCUMENT_STATUS mapSDocument() {
		getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
		URI resource = getResourceURI();
		logger.debug("Importing the file {}.", resource);

		SDocumentGraph graph = getDocument().getDocumentGraph();
		FLExTextReader reader = null;
		if (graph != null) {
			reader = new FLExTextReader(graph);
			
		}
		else {
			logger.error("SDocumentGraph for " + resource + " is null!");
		}
		this.readXMLResource(reader, getResourceURI());

		return (DOCUMENT_STATUS.COMPLETED);
	}

	@Override
	public void readXMLResource(DefaultHandler2 contentHandler, URI documentLocation) {
		if (documentLocation == null) {
			throw new PepperModuleXMLResourceException("Cannot load a xml-resource, because the given uri to locate file is null.");
		}

		File resourceFile = new File(documentLocation.toFileString());
		if (!resourceFile.exists()) {
			throw new PepperModuleXMLResourceException("Cannot load a xml-resource, because the file does not exist: " + resourceFile);
		}

		if (!resourceFile.canRead()) {
			throw new PepperModuleXMLResourceException("Cannot load a xml-resource, because the file can not be read: " + resourceFile);
		}

		SAXParser parser;
		XMLReader xmlReader;

		SAXParserFactory factory = SAXParserFactory.newInstance();

		try {
			parser = factory.newSAXParser();
			xmlReader = parser.getXMLReader();
			xmlReader.setErrorHandler(contentHandler);
			xmlReader.setContentHandler(contentHandler);
		} catch (ParserConfigurationException e) {
			throw new PepperModuleXMLResourceException("Cannot load a xml-resource '" + resourceFile.getAbsolutePath() + "'.", e);
		} catch (Exception e) {
			throw new PepperModuleXMLResourceException("Cannot load a xml-resource '" + resourceFile.getAbsolutePath() + "'.", e);
		}
		try {
			InputStream inputStream = new FileInputStream(resourceFile);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			xmlReader.parse(is);
		} catch (SAXException e) {
			try {
				parser = factory.newSAXParser();
				xmlReader = parser.getXMLReader();
				xmlReader.setContentHandler(contentHandler);
				xmlReader.parse(resourceFile.getAbsolutePath());
			} catch (Exception e1) {
				throw new PepperModuleXMLResourceException("Cannot load a xml-resource '" + resourceFile.getAbsolutePath() + "'.", e1);
			}
		} catch (Exception e) {
			if (e instanceof PepperModuleException) {
				throw (PepperModuleException) e;
			} else {
				throw new PepperModuleXMLResourceException("Cannot read xml-file'" + documentLocation + "', because of a nested exception. ", e);
			}
		}
	}


}
