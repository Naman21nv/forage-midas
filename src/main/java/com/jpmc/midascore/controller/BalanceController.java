package com.jpmc.midascore.controller;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller responsible for handling external HTTP requests regarding User Balances.
 * 
 * Flow & Purpose:
 * Midas Core needs to expose data to external web clients. The @RestController annotation 
 * tells Spring that this class acts as a Web API endpoint. 
 * 
 * It intercepts incoming HTTP GET requests at the "/balance" path. It accepts a 'userId' 
 * as a query parameter (e.g., http://localhost:8081/balance?userId=123).
 * 
 * It queries the database using the UserRepository. If the user exists, it maps their 
 * stored balance into a new Balance (Foundation DTO) object. If they don't exist, it 
 * defaults to 0. Spring then automatically serializes this Balance object into a JSON 
 * response sent back to the client.
 */
@RestController
public class BalanceController {

    private final UserRepository userRepository;

    /**
     * Constructor injection for the UserRepository.
     */
    public BalanceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Endpoint to fetch a specific user's balance.
     * 
     * @param userId The ID of the user, passed as a query parameter.
     * @return A serialized JSON representation of the Balance object.
     */
    @GetMapping("/balance")
    public Balance getBalance(@RequestParam Long userId) {
        float amount = userRepository.findById(userId)
                .map(UserRecord::getBalance)
                .orElse(0f);
        return new Balance(amount);
    }
}
