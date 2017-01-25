/**
 * 
 */
package org.corpus_tools.peppermodules.flex.exceptions;

import org.corpus_tools.salt.common.SDocument;
import org.xml.sax.SAXException;

/**
 * TODO Description
 *
 * @author Stephan Druskat <mail@sdruskat.net>
 *
 */
public class DocumentSAXParseFinishedEvent extends SAXException {

	private String docName;

	/**
	 * @param name
	 */
	public DocumentSAXParseFinishedEvent(String name) {
		this.docName = name;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6339266837241951030L;
	
	@Override
	public String getMessage() {
		return "Finished reading " + SDocument.class.getSimpleName() +  " '" + docName + "'.";
	}

}
