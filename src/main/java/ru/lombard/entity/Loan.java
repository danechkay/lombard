package ru.lombard.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(name = "contract_number", nullable = false, length = 64)
    private String contractNumber;

    @Column(name = "loan_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "repay_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal repayAmount;

    @Column(name = "interest_rate_daily", nullable = false, precision = 10, scale = 4)
    private BigDecimal interestRateDaily;

    @Column(name = "collateral_description", nullable = false, length = 1000)
    private String collateralDescription;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "status", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LoanStatus status = LoanStatus.ACTIVE;

    @Column(name = "payment_allowed", nullable = false)
    @Builder.Default
    private boolean paymentAllowed = false;

    @Column(name = "notes", length = 2000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum LoanStatus {
        ACTIVE, CLOSED, OVERDUE
    }
}
