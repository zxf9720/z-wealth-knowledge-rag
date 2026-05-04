package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.external.CustomerProfileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for customer data service.
 */
@Service
public class CustomerDataClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CustomerDataClient(RestTemplate restTemplate,
                              @Value("${services.customer-data.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public CustomerProfileResponse getCustomerProfile(String customerId) {
        return restTemplate.getForObject(
                baseUrl + "/customers/" + customerId,
                CustomerProfileResponse.class
        );
    }
}
