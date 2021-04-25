package leon.schenk.interfaces;

import leon.schenk.ProxyConstructor;
import leon.schenk.ProxyInterface;

@ProxyInterface(target = "org.springframework.boot.SpringApplication")
public interface SpringApplication {
    @ProxyConstructor
    SpringApplication construct(final Class<?>... primarySources);

    ConfigurableApplicationContext run(final String... args);
}
