package fi.maanmittauslaitos.pta.search.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ApplicationConfiguration.class)
public class TestConfig {
    @Bean
    public HakuController hakuController() {
        return new HakuController();
    }
}
