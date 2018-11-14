package org.corpus_tools.peppermodules.flex;

import org.corpus_tools.peppermodules.flex.FLExImporter;
import org.corpus_tools.peppermodules.flex.model.FLExText;
import org.corpus_tools.peppermodules.flex.properties.FLExImporterProperties;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.eclipse.emf.common.util.URI;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.common.FormatDesc;
import org.corpus_tools.pepper.testFramework.PepperImporterTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;

/**
 * Unit tests for the FLEx importer module.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class FLExImporterTest extends PepperImporterTest {
	private Logger rootLogger;

	@SuppressWarnings("rawtypes")
	private Appender mockAppender;

	/**
	 * This method is called by the JUnit environment each time before a test
	 * case starts. So each time a method annotated with @Test is called. This
	 * enables, that each method could run in its own environment being not
	 * influenced by before or after running test cases.
	 */
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		setFixture(new FLExImporter());
		this.getFixture().getCorpusDesc().getFormatDesc().setFormatName("toolbox-text").setFormatVersion("3.0");
		getFixture().getSaltProject().createCorpusGraph();

		// Logging
		rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		mockAppender = mock(Appender.class);
		when(mockAppender.getName()).thenReturn("MOCK");
		rootLogger.addAppender(mockAppender);
		rootLogger.setLevel(Level.WARN);

		FormatDesc formatDef = new FormatDesc();
		formatDef.setFormatName("xml");
		formatDef.setFormatVersion("1.0");
		addFormatWhichShouldBeSupported(formatDef);
	}

	/**
	 * // TODO Add description
	 * FIXME: What does this actually test?
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testCorrectResolutionOfMissingMorphologyMaterialAndAnnotations() {
		setTestFile("missing-morph-annos.flextext");
		start();

		SDocumentGraph graph = getFixture().getSaltProject().getCorpusGraphs().get(0).getDocuments().get(0)
				.getDocumentGraph();
		assertNotNull(graph);
		SLayer wordLayer = graph.getLayerByName(FLExText.TOKEN_LAYER_LEXICAL).get(0);
		SLayer morphLayer = graph.getLayerByName(FLExText.TOKEN_LAYER_MORPHOLOGICAL).get(0);
		assertNotNull(wordLayer);
		assertNotNull(morphLayer);
		List<SToken> wordtokens = new ArrayList<>();
		List<SToken> morphtokens = new ArrayList<>();
		for (SNode n : wordLayer.getNodes()) {
			if (n instanceof SToken) {
				wordtokens.add((SToken) n);
			}
		}
		for (SNode n : morphLayer.getNodes()) {
			if (n instanceof SToken) {
				morphtokens.add((SToken) n);
			}
		}
		List<SToken> wt = graph.getSortedTokenByText(wordtokens);
		List<SToken> mt = graph.getSortedTokenByText(morphtokens);
		assertThat(graph.getTokens().size(), is(33));
		for (SToken word : wt) {
			List<SRelation> rels = word.getOutRelations();
			for (SRelation rel : rels) {
				if (rel instanceof STimelineRelation) {
					assertThat(((STimelineRelation) rel).getStart(),
							anyOf(equalTo(0), equalTo(2), equalTo(5), equalTo(8), equalTo(10), equalTo(13), equalTo(14),
									equalTo(18), equalTo(20), equalTo(26), equalTo(28), equalTo(30), equalTo(31),
									equalTo(37), equalTo(43), equalTo(46), equalTo(52)));
					assertThat(((STimelineRelation) rel).getEnd(),
							anyOf(equalTo(2), equalTo(5), equalTo(8), equalTo(10), equalTo(13), equalTo(14),
									equalTo(18), equalTo(20), equalTo(26), equalTo(28), equalTo(30), equalTo(31),
									equalTo(37), equalTo(43), equalTo(46), equalTo(52), equalTo(53)));
				}
			}
		}
		for (SToken morpheme : mt) {
			List<SRelation> rels = morpheme.getOutRelations();
			for (SRelation rel : rels) {
				if (rel instanceof STimelineRelation) {
					assertThat(((STimelineRelation) rel).getStart(),
							anyOf(equalTo(0), equalTo(2), equalTo(5), equalTo(8), equalTo(10), equalTo(14), equalTo(18),
									equalTo(20), equalTo(26), equalTo(28), equalTo(30), equalTo(31), equalTo(34),
									equalTo(37), equalTo(43), equalTo(46)));
					assertThat(((STimelineRelation) rel).getEnd(),
							anyOf(equalTo(2), equalTo(5), equalTo(8), equalTo(10), equalTo(13), equalTo(18),
									equalTo(20), equalTo(26), equalTo(28), equalTo(30), equalTo(31), equalTo(34),
									equalTo(37), equalTo(43), equalTo(46), equalTo(52)));
				}
			}
		}
		SaltProject project = getFixture().getSaltProject();
		project.saveSaltProject(URI.createFileURI(System.getProperty("java.io.tmpdir") + "/flextest/saltproject/"));
	}

	private String getFile(String fileName) {
		return this.getClass().getClassLoader().getResource(fileName).getFile();
	}
	
	/* 
	 * ███████╗███████╗ █████╗ ████████╗██╗   ██╗██████╗ ███████╗███████╗
	 * ██╔════╝██╔════╝██╔══██╗╚══██╔══╝██║   ██║██╔══██╗██╔════╝██╔════╝
	 * █████╗  █████╗  ███████║   ██║   ██║   ██║██████╔╝█████╗  ███████╗
	 * ██╔══╝  ██╔══╝  ██╔══██║   ██║   ██║   ██║██╔══██╗██╔══╝  ╚════██║
	 * ██║     ███████╗██║  ██║   ██║   ╚██████╔╝██║  ██║███████╗███████║
	 * ╚═╝     ╚══════╝╚═╝  ╚═╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝╚══════╝╚══════╝
	 */

	/**
	 * Tests whether languages are correctly changed
	 * during the conversion process, with a full
	 * language set.
	 */
	@Test
	public void testLanguageMapping() {
		setTestFile("short-sample.flextext");
		setProperties("properties/language-map.properties");
		start();
		SDocumentGraph graph = getFixture().getSaltProject().getCorpusGraphs().get(0).getDocuments().get(0)
				.getDocumentGraph();
		assertNotNull(graph);
		for (SNode node : graph.getNodes()) {
			for (SAnnotation a : node.getAnnotations()) {
				/* Languages are mapped to namespaces.
				 */
				String val = a.getValue_STEXT();
				String ns = a.getNamespace();
				if (val.equals("pus")) {
					/* 
					 * Original language = "qaa-x-kal", 
					 * should now be "something"
					 */
					assertThat(ns, is("something"));
				}
				else if (val.equals("green")) {
					/* 
					 * Original language = "en", 
					 * should now be "ENGLISH"
					 */
					assertThat(ns, is("ENGLISH"));
				}
				else if (val.equals("french-example")) {
					/* 
					 * Original language = "fr", 
					 * should now be "FRENCH?"
					 */
					assertThat(ns, is("FRENCH?"));
				}
			}
		}
	}
	
	/**
	 * Tests whether languages are correctly changed
	 * during the conversion process, with a non-full
	 * language set (`fr` is not mapped).
	 */
	@Test
	public void testIncompleteLanguageMapping() {
		setTestFile("short-sample.flextext");
		setProperties("properties/incomplete-language-map.properties");
		start();
		SDocumentGraph graph = getFixture().getSaltProject().getCorpusGraphs().get(0).getDocuments().get(0)
				.getDocumentGraph();
		assertNotNull(graph);
		for (SNode node : graph.getNodes()) {
			for (SAnnotation a : node.getAnnotations()) {
				/* 
				 * Nothing else defined in the properties,
				 * hence languages should be mapped to namespaces.
				 */
				String val = a.getValue_STEXT();
				String ns = a.getNamespace();
				if (val.equals("pus")) {
					/* 
					 * Original language = "qaa-x-kal", 
					 * should now be "something"
					 */
					assertThat(ns, is("something"));
				}
				else if (val.equals("green")) {
					/* 
					 * Original language = "en", 
					 * should now be "ENGLISH"
					 */
					assertThat(ns, is("ENGLISH"));
				}
				else if (val.equals("french-example")) {
					/* 
					 * Original language = "fr", 
					 * should be unchanged
					 */
					assertThat(ns, is("fr"));
				}
			}
		}
	}
	
	/**
	 * Tests whether annotations are correctly
	 * added to layers during the conversion phase.
	 */
	@Test
	public void testLayerMapping() {
		setTestFile("short-sample.flextext");
		start();
		SDocumentGraph graph = getFixture().getSaltProject().getCorpusGraphs().get(0).getDocuments().get(0)
				.getDocumentGraph();
		assertNotNull(graph);
		// FLEx level: interlinear-text
		assertThat(graph.getDocument().getAnnotation("en", "title"), notNullValue());
		assertThat(graph.getDocument().getAnnotation("en", "title").getValue_STEXT(), is("Short sample"));
		assertThat(graph.getDocument().getAnnotation("en", "comment"), notNullValue());
		assertThat(graph.getDocument().getAnnotation("en", "comment").getValue_STEXT(), is("This is a short sample for text purposes."));
		// Other FLEx levels
		for (SNode node : graph.getNodes()) {
			for (SAnnotation a : node.getAnnotations()) {
				String val = a.getValue_STEXT();
				SLayer phraseLayer = graph.getLayerByName(FLExText.ITEM_LAYER_PHRASE).get(0);
				SLayer wordLayer = graph.getLayerByName(FLExText.ITEM_LAYER_WORD).get(0);
				SLayer morphLayer = graph.getLayerByName(FLExText.ITEM_LAYER_MORPH).get(0);
				if (val.equals("french-example")) {
					/* 
					 * Level: phrase
					 */
					assertTrue(phraseLayer.getNodes().contains(a.getContainer()));
				}
				else if (val.equals("green-word")) {
					/* 
					 * Level: word
					 */
					assertTrue(wordLayer.getNodes().contains(a.getContainer()));
				}
				else if (val.equals("adj")) {
					/* 
					 * Level: morph
					 */
					assertTrue(morphLayer.getNodes().contains(a.getContainer()));
				}
			}
		}
	}
	
	/**
	 * Tests whether languages are correctly changed
	 * during the conversion process, with a non-full
	 * language set (`fr` is not mapped).
	 */
	@Test
	public void testTypeMapping() {
		setTestFile("short-sample.flextext");
		setProperties("properties/type-map.properties");
		start();
		SDocumentGraph graph = getFixture().getSaltProject().getCorpusGraphs().get(0).getDocuments().get(0)
				.getDocumentGraph();
		assertNotNull(graph);
		/* 
		 * Check that types have been mapped according to
		 * type-map.properties 
		 */
		for (SNode node : graph.getNodes()) {
			for (SAnnotation a : node.getAnnotations()) {
				String val = a.getValue_STEXT();
				if (val.equals("pus")) {
					assertThat(a.getName(), is("tx"));
				}
				else if (val.equals("pusword")) {
					assertThat(a.getName(), is("tx"));
				}
				else if (val.equals("puscf")) {
					assertThat(a.getName(), is("cf"));
				}
				else if (val.equals("1hn")) {
					assertThat(a.getName(), is("nh"));
				}
				else if (val.equals("green")) {
					assertThat(a.getName(), is("ge"));
				}
				else if (val.equals("adj")) {
					assertThat(a.getName(), is("ms"));
				}
			}
		}
	}

	private void setProperties(String fileName) {
		FLExImporterProperties properties = new FLExImporterProperties();
		properties.setPropertyValues(new File(getFile(fileName)));
		getFixture().setProperties(properties);
	}

	private void setTestFile(String fileName) {
		getFixture().setCorpusDesc(new CorpusDesc().setCorpusPath(URI.createFileURI(getFile(fileName))));
	}

}
