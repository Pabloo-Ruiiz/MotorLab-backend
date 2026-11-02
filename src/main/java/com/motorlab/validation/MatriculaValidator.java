package com.motorlab.validation;

import java.util.regex.Pattern;

import com.motorlab.util.MatriculaUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador de {@link MatriculaEspanola}.
 * <p>Normalitza la matrícula amb {@link MatriculaUtils} i comprova que compleix el format
 * oficial: quatre xifres seguides de tres consonants (vocals i la Ñ excloses).</p>
 */
public class MatriculaValidator implements ConstraintValidator<MatriculaEspanola, String> {

    /** Quatre xifres + tres consonants permeses (vocals i la Ñ excloses). */
    private static final Pattern FORMATO = Pattern.compile("^[0-9]{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null) {
            return true;
        }
        return FORMATO.matcher(MatriculaUtils.normalizar(valor)).matches();
    }
}
