package leon.schenk.interfaces;

import leon.schenk.ProxyInterface;

@ProxyInterface(target = "org.springframework.beans.factory.config.ConfigurableListableBeanFactory")
public interface ConfigurableListableBeanFactory {

    BeanDefinition getBeanDefinition(String beanDefinitionName);
}
