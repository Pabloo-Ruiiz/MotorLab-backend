package com.motorlab.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorlab.dto.auth.LoginRequest;
import com.motorlab.dto.auth.RegistroRequest;
import com.motorlab.support.IntegracionBase;

@AutoConfigureMockMvc
@TestPropertySource(properties = "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-caracteres")
class AuthControllerTest extends IntegracionBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registroCorrectoDevuelve201() throws Exception {
        RegistroRequest solicitud = registro();

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(solicitud.email()));
    }

    @Test
    void registroConEmailRepetidoDevuelve409() throws Exception {
        RegistroRequest solicitud = registro();
        registrar(solicitud);

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(solicitud)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.codigo").value("EMAIL_DUPLICADO"));
    }

    @Test
    void registroSinPrivacidadDevuelve400ConDetalles() throws Exception {
        RegistroRequest solicitud = new RegistroRequest(
                "Usuario Prueba",
                emailUnico(),
                "600123456",
                "ClaveSegura123",
                false);

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.error.detalles", hasSize(1)))
                .andExpect(jsonPath("$.error.detalles[0].campo").value("acepta_privacidad"));
    }

    @Test
    void loginCorrectoDevuelveToken() throws Exception {
        RegistroRequest solicitud = registro();
        registrar(solicitud);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new LoginRequest(solicitud.email(), solicitud.password()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.usuario.email").value(solicitud.email()));
    }

    @Test
    void loginConPasswordErroneaDevuelve401() throws Exception {
        RegistroRequest solicitud = registro();
        registrar(solicitud);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new LoginRequest(solicitud.email(), "ClaveIncorrecta123"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void yoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/yo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void yoConTokenDevuelve200() throws Exception {
        RegistroRequest solicitud = registro();
        registrar(solicitud);
        String token = tokenDe(solicitud);

        mockMvc.perform(get("/api/v1/auth/yo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(solicitud.email()));
    }

    private RegistroRequest registro() {
        return new RegistroRequest(
                "Usuario Prueba",
                emailUnico(),
                "600123456",
                "ClaveSegura123",
                true);
    }

    private String emailUnico() {
        return "pruebas+" + UUID.randomUUID() + "@example.com";
    }

    private void registrar(RegistroRequest solicitud) throws Exception {
        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(solicitud)))
                .andExpect(status().isCreated());
    }

    private String tokenDe(RegistroRequest solicitud) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new LoginRequest(solicitud.email(), solicitud.password()))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode respuesta = objectMapper.readTree(resultado.getResponse().getContentAsByteArray());
        return respuesta.get("token").asText();
    }
}
