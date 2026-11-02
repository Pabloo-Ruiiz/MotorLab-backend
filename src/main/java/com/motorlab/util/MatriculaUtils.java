package com.motorlab.util;

/**
 * Utilitats per a la normalització de matrícules espanyoles.
 */
public final class MatriculaUtils {

    private MatriculaUtils() {
    }

    /**
     * Normalitza una matrícula: elimina espais i guions i la passa a majúscules.
     *
     * @param matricula la matrícula tal com s'ha rebut, pot ser nul·la
     * @return la matrícula normalitzada o {@code null} si l'entrada era nul·la
     */
    public static String normalizar(String matricula) {
        if (matricula == null) {
            return null;
        }
        return matricula.trim()
                .replace(" ", "")
                .replace("-", "")
                .toUpperCase();
    }
}
