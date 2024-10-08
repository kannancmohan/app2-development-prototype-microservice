package com.kcm.msp.dev.app2.development.prototype.microservice.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.Error;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(ItemNotFoundException.class)
  @ResponseBody
  public final ResponseEntity<Error> handleItemNotFoundException(
      final ItemNotFoundException ex, final WebRequest request) {
    log.debug("Item not found {}", request.getContextPath(), ex);
    final Error error = new Error();
    error.setCode(NOT_FOUND.getReasonPhrase());
    error.setMessage(ex.getLocalizedMessage());
    return new ResponseEntity<>(error, NOT_FOUND);
  }

  @ExceptionHandler(value = {AccessDeniedException.class})
  public final ResponseEntity<Error> handleAccessDeniedException(
      final Exception ex, final WebRequest request) {
    log.warn("Access denied in execution api {}", request.getContextPath(), ex);
    final Error error = new Error();
    error.setCode(UNAUTHORIZED.getReasonPhrase());
    error.setMessage(
        "Access Denied: You do not have necessary permissions to access this resource");
    return new ResponseEntity<>(error, UNAUTHORIZED);
  }

  @ExceptionHandler(value = {Exception.class})
  public final ResponseEntity<Error> handleGenericException(
      final Exception ex, final WebRequest request) {
    log.error("Error in the execution api {}", request.getContextPath(), ex);
    final Error error = new Error();
    error.setCode(INTERNAL_SERVER_ERROR.getReasonPhrase());
    error.setMessage(ex.getLocalizedMessage());
    return new ResponseEntity<>(error, INTERNAL_SERVER_ERROR);
  }
}
