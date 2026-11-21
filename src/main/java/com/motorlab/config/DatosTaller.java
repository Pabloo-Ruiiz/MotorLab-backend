package com.motorlab.config;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Datos del taller que aparecen en los documentos. Solo se muestran los que estén configurados
@Component
public class DatosTaller {

    private final String nombre;
    private final String direccion;
    private final String telefono;
    private final String email;
    private final String cif;

    public DatosTaller(
            @Value("${app.taller.nombre:Taller MotorLab}") String nombre,
            @Value("${app.taller.direccion:}") String direccion,
            @Value("${app.taller.telefono:}") String telefono,
            @Value("${app.taller.email:}") String email,
            @Value("${app.taller.cif:}") String cif) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.cif = cif;
    }

    // «Taller MotorLab · Calle Mayor 1 · 600 000 000 · CIF B00000000», sin los datos vacíos
    public String resumen() {
        return Stream.of(nombre, direccion, telefono, email, cif.isBlank() ? "" : "CIF " + cif)
                .filter(dato -> dato != null && !dato.isBlank())
                .collect(Collectors.joining(" · "));
    }

    public String nombre() {
        return nombre;
    }

    public String direccion() {
        return vacioANulo(direccion);
    }

    public String telefono() {
        return vacioANulo(telefono);
    }

    public String email() {
        return vacioANulo(email);
    }

    public String cif() {
        return vacioANulo(cif);
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }
}
