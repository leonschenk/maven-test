package leon.schenk;
 
import java.util.Locale;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
 
/**
 * Says "Hi" to the user.
 *
 */
@Mojo(name = "sayhi", defaultPhase = LifecyclePhase.SITE, requiresDependencyResolution = ResolutionScope.RUNTIME, requiresProject = true, threadSafe = true)
public class App extends AbstractMavenReport 
{
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public String getOutputName() {
        return "simple-report";
    }

    @Override
    public String getName(Locale arg0) {
        return "Simple Report";
    }

    @Override
    public String getDescription(Locale arg0) {
        return "This simple report is a very simple report that does nothing but shows off Maven's wonderful reporting capabilities.";
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        final Log logger = getLog();
        logger.info("Generating " + getOutputName() + ".html for " + project.getName() + " " + project.getVersion());

        final Sink mainSink = getSink();
        if (mainSink == null) {
            throw new MavenReportException("Could not get the Doxia sink");
        }
 
        // Page title
        mainSink.head();
        mainSink.title();
        mainSink.text("Simple Report for " + project.getName() + " " + project.getVersion());
        mainSink.title_();
        mainSink.head_();
 
        mainSink.body();
 
        // Heading 1
        mainSink.section1();
        mainSink.sectionTitle1();
        mainSink.text("Simple Report for " + project.getName() + " " + project.getVersion());
        mainSink.sectionTitle1_();
 
        // Content
        mainSink.paragraph();
        mainSink.text("This page provides simple information, like its location: ");
        mainSink.text(project.getBasedir().getAbsolutePath());
        mainSink.paragraph_();
 
        // Close
        mainSink.section1_();
        mainSink.body_();
    }
}
