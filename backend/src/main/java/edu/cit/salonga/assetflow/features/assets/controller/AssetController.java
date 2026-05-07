package edu.cit.salonga.assetflow.features.assets.controller;

import edu.cit.salonga.assetflow.features.borrow.dto.BorrowRequestCreateDto;
import edu.cit.salonga.assetflow.features.borrow.dto.BorrowRequestDto;
import edu.cit.salonga.assetflow.features.borrow.service.BorrowService;
import edu.cit.salonga.assetflow.util.AuthenticationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*")
public class AssetController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private AuthenticationUtil authenticationUtil;

    // Submit borrow request for an asset
    @PostMapping("/{assetId}/borrow")
    public ResponseEntity<?> submitBorrowRequest(
            @PathVariable Long assetId,
            @RequestBody BorrowRequestCreateDto request) {
        try {
            System.out.println("🔵 [AssetController] POST /assets/" + assetId + "/borrow");
            
            Long userId = authenticationUtil.getCurrentUserId();
            System.out.println("📌 Current user ID: " + userId);
            
            BorrowRequestDto result = borrowService.submitBorrowRequest(userId, assetId, request);
            System.out.println("✅ [AssetController] Request created successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ [AssetController] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get all borrow requests (admin only)
    @GetMapping("/borrow-requests")
    public ResponseEntity<?> getAllBorrowRequests() {
        try {
            System.out.println("🔵 [AssetController] GET /assets/borrow-requests");
            
            List<BorrowRequestDto> requests = borrowService.getAllRequests();
            System.out.println("✅ [AssetController] Returning " + requests.size() + " requests");
            
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            System.err.println("❌ [AssetController] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Update borrow request status (admin only)
    @PatchMapping("/borrow-requests/{requestId}")
    public ResponseEntity<?> updateBorrowRequestStatus(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> request) {
        try {
            System.out.println("🔵 [AssetController] PATCH /assets/borrow-requests/" + requestId);
            
            String status = request.get("status");
            BorrowRequestDto result = borrowService.updateRequestStatus(requestId, status);
            
            System.out.println("✅ [AssetController] Request updated successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ [AssetController] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get user's borrow requests
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyBorrowRequests() {
        try {
            System.out.println("🔵 [AssetController] GET /assets/my-requests");
            
            Long userId = authenticationUtil.getCurrentUserId();
            List<BorrowRequestDto> requests = borrowService.getUserRequests(userId);
            
            System.out.println("✅ [AssetController] Returning " + requests.size() + " requests for user");
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            System.err.println("❌ [AssetController] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
