package com.jpmc.midascore.entity;

import jakarta.persistence.*;

/**
 * Entity class representing a Transaction in the database.
 * 
 * Flow & Purpose:
 * When the TransactionListener receives a valid Kafka message and verifies
 * that both the sender and recipient are valid and that the sender has sufficient
 * funds, we need a way to persist that transaction to our H2 database.
 * 
 * We cannot use the foundational Transaction class because it does not represent
 * foreign key constraints (it only has raw long IDs). Thus, this Entity class
 * was created. It leverages standard JPA (Java Persistence API) annotations 
 * to tell Spring and Hibernate how to map this Java object to a SQL table.
 */
@Entity
public class TransactionRecord {

    /**
     * Primary key for the transaction table.
     * The GenerationType.IDENTITY strategy tells the database to automatically 
     * auto-increment this value whenever a new transaction is saved.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many-to-One relationship mapping the sender to a UserRecord.
     * In the database, this translates to a 'sender_id' column that acts
     * as a foreign key pointing to the UserRecord table. A single user can be 
     * a sender for many transactions.
     */
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserRecord sender;

    /**
     * Many-to-One relationship mapping the recipient to a UserRecord.
     * Maps to a 'recipient_id' column, enforcing relational integrity.
     */
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserRecord recipient;

    /**
     * The flat amount that was transferred from the sender to the recipient.
     */
    @Column(nullable = false)
    private float amount;

    /**
     * The incentive amount retrieved from the external Incentive API.
     * This field is added to track how much bonus cash the recipient received
     * on top of the base transaction amount.
     */
    @Column(nullable = false)
    private float incentive;

    /**
     * Empty constructor required by JPA.
     * Hibernate uses reflection to instantiate this class, so a no-args 
     * constructor is mandatory.
     */
    protected TransactionRecord() {
    }

    /**
     * Parameterized constructor used by our application code to easily 
     * instantiate new records before persisting them via the DatabaseConduit.
     */
    public TransactionRecord(UserRecord sender, UserRecord recipient, float amount, float incentive) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.incentive = incentive;
    }

    public Long getId() {
        return id;
    }

    public UserRecord getSender() {
        return sender;
    }

    public void setSender(UserRecord sender) {
        this.sender = sender;
    }

    public UserRecord getRecipient() {
        return recipient;
    }

    public void setRecipient(UserRecord recipient) {
        this.recipient = recipient;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getIncentive() {
        return incentive;
    }

    public void setIncentive(float incentive) {
        this.incentive = incentive;
    }
}
