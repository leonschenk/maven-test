package leon.schenk.interfaces;

import leon.schenk.ProxyInterface;

@ProxyInterface(target = "org.springframework.core.env.PropertySource")
public interface PropertySource<T> {

    String getName();
}
