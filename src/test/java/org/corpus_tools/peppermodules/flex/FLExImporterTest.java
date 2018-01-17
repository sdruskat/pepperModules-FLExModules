package org.corpus_tools.peppermodules.flex;

import org.corpus_tools.peppermodules.flex.FLExImporter;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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
		rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
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
		assertThat(getFixture().getSaltProject().getCorpusGraphs().get(0).getCorpora().get(0).getName(), is("flextext"));
	}
}
