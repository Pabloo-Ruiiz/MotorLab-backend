package com.motorlab.entity.enums;

public enum EstadoOrden {
    // Las constantes en minúscula coinciden intencionadamente con los valores CHECK de PostgreSQL.
    solicitud_recibida,
    pendiente_presupuesto,
    presupuesto_enviado,
    presupuesto_autorizado,
    recibido,
    en_preparacion,
    esperando_pieza,
    arreglando,
    listo,
    entregado,
    rechazada,
    cancelada;

    public int progreso() {
        return switch (this) {
            case recibido -> 20;
            case en_preparacion -> 40;
            case esperando_pieza -> 60;
            case arreglando -> 80;
            case listo, entregado -> 100;
            default -> 0;
        };
    }
}
