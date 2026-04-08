package ru.lombard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lombard.dto.LoanDto;
import ru.lombard.entity.Loan;
import ru.lombard.entity.User;
import ru.lombard.repository.LoanRepository;
import ru.lombard.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    public List<LoanDto> findByUser(Long userId) {
        return loanRepository.findByUserIdOrderByIssueDateDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public LoanDto createLoan(Loan loan, Long userId, Long managerId) {
        User user = userRepository.findById(userId).orElseThrow();
        loan.setUser(user);
        if (managerId != null) {
            User manager = userRepository.findById(managerId).orElse(null);
            loan.setManager(manager);
        }
        return toDto(loanRepository.save(loan));
    }

    public LoanDto toDto(Loan loan) {
        return LoanDto.builder()
                .id(loan.getId())
                .userId(loan.getUser() != null ? loan.getUser().getId() : null)
                .userFullName(loan.getUser() != null ? loan.getUser().getFullName() : null)
                .contractNumber(loan.getContractNumber())
                .loanAmount(loan.getLoanAmount())
                .repayAmount(loan.getRepayAmount())
                .interestRateDaily(loan.getInterestRateDaily())
                .collateralDescription(loan.getCollateralDescription())
                .issueDate(loan.getIssueDate())
                .dueDate(loan.getDueDate())
                .status(loan.getStatus())
                .paymentAllowed(loan.isPaymentAllowed())
                .notes(loan.getNotes())
                .managerName(loan.getManager() != null ? loan.getManager().getFullName() : null)
                .build();
    }
}
