package leon.schenk.interfaces;

import leon.schenk.ProxyInterface;

@ProxyInterface(target = "org.springframework.core.env.ConfigurableEnvironment")
public interface ConfigurableEnvironment {

    MutablePropertySources getPropertySources();

    String getProperty(final String propertyName);
}
