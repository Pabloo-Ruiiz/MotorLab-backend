package com.motorlab.dto.pieza;

import java.math.BigDecimal;

public record PiezaResponse(
        Integer id,
        String referencia,
        String nombre,
        BigDecimal precio,
        int stock,
        boolean activa,
        boolean stockBajo) {
}
