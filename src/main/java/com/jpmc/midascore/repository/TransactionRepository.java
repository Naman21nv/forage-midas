package com.jpmc.midascore.repository;

import com.jpmc.midascore.entity.TransactionRecord;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository interface for managing TransactionRecord entities.
 * 
 * Flow & Purpose:
 * In Spring Data JPA, you don't need to write custom SQL statements for basic
 * CRUD (Create, Read, Update, Delete) operations. You simply create an interface 
 * that extends CrudRepository or JpaRepository.
 * 
 * Spring scans this package, detects the interface, and automatically provides 
 * the implementation at runtime. 
 * 
 * Here, we extend CrudRepository with type parameters <TransactionRecord, Long>.
 * - TransactionRecord: Tells Spring what Entity this repository maps to.
 * - Long: Tells Spring the data type of the Primary Key (@Id) for that Entity.
 * 
 * This provides the DatabaseConduit with out-of-the-box access to save() and findAll().
 */
public interface TransactionRepository extends CrudRepository<TransactionRecord, Long> {
}
