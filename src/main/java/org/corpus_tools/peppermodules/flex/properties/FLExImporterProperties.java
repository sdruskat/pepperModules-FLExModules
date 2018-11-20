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
package org.corpus_tools.peppermodules.flex.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;

/**
 * Properties for the FLExImporter.
 * 
 * The single properties are explained in the respective field Javadoc.
 * 
 * **Note:** The properties should be considered the central API for this
 * and the other Pepper modules. 
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class FLExImporterProperties extends PepperModuleProperties {

	/**
	 * Default serial version ID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * A map with original 'lang' strings and the target strings
	 * the original should be changed to during conversion.
	 */
	static final String PROP_LANGUAGEMAP = "languageMap";
	/**
	 * A map with original 'type' strings and the target strings
	 * the original should be changed to during conversion.
	 */
	static final String PROP_TYPEMAP = "typeMap";
	/**
	 * A constant for the the mapping symbol in
	 * {@link #PROP_LANGUAGEMAP} and {@link #PROP_TYPEMAP}.
	 */
	private static final String MAPPING_EQUAL_SYMBOL = "=";
	
	/**
	 * No-arg constructor adding all properties to the instance.
	 */
	public FLExImporterProperties() {
	addProperty(PepperModuleProperty.create().withName(PROP_LANGUAGEMAP).withType(String.class).withDescription(
			"Map for changing FLEx 'lang' element values during conversion. Syntax: 'original-value=new-value,English=en")
			.isRequired(false).build());
	addProperty(PepperModuleProperty.create().withName(PROP_TYPEMAP).withType(String.class).withDescription(
			"Map for changing FLEx 'type' element values (i.e., annotation keys) during conversion. Syntax: 'original-value=new-value,gls=ge")
			.isRequired(false).build());
	}
	
	@SuppressWarnings("javadoc")
	public Map<String, String> getLanguageMap() {
		return buildMap(getProperty(PROP_LANGUAGEMAP));
	}

	@SuppressWarnings("javadoc")
	public Map<String, String> getTypeMap() {
		return buildMap(getProperty(PROP_TYPEMAP));
	}

	/**
	 * Creates a {@link Map} from the passed
	 * string argument, iff the String represents
	 * a comma-separated list of strings, which
	 * in turn match the pattern
	 * `\s*.+\s*=\s*.+\s*`.
	 * 
	 * @param property The property whose value is being mapped
	 * @return a {@link Map} representation of the argument
	 */
	private Map<String, String> buildMap(PepperModuleProperty<?> property) {
		Map<String, String> map = new HashMap<>();
		String mapString = (String) property.getValue();
		if (mapString != null) {
			String[] entryArray = mapString.split(",");
			// Trim all entries
			Set<String> trimmedEntries = new HashSet<>();
			for (String entry : entryArray) {
				trimmedEntries.add(entry.trim());
			}
			// Split entries at "=", and put in map with first as key and second
			// as value
			for (String entry : trimmedEntries) {
				String[] splitEntry = entry.split(MAPPING_EQUAL_SYMBOL);
				map.put(splitEntry[0].trim(), splitEntry[1].trim());
			}
		}
		return map;
	}

}
