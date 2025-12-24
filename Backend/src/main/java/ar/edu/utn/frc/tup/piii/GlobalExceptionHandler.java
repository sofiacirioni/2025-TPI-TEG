package ar.edu.utn.frc.tup.piii;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> manejarIllegalArgument(IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                Map.of(
                                                "mensaje", ex.getMessage(),
                                                "timestamp", generarTimestamp()));
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
                String mensaje = ex.getConstraintViolations()
                                .stream()
                                .map(ConstraintViolation::getMessage)
                                .findFirst()
                                .orElse("Violación de restricción");

                return ResponseEntity
                                .badRequest()
                                .body(Map.of(
                                                "mensaje", mensaje,
                                                "timestamp", generarTimestamp()));
        }

        private int[] generarTimestamp() {
                LocalDateTime ahora = LocalDateTime.now();
                return new int[] {
                                ahora.getYear(),
                                ahora.getMonthValue(),
                                ahora.getDayOfMonth(),
                                ahora.getHour(),
                                ahora.getMinute(),
                                ahora.getSecond(),
                                ahora.getNano()
                };
        }

}
