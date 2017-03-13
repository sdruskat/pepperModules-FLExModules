/**
 * 
 */
package org.corpus_tools.peppermodules.flex.readers;

import java.util.HashMap;
import java.util.Map;

import org.corpus_tools.peppermodules.flex.model.FLExText;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SNode;
import org.xml.sax.ext.DefaultHandler2;

/**
 * @author Stephan Druskat
 * 
 * // TODO Add description
 *
 */
public class FLExReader extends DefaultHandler2 implements FLExText {
	
	/**
	 * 
	 */
	private Map<String, Integer> multipleAnnoMap = new HashMap<>();
	
	/**
	 * Forwards to
	 * {@link #createAnnotation(SDocument, String, String, String, boolean)}
	 * with the `isMeta` argument set to `true`.
	 * 
	 * @param doc
	 * @param namespace
	 * @param name
	 * @param value
	 */
	protected void createMetaAnnotation(SNode node, String namespace, String name, String value) {
		createAnnotation(node, namespace, name, value, true);
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
	 * Handles failsafe creation of meta annotations by checking against a map
	 * whether an annotation with the same namespace and name already exists,
	 * and adding an incrementing counter to the name should it exist.
	 * 
	 * @param doc
	 * @param string
	 * @param string2
	 * @param activeElementValue2
	 */
	private void createAnnotation(SNode node, String namespace, String name, String value, boolean isMeta) {
		String pattern = namespace + PROCESSING__KEY_VALUE_SEPARATOR + name;
		if (multipleAnnoMap.containsKey(pattern)) {
			Integer count = multipleAnnoMap.get(pattern);
			multipleAnnoMap.put(namespace + PROCESSING__KEY_VALUE_SEPARATOR + name, count++);
			if (isMeta) {
				node.createMetaAnnotation(namespace, name + PROCESSING__UNDERSCORE + count.toString(), value);
			} else {
				node.createAnnotation(namespace, name + PROCESSING__UNDERSCORE + count.toString(), value);
			}
		} else {
			multipleAnnoMap.put(pattern, 1);
			if (isMeta)
				node.createMetaAnnotation(namespace, name, value);
			else {
				node.createAnnotation(namespace, name, value);
			}
		}
	}

}
