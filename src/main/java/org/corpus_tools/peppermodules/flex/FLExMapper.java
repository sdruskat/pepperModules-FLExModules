/*******************************************************************************
 * Copyright (c) 2016, 2018ff. Stephan Druskat
 * Exploitation rights for this version belong exclusively to Humboldt-Universität zu Berlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Stephan Druskat - initial API and implementation
 *******************************************************************************/
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
import org.corpus_tools.peppermodules.flex.model.FLExText;
import org.corpus_tools.peppermodules.flex.readers.FLExDocumentReader;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * A mapper for FLEx XML.
 *
 * @author Stephan Druskat <mail@sdruskat.net>
 *
 */
class FLExMapper extends PepperMapperImpl {
	
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
		graph.addLayer(getLayer(FLExText.ITEM_LAYER_PHRASE));
		graph.addLayer(getLayer(FLExText.ITEM_LAYER_WORD));
		graph.addLayer(getLayer(FLExText.ITEM_LAYER_MORPH));
		graph.addLayer(getLayer(FLExText.TOKEN_LAYER_LEXICAL));
		graph.addLayer(getLayer(FLExText.TOKEN_LAYER_MORPHOLOGICAL));
		
		// Read document
		FLExDocumentReader reader = new FLExDocumentReader(getDocument(), getProperties());
		try {
			this.readXMLResource(reader, getResourceURI());
		}
		catch (Exception e) {
			logger.error("An error occurred while reading the file '{}'.", getResourceURI().path(), e);
			return DOCUMENT_STATUS.FAILED;
		}
		return DOCUMENT_STATUS.COMPLETED;
	}

	/**
	 * Creates and returns a Salt
	 * {@link SLayer} with the passed name.
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
