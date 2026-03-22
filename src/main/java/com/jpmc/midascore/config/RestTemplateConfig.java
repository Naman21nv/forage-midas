package com.jpmc.midascore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class to register third-party beans into the Spring Application Context.
 * 
 * Flow & Purpose:
 * When using classes that belong to the Spring Framework (like RestTemplate) rather than 
 * classes we created ourselves, we cannot simply use @Component. 
 * 
 * Instead, we use a @Configuration class with a @Bean method. When the Midas application 
 * starts up, Spring runs this method, takes the returned RestTemplate object, and holds 
 * it in memory. This allows other components (like our IncentiveClient) to easily ask 
 * Spring to inject a RestTemplate without having to manually construct it themselves.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
