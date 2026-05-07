package edu.cit.salonga.assetflow.features.borrow.repository;

import edu.cit.salonga.assetflow.features.borrow.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByAssetId(Long assetId);
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    List<Transaction> findByUserIdOrderByRequestDateDesc(Long userId);
}
