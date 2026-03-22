package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client component responsible for communicating with external APIs.
 * 
 * Flow & Purpose:
 * In Microservice architectures, services often need to talk to each other over HTTP.
 * This class abstracts away the complexity of making an HTTP request to the 
 * external 'Incentive API'. 
 * 
 * It utilizes Spring's RestTemplate to easily serialize our Transaction object into 
 * a JSON payload, POST it to a URL, and deserialize the response back into an Incentive object.
 */
@Component
public class IncentiveClient {

    private final RestTemplate restTemplate;
    private final String incentiveUrl;

    /**
     * Constructor injection.
     * @param restTemplate A shared, configured HTTP client provided by Spring.
     * @param incentiveUrl The URL of the API, dynamically loaded from application.yml using @Value.
     */
    public IncentiveClient(RestTemplate restTemplate, @Value("${incentive.url}") String incentiveUrl) {
        this.restTemplate = restTemplate;
        this.incentiveUrl = incentiveUrl;
    }

    /**
     * Makes a synchronous POST request to the Incentive API.
     * 
     * @param transaction The valid transaction that needs an incentive calculation.
     * @return The Incentive object returned by the API. Defaults to 0 if the API fails or is offline.
     */
    public Incentive getIncentive(Transaction transaction) {
        try {
            // Spring handles converting 'transaction' to JSON and 'Incentive.class' from JSON
            return restTemplate.postForObject(incentiveUrl, transaction, Incentive.class);
        } catch (Exception e) {
            // Graceful degradation: if the external service fails, we just grant $0 rather than crashing Midas
            return new Incentive(0f);
        }
    }
}
