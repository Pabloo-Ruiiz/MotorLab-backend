package com.motorlab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sirve la aplicación Angular al entrar directamente en una de sus rutas o al recargar la página:
 * esas rutas solo existen en el navegador, así que siempre se responde con index.html.
 */
@Controller
public class SpaForwardController {

    @GetMapping({ "/cliente/**", "/taller/**", "/login", "/registro", "/verificar-correo", "/403", "/404" })
    public String reenviarAIndex() {
        return "forward:/index.html";
    }
}
