package leon.schenk;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import leon.schenk.interfaces.BeanDefinition;
import leon.schenk.interfaces.ConfigurableApplicationContext;
import leon.schenk.interfaces.ConfigurableEnvironment;
import leon.schenk.interfaces.ConfigurableListableBeanFactory;
import leon.schenk.interfaces.EnumerablePropertySource;
import leon.schenk.interfaces.MutablePropertySources;
import leon.schenk.interfaces.PropertySource;
import leon.schenk.interfaces.ResolvableType;
import leon.schenk.interfaces.SpringApplication;

/**
 * Says "Hi" to the user.
 *
 */
@Mojo(name = "sayhi", defaultPhase = LifecyclePhase.SITE, requiresDependencyResolution = ResolutionScope.RUNTIME, requiresProject = true, threadSafe = false)
public class ReportingApp extends AbstractMavenReport {
    @Parameter(readonly = true)
    private String springApplicationClass;

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
        mainSink.text("Spring components");
        mainSink.title_();
        mainSink.head_();

        mainSink.body();

        // Heading 1
        mainSink.section1();
        mainSink.sectionTitle1();
        mainSink.text("Spring components");
        mainSink.sectionTitle1_();

        // Content
        mainSink.paragraph();
        mainSink.list();

        final ClassLoader classLoader = getProjectClassLoader();
        final ProxyGenerator proxyGenerator = new ProxyGenerator(classLoader);

        final Thread thread = new Thread(() -> {
            try {
                final SpringApplication springApplication = proxyGenerator.staticContext(SpringApplication.class).construct(proxyGenerator.referenceClass(springApplicationClass));

                final ConfigurableApplicationContext applicationContext = springApplication.run();

                print(proxyGenerator, mainSink, getSinkFactory(), applicationContext);

                applicationContext.close();
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        });
        thread.setContextClassLoader(classLoader);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mainSink.paragraph_();

        // Close
        mainSink.section1_();
        mainSink.body_();
    }

    private void print(final ProxyGenerator proxyGenerator, final Sink mainSink, final SinkFactory sinkFactory, final ConfigurableApplicationContext applicationContext) {
        try {
            final String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
            final ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
            final ConfigurableEnvironment environment = applicationContext.getEnvironment();

            final MutablePropertySources propertySources = environment.getPropertySources();

            final List<String> propertyNames = new ArrayList<>();
            for (final PropertySource<?> propertySource : propertySources) {
                getLog().info(propertySource.toString());
                if (propertySource instanceof EnumerablePropertySource) {
                    if (!Arrays.asList("systemProperties", "systemEnvironment").contains(propertySource.getName())) {
                        final String[] propertyNamesL = ((EnumerablePropertySource<?>)propertySource).getPropertyNames();
                        propertyNames.addAll(Arrays.asList(propertyNamesL));
                    }
                }
            }

            Arrays.sort(beanDefinitionNames);
            Collections.sort(propertyNames);
            for (final String beanDefinitionName : beanDefinitionNames) {
                final Sink perBeanSink = getSinkFactory().createSink(outputDirectory, hashMD5(beanDefinitionName) + ".html");
                final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
                getLog().info(String.format("Bean %s is defined by: %s", beanDefinitionName, beanDefinition));

                mainSink.listItem();
                mainSink.link(hashMD5(beanDefinitionName) + ".html");
                mainSink.text(beanDefinitionName + " - " + beanDefinition.getBeanClassName());
                mainSink.link_();
                mainSink.listItem_();
                getLog().info(String.format("Bean: %s", beanDefinitionName));

                perBeanSink.head();
                perBeanSink.title();
                perBeanSink.text("Spring components");
                perBeanSink.title_();
                perBeanSink.head_();
                perBeanSink.body();
                perBeanSink.section1();
                perBeanSink.sectionTitle1();
                perBeanSink.text("Spring bean - " + beanDefinitionName);
                perBeanSink.sectionTitle1_();
                perBeanSink.paragraph();
                final ResolvableType resolvableType = beanDefinition.getResolvableType();
                final Class<?> rawClazz = resolvableType == null ? null : resolvableType.getRawClass();
                perBeanSink.text("Class: " + rawClazz);
                perBeanSink.text("Annotations: " + Arrays.toString(rawClazz == null ? null : rawClazz.getAnnotations()));
                perBeanSink.paragraph_();
                perBeanSink.paragraph();
                perBeanSink.text(beanDefinition.toString());
                perBeanSink.paragraph_();
                perBeanSink.paragraph();
                perBeanSink.text(beanDefinition.getResourceDescription());
                perBeanSink.paragraph_();
                perBeanSink.body_();
                perBeanSink.close();
            }
            mainSink.list_();
            mainSink.list();
            for (final String property : propertyNames) {
                mainSink.listItem();
                mainSink.text(property + " - ");
                mainSink.text(environment.getProperty(property));
                mainSink.listItem_();
                getLog().info(String.format("Property: %s", property));
            }
            mainSink.list_();
            getLog().info(String.format("Application Context %s", applicationContext));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ClassLoader getProjectClassLoader() throws MavenReportException {
        try {
            final List<String> classpathElements = getProject().getRuntimeClasspathElements();
            final URL urls[] = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); i++) {
                urls[i] = new File(classpathElements.get(i)).toURI().toURL();
            }
            return new URLClassLoader(urls);
        } catch (final Exception e) {
            throw new MavenReportException("Kan het project niet laden.", e);
        }
    }

    private String hashMD5(final String input) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes());
        final byte[] outputBytes = md.digest();
        return convertToHex(outputBytes);
    }

    private String convertToHex(final byte[] input) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length; i++) {
            sb.append(String.format("%02X", input[i]));
        }
        return sb.toString();
    }
}
