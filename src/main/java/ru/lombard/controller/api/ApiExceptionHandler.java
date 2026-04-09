package ru.lombard.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice(basePackages = "ru.lombard.controller.api")
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalState(IllegalStateException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound() {
        return Map.of("message", "Сущность не найдена");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleOtherExceptions(Exception ex, WebRequest request) {
        // Возвращаем детали, чтобы можно было понять первопричину при отладке.
        // (Если сообщения пустые — покажем тип и сообщение первопричины.)
        String msg = ex.getMessage();
        String rootMsg = null;
        Throwable t = ex;
        while (t != null) {
            if (t.getMessage() != null && !t.getMessage().isBlank()) {
                rootMsg = t.getMessage();
                break;
            }
            t = t.getCause();
        }
        if (msg == null || msg.isBlank()) msg = (ex.getClass().getName() + (rootMsg != null ? (": " + rootMsg) : ""));
        return Map.of("message", msg);
    }
}
