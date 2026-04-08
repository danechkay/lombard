package ru.lombard.dto;

import lombok.Builder;
import lombok.Data;
import ru.lombard.entity.Loan;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LoanDto {
    private Long id;
    private Long userId;
    private String userFullName;
    private String contractNumber;
    private BigDecimal loanAmount;
    private BigDecimal repayAmount;
    private BigDecimal interestRateDaily;
    private String collateralDescription;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private Loan.LoanStatus status;
    private boolean paymentAllowed;
    private String notes;
    private String managerName;
}
