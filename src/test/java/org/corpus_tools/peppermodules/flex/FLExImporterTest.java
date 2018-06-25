package org.corpus_tools.peppermodules.flex;

import org.corpus_tools.peppermodules.flex.FLExImporter;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.eclipse.emf.common.util.URI;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.anyOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
 * // TODO Add description
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

	@Test
	public void test_DummyImplementation() {
		getFixture().getCorpusDesc().setCorpusPath(getTempURI("FLExImporter"));
		start();

		assertNotNull(getFixture().getSaltProject());
		assertThat(getFixture().getSaltProject().getCorpusGraphs().get(0).getCorpora().get(0).getName(),
				is("FLExImporter"));
	}

	@Test
	public void testCorrectResolutionOfMissingMorphologyMaterialAndAnnotations() {
		getFixture().setCorpusDesc(
				new CorpusDesc().setCorpusPath(URI.createFileURI(getFile("missing-morph-annos.flextext"))));
		start();

		SDocumentGraph graph = getFixture().getSaltProject().getCorpusGraphs().get(0).getDocuments().get(0)
				.getDocumentGraph();
		assertNotNull(graph);
		SLayer wordLayer = graph.getLayerByName("words").get(0);
		SLayer morphLayer = graph.getLayerByName("morphemes").get(0);
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
		project.saveSaltProject(URI.createFileURI("/home/stephan/tmp/flextest/saltproject/"));
	}

	private String getFile(String fileName) {
		return this.getClass().getClassLoader().getResource(fileName).getFile();
	}

}
