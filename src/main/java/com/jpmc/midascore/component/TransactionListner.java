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

@Component
public class TransactionListner {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListner.class);
    
    private final UserRepository userRepository;
    private final DatabaseConduit databaseConduit;
    private final IncentiveClient incentiveClient;

    public TransactionListner(UserRepository userRepository, DatabaseConduit databaseConduit, IncentiveClient incentiveClient) {
        this.userRepository = userRepository;
        this.databaseConduit = databaseConduit;
        this.incentiveClient = incentiveClient;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    public void listen(Transaction transaction) {
        if (transaction == null) return;

        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender != null && recipient != null) {
            if (sender.getBalance() >= transaction.getAmount()) {
                // Adjust sender balance
                sender.setBalance(sender.getBalance() - transaction.getAmount());

                // Fetch incentive and add to recipient balance
                Incentive incentive = incentiveClient.getIncentive(transaction);
                float incentiveAmount = incentive != null ? incentive.getAmount() : 0f;
                recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

                // Save adjusted balances
                databaseConduit.save(sender);
                databaseConduit.save(recipient);

                // Save transaction record with incentive
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
