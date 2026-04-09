package ru.lombard.controller.api.admin;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.lombard.dto.LoanDto;
import ru.lombard.entity.Loan;
import ru.lombard.entity.User;
import ru.lombard.repository.UserRepository;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.LoanService;
import ru.lombard.service.PhoneVerificationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/admin/loans")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AdminLoanApiController {

    private final LoanService loanService;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PhoneVerificationService phoneVerificationService;

    @GetMapping("/users")
    public List<UserOption> users(@RequestParam(required = false) String q) {
        String query = normalize(q);
        return userRepository.findAllByOrderByIdAsc().stream()
                .filter(u -> u.getRole() == User.Role.USER)
                .filter(u -> query == null
                        || containsIgnoreCase(u.getFullName(), query)
                        || containsIgnoreCase(u.getEmail(), query)
                        || containsIgnoreCase(u.getPhone(), query))
                .map(u -> new UserOption(u.getId(), u.getFullName(), u.getEmail()))
                .limit(25)
                .toList();
    }

    @PostMapping
    public LoanDto create(@RequestBody CreateLoanRequest request) {
        if (request.getUserId() == null) throw new ResponseStatusException(BAD_REQUEST, "Выберите пользователя");
        String verifySessionId = require(request.getVerifySessionId(), "Нужно подтверждение по SMS");
        phoneVerificationService.consumeVerified(verifySessionId, request.getUserId());

        Loan loan = Loan.builder()
                .contractNumber(require(request.getContractNumber(), "Укажите номер договора"))
                .loanAmount(requireMoney(request.getLoanAmount(), "Укажите сумму займа"))
                .repayAmount(requireMoney(request.getRepayAmount(), "Укажите сумму к погашению"))
                .interestRateDaily(requireMoney(request.getInterestRateDaily(), "Укажите ставку"))
                .collateralDescription(require(request.getCollateralDescription(), "Укажите предмет залога"))
                .issueDate(requireDate(request.getIssueDate(), "Укажите дату выдачи"))
                .dueDate(requireDate(request.getDueDate(), "Укажите дату окончания"))
                .status(request.getStatus() == null ? Loan.LoanStatus.ACTIVE : request.getStatus())
                .paymentAllowed(request.isPaymentAllowed())
                .notes(blankToNull(request.getNotes()))
                .build();
        Long managerId = currentUserService.getCurrentUserIdOrNull();
        return loanService.createLoan(loan, request.getUserId(), managerId);
    }

    @PostMapping("/send-code")
    public SendCodeResponse sendCode(@RequestBody SendCodeRequest request) {
        if (request.getUserId() == null) throw new ResponseStatusException(BAD_REQUEST, "Выберите пользователя");
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Пользователь не найден"));
        String phone = blankToNull(user.getPhone());
        if (phone == null) {
            throw new ResponseStatusException(BAD_REQUEST, "У пользователя не указан номер телефона");
        }
        PhoneVerificationService.SendResult result = phoneVerificationService.sendCode(user.getId(), phone);
        return new SendCodeResponse(result.sessionId(), result.maskedPhone(), result.debugCode());
    }

    @PostMapping("/verify-code")
    public MessageResponse verifyCode(@RequestBody VerifyCodeRequest request) {
        if (request.getUserId() == null) throw new ResponseStatusException(BAD_REQUEST, "Выберите пользователя");
        phoneVerificationService.verifyCode(
                require(request.getSessionId(), "Сначала отправьте код"),
                request.getUserId(),
                require(request.getCode(), "Введите код")
        );
        return new MessageResponse("Код подтвержден");
    }

    public record UserOption(Long id, String fullName, String email) {}

    @Data
    public static class CreateLoanRequest {
        private Long userId;
        private String verifySessionId;
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
    }

    @Data
    public static class SendCodeRequest {
        private Long userId;
    }

    @Data
    public static class VerifyCodeRequest {
        private Long userId;
        private String sessionId;
        private String code;
    }

    public record SendCodeResponse(String sessionId, String maskedPhone, String debugCode) {}

    public record MessageResponse(String message) {}

    private static String require(String value, String message) {
        String normalized = blankToNull(value);
        if (normalized == null) throw new ResponseStatusException(BAD_REQUEST, message);
        return normalized;
    }

    private static BigDecimal requireMoney(BigDecimal value, String message) {
        if (value == null || value.signum() <= 0) throw new ResponseStatusException(BAD_REQUEST, message);
        return value;
    }

    private static LocalDate requireDate(LocalDate value, String message) {
        if (value == null) throw new ResponseStatusException(BAD_REQUEST, message);
        return value;
    }

    private static String normalize(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private static String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean containsIgnoreCase(String value, String query) {
        if (value == null || query == null) return false;
        return value.toLowerCase().contains(query);
    }
}
