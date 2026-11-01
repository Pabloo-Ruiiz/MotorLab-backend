package com.motorlab.exception;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.motorlab.dto.common.ErrorDetalle;
import com.motorlab.dto.common.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> manejarApiException(ApiException excepcion) {
        return respuesta(excepcion.getEstado(), excepcion.getCodigo(), excepcion.getMessage(), excepcion.getDetalles());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarArgumentoNoValido(MethodArgumentNotValidException excepcion) {
        List<ErrorDetalle> detalles = excepcion.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorDetalle(aSnakeCase(error.getField()), error.getDefaultMessage()))
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "VALIDACION", "Hay campos con errores", detalles);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> manejarViolacionRestriccion(ConstraintViolationException excepcion) {
        List<ErrorDetalle> detalles = excepcion.getConstraintViolations().stream()
                .map(this::convertirViolacion)
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "VALIDACION", "Hay campos con errores", detalles);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> manejarPeticionMalFormada(Exception excepcion) {
        return respuesta(HttpStatus.BAD_REQUEST, "VALIDACION", "La petición no tiene un formato válido", List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> manejarAutenticacion(AuthenticationException excepcion) {
        String mensaje = excepcion instanceof BadCredentialsException
                ? "Credenciales incorrectas"
                : "No se ha podido autenticar la petición";
        return respuesta(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", mensaje, List.of());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(
            org.springframework.security.access.AccessDeniedException excepcion) {
        return respuesta(HttpStatus.FORBIDDEN, "SIN_PERMISO", "No tiene permiso para realizar esta operación",
                List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridad(DataIntegrityViolationException excepcion) {
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO", "La operación entra en conflicto con los datos existentes",
                List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorInterno(Exception excepcion) {
        LOG.error("Error interno no controlado", excepcion);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Error interno del servidor", List.of());
    }

    private ErrorDetalle convertirViolacion(ConstraintViolation<?> violacion) {
        String campo = violacion.getPropertyPath() == null
                ? "general"
                : aSnakeCase(violacion.getPropertyPath().toString());
        return new ErrorDetalle(campo, Objects.requireNonNullElse(violacion.getMessage(), "Valor no válido"));
    }

    private ResponseEntity<ErrorResponse> respuesta(
            HttpStatus estado, String codigo, String mensaje, List<ErrorDetalle> detalles) {
        return ResponseEntity.status(estado).body(ErrorResponse.de(codigo, mensaje, detalles));
    }

    private String aSnakeCase(String valor) {
        String[] segmentos = valor.split("\\.");
        return java.util.Arrays.stream(segmentos)
                .map(this::convertirSegmento)
                .reduce((primero, segundo) -> primero + "." + segundo)
                .orElse(valor.toLowerCase(Locale.ROOT));
    }

    private String convertirSegmento(String segmento) {
        return segmento.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }
}
