package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Service Layer abstraction to interact with the database.
 * 
 * Flow & Purpose:
 * In Spring applications, you shouldn't directly use repositories inside controllers 
 * or low-level listener logic. Instead, a "Conduit" or "Service" layer is placed
 * in between.
 * 
 * By annotating this class with @Component, Spring recognizes it during its component
 * scanning and creates a Singleton instance of it. It injects the required 
 * repositories through constructor injection, which is considered a best practice.
 * 
 * This class exposes an overloaded save() method. It delegates the responsibility 
 * of saving User and Transaction Entities directly to the underlying repositories.
 */
@Component
public class DatabaseConduit {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Constructor injection. Spring automatically provides instances of the 
     * repositories when constructing this class.
     */
    public DatabaseConduit(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Saves a user back to the database after their balance is updated.
     */
    public void save(UserRecord userRecord) {
        userRepository.save(userRecord);
    }

    /**
     * Records a completely processed transaction in the database.
     */
    public void save(TransactionRecord transactionRecord) {
        transactionRepository.save(transactionRecord);
    }
}
