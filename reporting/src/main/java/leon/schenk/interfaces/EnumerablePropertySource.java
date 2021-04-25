package leon.schenk.interfaces;

import leon.schenk.ProxyInterface;

@ProxyInterface(target = "org.springframework.core.env.EnumerablePropertySource")
public interface EnumerablePropertySource<T> extends PropertySource<T> {

    String[] getPropertyNames();
}
