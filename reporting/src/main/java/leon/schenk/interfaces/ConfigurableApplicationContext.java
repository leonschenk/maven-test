package leon.schenk.interfaces;

import java.io.Closeable;

import leon.schenk.ProxyInterface;

@ProxyInterface(target = "org.springframework.context.ConfigurableApplicationContext")
public interface ConfigurableApplicationContext extends Closeable {
    String[] getBeanDefinitionNames();

    ConfigurableListableBeanFactory getBeanFactory();

    ConfigurableEnvironment getEnvironment();
}
