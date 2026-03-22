package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * The core business logic component of the Midas application.
 * 
 * Flow & Purpose:
 * 1. Listening to Kafka: The class is annotated with @Component, making it a Spring Bean.
 *    The method listen() uses @KafkaListener to bind to the topic defined in our application.yml.
 *    Whenever a JSON message is published to the 'trader-updates' topic, Spring Kafka 
 *    deserializes the JSON into our Foundation 'Transaction' object and triggers this method.
 * 
 * 2. Validation Rule 1 & 2: It queries the database using the UserRepository to ensure 
 *    that both the senderId and recipientId mapped in the message belong to real users.
 * 
 * 3. Validation Rule 3: It checks if the Sender actually has enough money in their balance 
 *    to fulfill the transaction amount.
 * 
 * 4. External API Call: If the transaction is valid, it makes an HTTP POST request via the 
 *    IncentiveClient to a standalone Incentive API. This API returns a bonus "incentive" amount.
 * 
 * 5. State Mutation: The sender's balance is deducted strictly by the transaction amount. 
 *    The recipient's balance is incremented by the transaction amount PLUS the incentive amount.
 * 
 * 6. Persistence: It relies on the DatabaseConduit to save the updated sender state, 
 *    updated recipient state, and finally, a relational TransactionRecord (tracking both users, 
 *    the amount, and the incentive) to the H2 database.
 */
@Component
public class TransactionListner {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListner.class);
    
    // Dependencies injected by Spring
    private final UserRepository userRepository;
    private final DatabaseConduit databaseConduit;
    private final IncentiveClient incentiveClient;

    /**
     * Constructor used by Spring to inject dependencies.
     */
    public TransactionListner(UserRepository userRepository, DatabaseConduit databaseConduit, IncentiveClient incentiveClient) {
        this.userRepository = userRepository;
        this.databaseConduit = databaseConduit;
        this.incentiveClient = incentiveClient;
    }

    /**
     * Consumes messages from Kafka.
     * @param transaction The deserialized JSON object containing senderId, recipientId, and amount.
     */
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    public void listen(Transaction transaction) {
        if (transaction == null) return;

        // Fetch entities from database to ensure they exist
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender != null && recipient != null) {
            
            // Check if sender has enough money
            if (sender.getBalance() >= transaction.getAmount()) {
                
                // Adjust sender balance (deducts exactly what they meant to send)
                sender.setBalance(sender.getBalance() - transaction.getAmount());

                // Fetch external incentive
                Incentive incentive = incentiveClient.getIncentive(transaction);
                float incentiveAmount = incentive != null ? incentive.getAmount() : 0f;
                
                // Add the main amount AND the incentive to the recipient
                recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

                // Save adjusted balances back to the Database
                databaseConduit.save(sender);
                databaseConduit.save(recipient);

                // Create a relational record and save it
                TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount(), incentiveAmount);
                databaseConduit.save(transactionRecord);
                
                logger.info("Transaction processed: Sender {}, Recipient {}, Amount {}, Incentive {}", sender.getName(), recipient.getName(), transaction.getAmount(), incentiveAmount);
            } else {
                logger.info("Transaction discarded: Sender {} has insufficient balance", sender.getName());
            }
        } else {
            logger.info("Transaction discarded: Invalid sender or recipient");
        }
    }
}
