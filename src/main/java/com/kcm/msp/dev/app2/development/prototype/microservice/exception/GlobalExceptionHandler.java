package com.kcm.msp.dev.app2.development.prototype.microservice.exception;

import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.Error;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = {ItemNotFoundException.class})
  public final ResponseEntity<Error> handleItemNotFoundException(
      final Exception ex, final WebRequest request) {
    final Error error = new Error();
    error.setCode(HttpStatus.NOT_FOUND.getReasonPhrase());
    error.setMessage(ex.getLocalizedMessage());
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(value = {Exception.class})
  public final ResponseEntity<Error> handleGenericException(
      final Exception ex, final WebRequest request) {
    log.error("Error in the execution api {}", request.getContextPath(), ex);
    final Error error = new Error();
    error.setCode(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    error.setMessage(ex.getLocalizedMessage());
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
