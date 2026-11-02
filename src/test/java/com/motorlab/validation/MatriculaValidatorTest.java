package com.motorlab.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MatriculaValidatorTest {

    private final MatriculaValidator validator = new MatriculaValidator();

    @Test
    void debeAceptarMatriculasConEspaciosYGueiones() {
        assertTrue(validator.isValid("4821 KLB", null));
        assertTrue(validator.isValid("4821-klb", null));
    }

    @Test
    void debeAceptarMatriculaCompacta() {
        assertTrue(validator.isValid("1234BCD", null));
    }

    @Test
    void debeRechazarVocalesEnLaParteLetras() {
        assertFalse(validator.isValid("4821 AEI", null));
    }

    @Test
    void debeRechazarFormatosIncorrectos() {
        assertFalse(validator.isValid("123 ABC", null));
        assertFalse(validator.isValid("4821KL", null));
        assertFalse(validator.isValid("4821 KLQ", null));
    }

    @Test
    void debeRechazarCadenaVacia() {
        assertFalse(validator.isValid("", null));
    }

    @Test
    void nullEsValidoLaObligatoriedadSeControlaConNotBlank() {
        assertTrue(validator.isValid(null, null));
    }
}
