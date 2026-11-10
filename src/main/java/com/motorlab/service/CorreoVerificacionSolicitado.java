package com.motorlab.service;

// Se publica dentro de la transacción y el correo sale solo si esta llega a confirmarse
public record CorreoVerificacionSolicitado(String email, String nombre, String token) {
}
