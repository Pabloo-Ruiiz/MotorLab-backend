package com.motorlab.entity.enums;

public enum EstadoPago {
    // Las constantes en minúscula coinciden intencionadamente con los valores CHECK de PostgreSQL.
    // Sin valor (null): el cliente todavía no ha elegido cómo pagar.
    pendiente_efectivo, pagado_tarjeta, pagado_efectivo
}
