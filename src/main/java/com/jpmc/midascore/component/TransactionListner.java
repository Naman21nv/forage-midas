package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListner {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListner.class);
    private int transactionCount = 0;

    @KafkaListener(topics = "trader-updates", groupId = "midas-core-group")
    public void listen(Transaction transaction) {
        transactionCount++;
        if (transactionCount <= 4) {
            logger.info("Received Transaction #{}: Amount = {}", transactionCount, transaction.getAmount());
        }
    }
}
