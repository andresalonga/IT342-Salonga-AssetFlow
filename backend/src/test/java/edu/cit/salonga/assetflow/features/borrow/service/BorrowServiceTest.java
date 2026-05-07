package edu.cit.salonga.assetflow.features.borrow.service;

import edu.cit.salonga.assetflow.features.assets.entity.Asset;
import edu.cit.salonga.assetflow.features.assets.repository.AssetRepository;
import edu.cit.salonga.assetflow.features.auth.entity.Role;
import edu.cit.salonga.assetflow.features.auth.entity.User;
import edu.cit.salonga.assetflow.features.auth.repository.UserRepository;
import edu.cit.salonga.assetflow.features.borrow.dto.BorrowRequestCreateDto;
import edu.cit.salonga.assetflow.features.borrow.dto.BorrowRequestDto;
import edu.cit.salonga.assetflow.features.borrow.entity.Transaction;
import edu.cit.salonga.assetflow.features.borrow.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for BorrowService
 * Tests borrow request logic: submission, status transitions, validation
 */
@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BorrowService borrowService;

    private User testUser;
    private Asset testAsset;
    private BorrowRequestCreateDto createDto;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("student@test.edu");
        testUser.setName("John Student");
        testUser.setRole(Role.USER);

        testAsset = new Asset();
        testAsset.setId(5L);
        testAsset.setName("Microscope");
        testAsset.setSerialNumber("MICRO-001");
        testAsset.setStatus(Asset.AssetStatus.AVAILABLE);

        createDto = new BorrowRequestCreateDto();
        createDto.dueDate = LocalDate.now().plusDays(7);

        testTransaction = new Transaction();
        testTransaction.setId(10L);
        testTransaction.setUser(testUser);
        testTransaction.setAsset(testAsset);
        testTransaction.setRequestDate(LocalDate.now());
        testTransaction.setDueDate(createDto.dueDate);
        testTransaction.setStatus(Transaction.TransactionStatus.PENDING);
        testTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testSubmitBorrowRequestSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(assetRepository.findById(5L)).thenReturn(Optional.of(testAsset));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        BorrowRequestDto result = borrowService.submitBorrowRequest(1L, 5L, createDto);

        // Assert
        assertNotNull(result);
        assertEquals("PENDING", result.status);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testSubmitBorrowAssetUnavailable() {
        // Arrange - Asset is already borrowed
        testAsset.setStatus(Asset.AssetStatus.BORROWED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(assetRepository.findById(5L)).thenReturn(Optional.of(testAsset));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            borrowService.submitBorrowRequest(1L, 5L, createDto),
            "Should throw exception when asset is not available"
        );
        verify(transactionRepository, times(0)).save(any(Transaction.class));
    }

    @Test
    void testSubmitBorrowPastDueDate() {
        // Arrange
        createDto.dueDate = LocalDate.now().minusDays(1); // Past date
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(assetRepository.findById(5L)).thenReturn(Optional.of(testAsset));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            borrowService.submitBorrowRequest(1L, 5L, createDto),
            "Should throw exception for past due date"
        );
    }

    @Test
    void testApproveBorrowRequest() {
        // Arrange
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(testTransaction));
        testTransaction.setAsset(testAsset);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        BorrowRequestDto result = borrowService.updateRequestStatus(10L, "APPROVED");

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.status);
        assertEquals(Asset.AssetStatus.BORROWED, testAsset.getStatus());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testRejectBorrowRequest() {
        // Arrange
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        BorrowRequestDto result = borrowService.updateRequestStatus(10L, "REJECTED");

        // Assert
        assertNotNull(result);
        assertEquals("REJECTED", result.status);
        assertEquals(Asset.AssetStatus.AVAILABLE, testAsset.getStatus());
    }

    @Test
    void testReturnAsset() {
        // Arrange
        testTransaction.setStatus(Transaction.TransactionStatus.APPROVED);
        testAsset.setStatus(Asset.AssetStatus.BORROWED);
        
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            assertNotNull(tx.getReturnDate(), "Return date should be set");
            assertEquals(Transaction.TransactionStatus.RETURNED, tx.getStatus());
            return tx;
        });

        // Act
        BorrowRequestDto result = borrowService.updateRequestStatus(10L, "RETURNED");

        // Assert
        assertNotNull(result);
        assertEquals("RETURNED", result.status);
        assertNotNull(result.returnDate);
        assertEquals(Asset.AssetStatus.AVAILABLE, testAsset.getStatus());
    }

    @Test
    void testGetUserRequests() {
        // Arrange
        List<Transaction> userTransactions = Arrays.asList(
            testTransaction,
            createTransaction(11L, "APPROVED")
        );
        when(transactionRepository.findByUserId(1L)).thenReturn(userTransactions);

        // Act
        List<BorrowRequestDto> results = borrowService.getUserRequests(1L);

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(tr -> "1".equals(tr.userId)));
    }

    @Test
    void testGetAllRequests() {
        // Arrange
        List<Transaction> allTransactions = Arrays.asList(
            testTransaction,
            createTransactionForUser(11L, new User() {{ setId(2L); setEmail("student2@test.edu"); }})
        );
        when(transactionRepository.findAll()).thenReturn(allTransactions);

        // Act
        List<BorrowRequestDto> results = borrowService.getAllRequests();

        // Assert
        assertEquals(2, results.size());
    }

    // Helper methods
    private Transaction createTransaction(Long txId, String status) {
        Transaction tx = new Transaction();
        tx.setId(txId);
        tx.setUser(testUser);
        tx.setAsset(testAsset);
        tx.setRequestDate(LocalDate.now());
        tx.setStatus(Transaction.BorrowStatus.valueOf(status));
        return tx;
    }

    private Transaction createTransactionForUser(Long txId, User user) {
        Transaction tx = new Transaction();
        tx.setId(txId);
        tx.setUser(user);
        tx.setAsset(testAsset);
        tx.setRequestDate(LocalDate.now());
        tx.setStatus(Transaction.BorrowStatus.PENDING);
        return tx;
    }

}
