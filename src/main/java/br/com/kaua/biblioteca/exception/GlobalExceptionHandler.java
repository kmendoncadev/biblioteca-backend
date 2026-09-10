package br.com.kaua.biblioteca.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(RecursoNaoEncontradoException exception) {

		ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage(),
				LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException exception) {

		ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), exception.getMessage(),
				LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {

		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Dados inválidos.",
				LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

	}
}
