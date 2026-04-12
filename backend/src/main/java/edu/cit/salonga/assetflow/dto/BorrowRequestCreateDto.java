package edu.cit.salonga.assetflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class BorrowRequestCreateDto {
    @JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate dueDate;

    public BorrowRequestCreateDto() {}

    public BorrowRequestCreateDto(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
