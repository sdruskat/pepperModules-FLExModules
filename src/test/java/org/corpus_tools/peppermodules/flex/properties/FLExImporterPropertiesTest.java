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
package org.corpus_tools.peppermodules.flex.properties;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.Map;

import org.corpus_tools.peppermodules.flex.properties.FLExImporterProperties;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link FLExImporterProperties}.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 */
public class FLExImporterPropertiesTest {
	
	private static FLExImporterProperties fixture = null;

	/**
	 * Sets up the fixture.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		fixture = new FLExImporterProperties();
	}

	/**
	 * Unit test for {@link FLExImporterProperties#getLanguageMap()}.
	 */
	@Test
	public final void testGetLanguageMap() {
		fixture.setPropertyValue(FLExImporterProperties.PROP_LANGUAGEMAP, " English = en, German=de, mmg-fonipa-x-emic=mmg ");
		Map<String, String> map = fixture.getLanguageMap();
		assertThat(map.size(), is(3));
		assertThat(map.get("English"), is("en"));
		assertThat(map.get("German"), is("de"));
		assertThat(map.get("mmg-fonipa-x-emic"), is("mmg"));
	}
	
	/**
	 * Unit test for {@link FLExImporterProperties#getTypeMap()}.
	 */
	@Test
	public final void testGetAnnotationMap() {
		fixture.setPropertyValue(FLExImporterProperties.PROP_TYPEMAP, " KEY_1 = ke, key-2=k2, key value with space =kv ");
		Map<String, String> map = fixture.getTypeMap();
		assertThat(map.size(), is(3));
		assertThat(map.get("KEY_1"), is("ke"));
		assertThat(map.get("key-2"), is("k2"));
		assertThat(map.get("key value with space"), is("kv"));
	}
	
	/**
	 * @return the fixture
	 */
	public static final FLExImporterProperties getFixture() {
		return fixture;
	}

}
