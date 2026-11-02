package com.motorlab.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Valida que una cadena tingui el format d'una matrícula espanyola vàlida.
 *
 * <p>El valor es normalitza amb {@link com.motorlab.util.MatriculaUtils} abans de validar-lo,
 * de manera que espais i guions són permesos. Un valor nul es considera vàlid: l'obligatorietat
 * es controla per separat amb {@code @NotBlank}.</p>
 */
@Documented
@Constraint(validatedBy = MatriculaValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface MatriculaEspanola {

    String message() default "Formato de matrícula no válido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
