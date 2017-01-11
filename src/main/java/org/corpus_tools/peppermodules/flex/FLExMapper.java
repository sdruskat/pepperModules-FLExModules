/**
 * 
 */
package org.corpus_tools.peppermodules.flex;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.salt.SaltFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(FLExMapper.class);
	
	@Override
	public DOCUMENT_STATUS mapSCorpus() {
		// getScorpus() returns the current corpus object.
		getCorpus().createMetaAnnotation(null, "date", "1989-12-17");

		return (DOCUMENT_STATUS.COMPLETED);
	}

	@Override
	public DOCUMENT_STATUS mapSDocument() {
		getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
		URI resource = getResourceURI();
		logger.debug("Importing the file {}.", resource);

		addProgress(0.16);
		// we set progress to 'done' to notify the user about the process
		// status (this is very helpful, especially for longer taking
		// processes)
		setProgress(1.0);

		// now we are done and return the status that everything was
		// successful
		return (DOCUMENT_STATUS.COMPLETED);
	}


}
