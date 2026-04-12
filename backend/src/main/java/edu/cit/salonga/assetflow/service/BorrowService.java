package edu.cit.salonga.assetflow.service;

import edu.cit.salonga.assetflow.dto.BorrowRequestCreateDto;
import edu.cit.salonga.assetflow.dto.BorrowRequestDto;
import edu.cit.salonga.assetflow.entity.Asset;
import edu.cit.salonga.assetflow.entity.Transaction;
import edu.cit.salonga.assetflow.entity.User;
import edu.cit.salonga.assetflow.repository.AssetRepository;
import edu.cit.salonga.assetflow.repository.TransactionRepository;
import edu.cit.salonga.assetflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    // Submit a borrow request
    public BorrowRequestDto submitBorrowRequest(Long userId, Long assetId, BorrowRequestCreateDto request) {
        System.out.println("📝 Submitting borrow request for user=" + userId + ", asset=" + assetId + ", dueDate=" + request.dueDate);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    System.err.println("❌ User not found: " + userId);
                    return new RuntimeException("User not found");
                });
                
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> {
                    System.err.println("❌ Asset not found: " + assetId);
                    return new RuntimeException("Asset not found");
                });

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAsset(asset);
        transaction.setRequestDate(LocalDate.now());
        transaction.setDueDate(request.dueDate);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        Transaction saved = transactionRepository.save(transaction);
        System.out.println("✅ Borrow request saved: id=" + saved.getId() + ", user=" + user.getName());
        return mapToDto(saved);
    }

    // Get all pending borrow requests (for admin)
    public List<BorrowRequestDto> getAllPendingRequests() {
        System.out.println("📋 Fetching all PENDING borrow requests");
        List<BorrowRequestDto> results = transactionRepository.findByStatus(Transaction.TransactionStatus.PENDING)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        System.out.println("Found " + results.size() + " pending requests");
        return results;
    }

    // Get all borrow requests (for admin)
    public List<BorrowRequestDto> getAllRequests() {
        System.out.println("📋 Fetching ALL borrow requests");
        List<BorrowRequestDto> results = transactionRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        System.out.println("Found " + results.size() + " total requests");
        return results;
    }

    // Get user's borrow requests
    public List<BorrowRequestDto> getUserRequests(Long userId) {
        System.out.println("📋 Fetching borrow requests for user: " + userId);
        List<BorrowRequestDto> results = transactionRepository.findByUserIdOrderByRequestDateDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        System.out.println("Found " + results.size() + " requests for user");
        return results;
    }

    // Update request status
    public BorrowRequestDto updateRequestStatus(Long requestId, String status) {
        System.out.println("🔄 Updating request " + requestId + " to status: " + status);
        
        Transaction transaction = transactionRepository.findById(requestId)
                .orElseThrow(() -> {
                    System.err.println("❌ Request not found: " + requestId);
                    return new RuntimeException("Request not found");
                });

        Transaction.TransactionStatus newStatus = Transaction.TransactionStatus.valueOf(status.toUpperCase());
        transaction.setStatus(newStatus);

        if (newStatus == Transaction.TransactionStatus.APPROVED) {
            transaction.getAsset().setStatus(Asset.AssetStatus.BORROWED);
            assetRepository.save(transaction.getAsset());
            System.out.println("✅ Asset marked as BORROWED");
        } else if (newStatus == Transaction.TransactionStatus.RETURNED) {
            transaction.setReturnDate(LocalDate.now());
            transaction.getAsset().setStatus(Asset.AssetStatus.AVAILABLE);
            assetRepository.save(transaction.getAsset());
            System.out.println("✅ Asset marked as AVAILABLE and return date set");
        }

        Transaction updated = transactionRepository.save(transaction);
        System.out.println("✅ Request status updated");
        return mapToDto(updated);
    }

    // Map Transaction to BorrowRequestDto
    private BorrowRequestDto mapToDto(Transaction transaction) {
        return new BorrowRequestDto(
                transaction.getId(),
                transaction.getUser().getId(),
                transaction.getUser().getName(),
                transaction.getAsset().getId(),
                transaction.getAsset().getName(),
                transaction.getRequestDate(),
                transaction.getDueDate(),
                transaction.getStatus().toString().toLowerCase()
        );
    }
}
