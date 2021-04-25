package leon.schenk.interfaces;

import leon.schenk.ProxyInterface;

@ProxyInterface(target = "org.springframework.core.env.MutablePropertySources")
public interface MutablePropertySources extends Iterable<PropertySource<?>> {

}
