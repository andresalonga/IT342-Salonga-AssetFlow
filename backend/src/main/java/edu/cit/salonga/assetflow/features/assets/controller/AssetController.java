package edu.cit.salonga.assetflow.features.assets.controller;

import edu.cit.salonga.assetflow.features.assets.dto.AssetCreateDto;
import edu.cit.salonga.assetflow.features.assets.dto.AssetDto;
import edu.cit.salonga.assetflow.features.assets.dto.AssetUpdateDto;
import edu.cit.salonga.assetflow.features.assets.service.AssetService;
import edu.cit.salonga.assetflow.features.borrow.dto.BorrowRequestCreateDto;
import edu.cit.salonga.assetflow.features.borrow.dto.BorrowRequestDto;
import edu.cit.salonga.assetflow.features.borrow.service.BorrowService;
import edu.cit.salonga.assetflow.util.AuthenticationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*")
public class AssetController {

    private static final long MAX_UPLOAD_BYTES = 1_048_576; // 1MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg");

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private AuthenticationUtil authenticationUtil;

    @Value("${assetflow.upload-dir:uploads/assets}")
    private String uploadDir;

    // List all assets
    @GetMapping
    public ResponseEntity<?> getAllAssets() {
        try {
            System.out.println("🔵 [AssetController] GET /assets");
            List<AssetDto> assets = assetService.getAll();
            System.out.println("✅ [AssetController] Returning " + assets.size() + " assets");
            return ResponseEntity.ok(assets);
        } catch (Exception e) {
            System.err.println("❌ [AssetController] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get asset by id
    @GetMapping("/{assetId}")
    public ResponseEntity<?> getAssetById(@PathVariable Long assetId) {
        try {
            System.out.println("🔵 [AssetController] GET /assets/" + assetId);
            AssetDto asset = assetService.getById(assetId);
            return ResponseEntity.ok(asset);
        } catch (Exception e) {
            System.err.println("❌ [AssetController] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Create asset
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createAsset(@RequestBody AssetCreateDto request) {
        try {
            System.out.println("🔵 [AssetController] POST /assets");
            AssetDto asset = assetService.create(request);
            System.out.println("✅ [AssetController] Asset created successfully");
            return ResponseEntity.ok(asset);
        } catch (Exception e) {
            System.err.println("❌ [AssetController] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Update asset
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{assetId}")
    public ResponseEntity<?> updateAsset(@PathVariable Long assetId, @RequestBody AssetUpdateDto request) {
        try {
            System.out.println("🔵 [AssetController] PUT /assets/" + assetId);
            AssetDto asset = assetService.update(assetId, request);
            System.out.println("✅ [AssetController] Asset updated successfully");
            return ResponseEntity.ok(asset);
        } catch (Exception e) {
            System.err.println("❌ [AssetController] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Delete asset
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{assetId}")
    public ResponseEntity<?> deleteAsset(@PathVariable Long assetId) {
        try {
            System.out.println("🔵 [AssetController] DELETE /assets/" + assetId);
            assetService.delete(assetId);
            System.out.println("✅ [AssetController] Asset deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("❌ [AssetController] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Upload asset image (PNG/JPG only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadAssetImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is required"));
            }

            if (file.getSize() > MAX_UPLOAD_BYTES) {
                return ResponseEntity.badRequest().body(Map.of("error", "File exceeds 1MB limit"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only PNG and JPG files are allowed"));
            }

            String extension = contentType.equals("image/png") ? ".png" : ".jpg";
            String filename = UUID.randomUUID() + extension;

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path destination = uploadPath.resolve(filename).normalize();
            file.transferTo(destination);

            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(filename)
                    .toUriString();

            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to save file"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Submit borrow request for an asset
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/borrow-requests/{requestId}")
    public ResponseEntity<?> updateBorrowRequestStatus(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> request) {
        try {
            System.out.println("🔵 [AssetController] PATCH /assets/borrow-requests/" + requestId);
            
            String status = request.get("status");
            String note = request.get("note");
            BorrowRequestDto result = borrowService.updateRequestStatus(requestId, status, note);
            
            System.out.println("✅ [AssetController] Request updated successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ [AssetController] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get user's borrow requests
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
