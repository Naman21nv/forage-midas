package com.jpmc.midascore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {
    private final String topic;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaProducer(@Value("${general.kafka-topic}") String topic, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(String transactionLine) {
        String[] transactionData = transactionLine.split(", ");
        Transaction transaction = new Transaction(Long.parseLong(transactionData[0]), Long.parseLong(transactionData[1]), Float.parseFloat(transactionData[2]));
        try {
            String json = objectMapper.writeValueAsString(transaction);
            kafkaTemplate.send(topic, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize transaction", e);
        }
    }
}