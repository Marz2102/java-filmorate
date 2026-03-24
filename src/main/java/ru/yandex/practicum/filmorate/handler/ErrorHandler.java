package ru.yandex.practicum.filmorate.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class, ResponseStatusException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final Exception e) {
        String message = switch (e) {
            case ValidationException err -> err.getMessage();
            case MethodArgumentNotValidException err -> err.getBindingResult().getAllErrors().stream()
                    .findFirst()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .orElse("Ошибка валидации полей");
            case ConstraintViolationException err -> err.getConstraintViolations().stream()
                    .findFirst()
                    .map(ConstraintViolation::getMessage)
                    .orElse("Ошибка валидации параметров");
            case ResponseStatusException err -> err.getReason();
            default -> "Неверные данные запроса";
        };

        return new ErrorResponse("Ошибка валидации данных", message);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        return new ErrorResponse("Ресурс не найден", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ErrorResponse handleUnsupportedMediaTypeException(final HttpMediaTypeNotSupportedException e) {
        return new ErrorResponse("Неподдерживаемый тип тела запроса", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerException(final Exception e) {
        StringBuilder error = new StringBuilder();

        if (e instanceof ResponseStatusException) {
            error.append("Ошибка сохранения данных в базу");
        } else {
            error.append("Ошибка сервера");
        }

        return new ErrorResponse(error.toString(), e.getMessage());
    }
}