package leon.schenk;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class App implements InitializingBean
{
    @Value("${hello.world}")
    private String helloWorldString;

    public static void main( String[] args )
    {
        final SpringApplication sa = new SpringApplication(App.class);
        ConfigurableApplicationContext run = sa.run();
        System.out.println(run);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(helloWorldString);
    }

    public String getHelloWorld() {
        return helloWorldString;
    }
}
