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
		System.err.println("-------------------- MAPPING CORPUS ---------------------");
		getCorpus().setName(getResourceURI().lastSegment());
		return DOCUMENT_STATUS.COMPLETED;
	}

	@Override
	public DOCUMENT_STATUS mapSDocument() {
		System.err.println("------------ MAPPING DOCUMENT --------------------");
//		getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
		SDocumentGraph graph = getDocument().getDocumentGraph() == null ? SaltFactory.createSDocumentGraph() : getDocument().getDocumentGraph();
		getDocument().setDocumentGraph(graph);
		FLExDocumentReader reader = null;

		if (getDocument().getDocumentGraph() != null) {
			reader = new FLExDocumentReader(getDocument());
		}
		else {
			logger.error("SDocumentGraph for " + getResourceURI() + " is null!");
			return DOCUMENT_STATUS.FAILED;
		}

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

}
