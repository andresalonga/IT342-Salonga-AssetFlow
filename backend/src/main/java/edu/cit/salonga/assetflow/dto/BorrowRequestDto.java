package edu.cit.salonga.assetflow.dto;

import java.time.LocalDate;

public class BorrowRequestDto {
    public String id;
    public String userId;
    public String userName;
    public String assetId;
    public String assetName;
    public String requestDate;
    public String dueDate;
    public String status;

    public BorrowRequestDto() {}

    public BorrowRequestDto(Long id, Long userId, String userName, Long assetId, String assetName, LocalDate requestDate, LocalDate dueDate, String status) {
        this.id = String.valueOf(id);
        this.userId = String.valueOf(userId);
        this.userName = userName;
        this.assetId = String.valueOf(assetId);
        this.assetName = assetName;
        this.requestDate = requestDate != null ? requestDate.toString() : null;
        this.dueDate = dueDate != null ? dueDate.toString() : null;
        this.status = status;
    }
}
