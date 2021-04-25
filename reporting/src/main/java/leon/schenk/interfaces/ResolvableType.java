package leon.schenk.interfaces;

import leon.schenk.ProxyInterface;

@ProxyInterface(target = "org.springframework.core.ResolvableType")
public interface ResolvableType {

    Class<?> getRawClass();

}
