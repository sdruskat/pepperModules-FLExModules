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
package org.corpus_tools.peppermodules.flex;

import org.corpus_tools.pepper.impl.PepperImporterImpl; 
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

/**
 * A Pepper importer for the FLEx XML format.
 *
 * @author Stephan Druskat <mail@sdruskat.net>
 */
@Component(name = "FLExImporterComponent", factory = "PepperImporterComponentFactory")
public class FLExImporter extends PepperImporterImpl implements PepperImporter{
	
	/**
	 * No-args contructor setting importer module metadata.
	 */
	public FLExImporter() {
		super();
		setName("FLExImporter");
		setSupplierContact(URI.createURI("stephan.druskat@hu-berlin.de"));
		setSupplierHomepage(URI.createURI("http://corpus-tools.org"));
		setDesc("An importer for the XML format written by SIL FLEx (FieldWorks Language Explorer), FLExText.");
		addSupportedFormat("xml", "1.0", null);
		getDocumentEndings().add("flextext");
	}
	
	/* (non-Javadoc)
	 * @see org.corpus_tools.pepper.impl.PepperModuleImpl#createPepperMapper(org.corpus_tools.salt.graph.Identifier)
	 */
	@Override
	public PepperMapper createPepperMapper(Identifier Identifier) {
		FLExMapper mapper = new FLExMapper();
		mapper.setResourceURI(getIdentifier2ResourceTable().get(Identifier));
		return (mapper);
	}

	/* (non-Javadoc)
	 * @see org.corpus_tools.pepper.impl.PepperImporterImpl#isImportable(org.eclipse.emf.common.util.URI)
	 */
	@Override
	public Double isImportable(URI corpusPath) {
		// TODO some code to analyze the given corpus-structure
		return (null);
	}

}
