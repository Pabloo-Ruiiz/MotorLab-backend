package com.motorlab.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.motorlab.support.IntegracionBase;

@AutoConfigureMockMvc
@TestPropertySource(properties = "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-caracteres")
class PresupuestoDefinitivoControllerTest extends IntegracionBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void consultarYDescargarExigenIniciarSesion() throws Exception {
        mockMvc.perform(get("/api/v1/ordenes/1/presupuesto-definitivo"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/ordenes/1/presupuesto-definitivo/pdf"))
                .andExpect(status().isUnauthorized());
    }
}
