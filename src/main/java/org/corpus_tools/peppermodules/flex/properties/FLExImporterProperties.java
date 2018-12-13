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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;

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
	 *  A list of annotations that should be ignored during 
	 *  conversion. Annotations are defined as 
	 *  `{phrase|word|morph}::{language}:name`, 
	 *  of which the layer (the first) and the language 
	 *  (the second) element are optional. `languages` 
	 *  is a reserved name and will drop all language 
	 *  meta annotations from the child elements of 
	 *  `<languages/>`.
	 */
	static final String PROP_DROP_ANNOTATIONS = "dropAnnotations";
	/**
	 *  A map whose keys are FLEx annotation and whose
	 *  values are annotations they should be mapped to.
	 *  
	 *  Syntax:
	 *  
	 *  - keys: `{interlinear-text|paragraph|phrase|word|morph}::{language}:name`
	 *  - values: `name`
	 *  - list: `key=value, ...`
	 *  
	 *  Behaviour:
	 *  
	 *  For annotations matching the key pattern, the name will
	 *  be changed to the `name` value. `language` will not be
	 *  changed, layer neither.
	 *  
	 *  Example:
	 *  
	 *  `annotationMap=word::en:gls=ge` will change the FLEx-notated
	 *  annotation `<word><item type="gls" lang="en">green</item></word>`
	 *  to an {@link SAnnotation} on an {@link SLayer} named "word"
	 *  with namespace "en" and name "ge".
	 *  
	 */
	static final String PROP_ANNOTATIONMAP = "annotationMap";
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
			"Map for changing FLEx 'lang' element values during conversion. Syntax: 'original-value=new-value,English=en'")
			.isRequired(false).build());
	addProperty(PepperModuleProperty.create().withName(PROP_TYPEMAP).withType(String.class).withDescription(
			"Map for changing FLEx 'type' element values (i.e., annotation keys) during conversion. Syntax: 'original-value=new-value,gls=ge'")
			.isRequired(false).build());
	addProperty(PepperModuleProperty.create().withName(PROP_DROP_ANNOTATIONS).withType(String.class).withDescription(
			"List of annotations to be dropped during conversion. Syntax: '{phrase|word|morph}::{language}:name,languages,morph::en:hn,fr:gls,morph::dro,xxx'")
			.isRequired(false).build());
	addProperty(PepperModuleProperty.create().withName(PROP_ANNOTATIONMAP).withType(String.class).withDescription(
			"map whose keys are FLEx annotation and whose values are annotations they should be mapped to. Syntax: '{interlinear-text|paragraph|phrase|word|morph}::{language}:name=name,morph::en:gls=ge'")
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
	
	@SuppressWarnings("javadoc")
	public List<Triple<String,String,String>> getAnnotationsToDrop() {
		List<Triple<String, String, String>> list = new ArrayList<>();
		if (getProperty(PROP_DROP_ANNOTATIONS).getValue() == null) {
			return list;
		}
		for (String annotation : ((String) getProperty(PROP_DROP_ANNOTATIONS).getValue()).split(",")) {
			Triple<String, String, String> triple = createTripleFromString(annotation);
			list.add(triple);
		}
		return list;
	}
	
	@SuppressWarnings("javadoc")
	public Map<Triple<String,String,String>,String> getAnnotationMap() {
		Map<Triple<String,String,String>,String> map = new HashMap<>();
		String prop = (String) getProperty(PROP_ANNOTATIONMAP).getValue();
		String[] split = prop.split(",");
		for (String mapping : split) {
			String[] newNameSplit = mapping.split("=");
			if (newNameSplit.length == 2) {
				String tripleString = newNameSplit[0];
				String newName = newNameSplit[1];
				Triple<String, String, String> triple = createTripleFromString(tripleString);
				map.put(triple, newName);
			}
			else {
				throw new PepperModuleException("Property 'annotationMap' is formatted incorrectly (no '=' found for value).");
			}
		}
		return map;
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

	private Triple<String, String, String> createTripleFromString(String string) {
		String layer = null;
		String language = null;
		String name = null;
		String[] layerSplit = string.split("::");
		if (layerSplit.length == 2) {
			// layer::name | layer::language:name
			layer = layerSplit[0].trim();
			String[] languageNameSplit = null;
			if ((languageNameSplit = layerSplit[1].split(":")).length == 2) {
				// layer::language:name
				language = languageNameSplit[0].trim();
				name = languageNameSplit[1].trim();
			}
			else {
				// layer::name
				name = layerSplit[1];
			}
		}
		else {
			// layer:name | name | 'languages'
			String[] languageNameSplit = string.split(":");
			if (languageNameSplit.length == 2) {
				// layer:name
				language = languageNameSplit[0].trim();
				name = languageNameSplit[1].trim();
			}
			else {
				// name | 'languages'
				name = string.trim();
			}
		}
		return Triple.of(layer, language, name);
	}

}
