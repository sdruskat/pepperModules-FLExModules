/**
 * 
 */
package org.corpus_tools.peppermodules.flex;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.peppermodules.flex.exceptions.DocumentSAXParseFinishedEvent;
import org.corpus_tools.peppermodules.flex.readers.FLExDocumentReader;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.core.SLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public DOCUMENT_STATUS mapSCorpus() {
		getCorpus().setName(getResourceURI().lastSegment());
		return DOCUMENT_STATUS.COMPLETED;
	}

	@Override
	public DOCUMENT_STATUS mapSDocument() {
		SDocumentGraph graph = getDocument().getDocumentGraph() == null ? SaltFactory.createSDocumentGraph() : getDocument().getDocumentGraph();
		// Graph set up
		graph.createTimeline();
		graph.addLayer(getLayer("words"));
		graph.addLayer(getLayer("morphemes"));
		STextualDS morphemes = graph.createTextualDS("");
		morphemes.setName("morphemes");
		STextualDS words = graph.createTextualDS("");
		words.setName("words");
		
		// Set graph on document
		getDocument().setDocumentGraph(graph);

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
