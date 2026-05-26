package edu.cit.salonga.assetflow.features.borrow.dto;

import java.time.LocalDate;

public class BorrowRequestDto {
    public String id;
    public String userId;
    public String userName;
    public String userEmail;
    public String assetId;
    public String assetName;
    public String requestDate;
    public String dueDate;
    public String status;
    public String requestDateTime;
    public String statusUpdatedAt;
    public String rejectionNote;

    public BorrowRequestDto() {}

    public BorrowRequestDto(Long id, Long userId, String userName, String userEmail, Long assetId, String assetName, LocalDate requestDate, LocalDate dueDate, String status, String requestDateTime, String statusUpdatedAt, String rejectionNote) {
        this.id = String.valueOf(id);
        this.userId = String.valueOf(userId);
        this.userName = userName;
        this.userEmail = userEmail;
        this.assetId = String.valueOf(assetId);
        this.assetName = assetName;
        this.requestDate = requestDate != null ? requestDate.toString() : null;
        this.dueDate = dueDate != null ? dueDate.toString() : null;
        this.status = status;
        this.requestDateTime = requestDateTime;
        this.statusUpdatedAt = statusUpdatedAt;
        this.rejectionNote = rejectionNote;
    }
}
