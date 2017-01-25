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
import org.eclipse.emf.common.util.URI;
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
		getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
		URI resource = getResourceURI();
		logger.info("Importing the document '{}'.", getDocument().getName());
		
		SDocumentGraph graph = getDocument().getDocumentGraph();
		FLExDocumentReader reader = null;
		if (graph != null) {
			reader = new FLExDocumentReader(graph);
			
		}
		else {
			logger.error("SDocumentGraph for " + resource + " is null!");
		}
		try {
			
			this.readXMLResource(reader, getResourceURI());
		}
		catch (Exception e) {
			if (e.getCause() instanceof DocumentSAXParseFinishedEvent) {
				logger.info(e.getCause().getMessage());
			}
		}

		return DOCUMENT_STATUS.COMPLETED;
	}

}
