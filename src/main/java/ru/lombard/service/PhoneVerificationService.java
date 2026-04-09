package ru.lombard.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class PhoneVerificationService {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public SendResult sendCode(Long userId, String phone) {
        String sessionId = UUID.randomUUID().toString();
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        Instant expiresAt = Instant.now().plusSeconds(5 * 60);
        sessions.put(sessionId, new Session(sessionId, userId, phone, code, expiresAt, false, 0));

        // MVP-заглушка: в проде здесь интеграция с SMS-провайдером.
        System.out.println("[SMS-CODE] userId=" + userId + " phone=" + phone + " code=" + code + " session=" + sessionId);

        return new SendResult(sessionId, maskPhone(phone), code);
    }

    public void verifyCode(String sessionId, Long userId, String code) {
        Session session = getActiveSession(sessionId, userId);
        if (session.attempts >= 5) {
            sessions.remove(sessionId);
            throw new ResponseStatusException(BAD_REQUEST, "Превышено число попыток. Отправьте код заново.");
        }
        if (!session.code.equals(code)) {
            sessions.put(sessionId, session.withAttempts(session.attempts + 1));
            throw new ResponseStatusException(BAD_REQUEST, "Неверный код подтверждения.");
        }
        sessions.put(sessionId, session.withVerified(true));
    }

    public void consumeVerified(String sessionId, Long userId) {
        Session session = getActiveSession(sessionId, userId);
        if (!session.verified) {
            throw new ResponseStatusException(BAD_REQUEST, "Сначала подтвердите код из SMS.");
        }
        sessions.remove(sessionId);
    }

    private Session getActiveSession(String sessionId, Long userId) {
        Session session = sessions.get(sessionId);
        if (session == null || !session.userId.equals(userId)) {
            throw new ResponseStatusException(BAD_REQUEST, "Сессия подтверждения не найдена.");
        }
        if (Instant.now().isAfter(session.expiresAt)) {
            sessions.remove(sessionId);
            throw new ResponseStatusException(BAD_REQUEST, "Срок действия кода истек. Отправьте новый код.");
        }
        return session;
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return "";
        String value = phone.trim();
        if (value.length() < 4) return "****";
        return "***" + value.substring(value.length() - 4);
    }

    public record SendResult(String sessionId, String maskedPhone, String debugCode) {}

    private record Session(
            String id,
            Long userId,
            String phone,
            String code,
            Instant expiresAt,
            boolean verified,
            int attempts
    ) {
        Session withAttempts(int nextAttempts) {
            return new Session(id, userId, phone, code, expiresAt, verified, nextAttempts);
        }

        Session withVerified(boolean nextVerified) {
            return new Session(id, userId, phone, code, expiresAt, nextVerified, attempts);
        }
    }
}
