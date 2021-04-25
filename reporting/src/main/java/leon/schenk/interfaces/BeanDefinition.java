package leon.schenk.interfaces;

import leon.schenk.ProxyInterface;

@ProxyInterface(target = "org.springframework.beans.factory.config.BeanDefinition")
public interface BeanDefinition {

    String getBeanClassName();

    ResolvableType getResolvableType();

    String getResourceDescription();
}
