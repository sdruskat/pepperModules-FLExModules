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
package org.corpus_tools.peppermodules.flex.readers;

import java.util.HashMap; 
import java.util.Map;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.peppermodules.flex.model.FLExText;
import org.corpus_tools.peppermodules.flex.properties.FLExImporterProperties;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SAbstractAnnotation;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.xml.sax.ext.DefaultHandler2;

/**
 * @author Stephan Druskat
 * 
 * A generic FLExReader class that can create annotations
 * and meta annotations from FLEx 'lang' and 'type' 
 * elements and he respective values.
 */
class FLExReader extends DefaultHandler2 implements FLExText {
	
	/**
	 * 
	 */
	private Map<String, Integer> multipleAnnoMap = new HashMap<>();
	private final PepperModuleProperties properties;
	
	/**
	 * A constructor taking a properties argument and
	 * setting the respective field.
	 * 
	 * @param properties
	 */
	FLExReader(PepperModuleProperties properties) {
		this.properties = properties;
	}

	/**
	 * Forwards to
	 * {@link #createAnnotation(SDocument, String, String, String, boolean)}
	 * with the `isMeta` argument set to `false`.
	 * 
	 * @param doc
	 * @param namespace
	 * @param name
	 * @param value
	 */
	protected void createAnnotation(SNode node, String namespace, String name, String value) {
		createAnnotation(node, namespace, name, value, false);
	}
	
	/**
	 * Checks for language mapping properties,
	 * makes required changed to language strings,
	 * and forwards to
	 * {@link #createAnnotation(SDocument, String, String, String, boolean)}
	 * with the `isMeta` argument set to `false`. 
	 * 
	 * @param node
	 * @param languageString
	 * @param name
	 * @param value
	 */
	protected SAbstractAnnotation createLanguagedAnnotation(SNode node, String languageString, String name, String value) {
		/* 
		 * Check if we have properties of type
		 * FLExImporterProperties attached
		 */
		FLExImporterProperties flexImporterProperties = null;
		if (properties instanceof FLExImporterProperties) {
			flexImporterProperties = (FLExImporterProperties) properties;
		}
		// In case no properties have been attached, forward and return
		else {
			return createAnnotation(node, languageString, name, value, false);
		}
		// From here we can assume we have FLExImporterProperties
		Map<String, String> languageMap = flexImporterProperties.getLanguageMap();
		if (!languageMap.isEmpty()) {
			String newLanguageString = languageMap.get(languageString);
			if (newLanguageString != null) {
				languageString = newLanguageString;
			}
		}
		return createAnnotation(node, languageString, name, value, false);
	}
	/**
	 * Handles failsafe creation of (meta) annotations by checking against a map
	 * whether an annotation with the same namespace and name already exists
	 * for the respective node, and adding an incrementing counter to the 
	 * name should it exist.
	 * 
	 * This method expects node.getId() to not return `null`!
	 * 
	 * @param node The node to create an annotation on (could be graphs, tokens, spans, etc.)
	 * @param namespace The namespace of the annotation to create
	 * @param name The nae of the annotation to create
	 * @param value The value of the annotation to crete
	 * @param isMeta Whether the annotation to create should be of type {@link SMetaAnnotation}
	 */
	private SAbstractAnnotation createAnnotation(SNode node, String namespace, String name, String value, boolean isMeta) {
		// Do we need to change the annotation name?
		String newName = null;
		if (properties instanceof FLExImporterProperties) {
			newName = ((FLExImporterProperties) properties).getTypeMap().get(name);
			if (newName != null) {
				name = newName;
			}
		}
		String pattern = node.getId() + PROCESSING__KEY_VALUE_SEPARATOR + namespace
				+ PROCESSING__KEY_VALUE_SEPARATOR + name;
		if (multipleAnnoMap.containsKey(pattern)) {
			Integer count = multipleAnnoMap.get(pattern);
			count++;
			multipleAnnoMap.put(pattern, count);
			if (isMeta) {
				return node.createMetaAnnotation(namespace, name + PROCESSING__UNDERSCORE + count.toString(), value);
			} else {
				return node.createAnnotation(namespace, name + PROCESSING__UNDERSCORE + count.toString(), value);
			}
		} else {
			multipleAnnoMap.put(pattern, 1);
			if (isMeta)
				return node.createMetaAnnotation(namespace, name, value);
			else {
				return node.createAnnotation(namespace, name, value);
			}
		}
	}

}
