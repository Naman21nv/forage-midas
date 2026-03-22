package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IncentiveClient {

    private final RestTemplate restTemplate;
    private final String incentiveUrl;

    public IncentiveClient(RestTemplate restTemplate, @Value("${incentive.url}") String incentiveUrl) {
        this.restTemplate = restTemplate;
        this.incentiveUrl = incentiveUrl;
    }

    public Incentive getIncentive(Transaction transaction) {
        try {
            return restTemplate.postForObject(incentiveUrl, transaction, Incentive.class);
        } catch (Exception e) {
            return new Incentive(0f);
        }
    }
}
