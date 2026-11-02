package com.motorlab.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorlab.dto.auth.LoginRequest;
import com.motorlab.dto.orden.OrdenCreateRequest;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.Vehiculo;
import com.motorlab.entity.enums.ConduccionSegura;
import com.motorlab.entity.enums.ContactoPreferido;
import com.motorlab.entity.enums.FranjaPreferida;
import com.motorlab.entity.enums.Rol;
import com.motorlab.entity.enums.TipoServicio;
import com.motorlab.entity.enums.Urgencia;
import com.motorlab.repository.UsuarioRepository;
import com.motorlab.repository.VehiculoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-caracteres")
@Transactional
class OrdenControllerTest {

    private static final String PASSWORD = "ClaveSegura123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    private Usuario clienteA;
    private Usuario clienteB;
    private Usuario mecanico;
    private Usuario administrador;
    private Vehiculo vehiculoClienteA;
    private Vehiculo vehiculoClienteB;

    @BeforeEach
    void prepararDatos() {
        clienteA = guardarUsuario("Cliente A", Rol.cliente);
        clienteB = guardarUsuario("Cliente B", Rol.cliente);
        mecanico = guardarUsuario("Mecánico", Rol.mecanico);
        administrador = guardarUsuario("Administrador", Rol.admin);
        vehiculoClienteA = guardarVehiculo(clienteA, "1234BCF");
        vehiculoClienteB = guardarVehiculo(clienteB, "5678DGH");
    }

    @Test
    void clienteCreaFrenosYLaUrgenciaQuedaEnAlta() throws Exception {
        OrdenCreateRequest solicitud = solicitud(vehiculoClienteA, TipoServicio.frenos,
                "Hace un ruido extraño al frenar", LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/v1/ordenes")
                        .header("Authorization", "Bearer " + tokenDe(clienteA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.urgencia").value("alta"));
    }

    @Test
    void solicitudConDatosInvalidosDevuelve400ConDetalles() throws Exception {
        OrdenCreateRequest solicitud = solicitud(vehiculoClienteA, TipoServicio.motor,
                "Síntoma corto", LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/v1/ordenes")
                        .header("Authorization", "Bearer " + tokenDe(clienteA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.error.detalles", hasSize(2)))
                .andExpect(jsonPath("$.error.detalles[*].campo", hasItem("sintoma")))
                .andExpect(jsonPath("$.error.detalles[*].campo", hasItem("fecha_preferida")));
    }

    @Test
    void clienteNoPuedeUsarVehiculoDeOtroCliente() throws Exception {
        OrdenCreateRequest solicitud = solicitud(vehiculoClienteB, TipoServicio.motor,
                "El motor vibra al arrancar y pierde potencia", LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/v1/ordenes")
                        .header("Authorization", "Bearer " + tokenDe(clienteA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(solicitud)))
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteBNoPuedePedirOrdenDeClienteA() throws Exception {
        int ordenId = crearOrden(vehiculoClienteA, clienteA);

        mockMvc.perform(get("/api/v1/ordenes/{id}", ordenId)
                        .header("Authorization", "Bearer " + tokenDe(clienteB)))
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteSoloVeSusOrdenes() throws Exception {
        crearOrden(vehiculoClienteA, clienteA);
        crearOrden(vehiculoClienteB, clienteB);

        mockMvc.perform(get("/api/v1/ordenes")
                        .header("Authorization", "Bearer " + tokenDe(clienteA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos", hasSize(1)))
                .andExpect(jsonPath("$.datos[0].cliente_nombre").value("Cliente A"));
    }

    @Test
    void mecanicoNoAsignadoNoPuedePedirLaOrden() throws Exception {
        int ordenId = crearOrden(vehiculoClienteA, clienteA);

        mockMvc.perform(get("/api/v1/ordenes/{id}", ordenId)
                        .header("Authorization", "Bearer " + tokenDe(mecanico)))
                .andExpect(status().isNotFound());
    }

    private Usuario guardarUsuario(String nombre, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(nombre.toLowerCase().replace(" ", ".") + "." + UUID.randomUUID() + "@example.com");
        usuario.setTelefono("600000000");
        usuario.setPasswordHash(passwordEncoder.encode(PASSWORD));
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private Vehiculo guardarVehiculo(Usuario usuario, String matricula) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setUsuario(usuario);
        vehiculo.setMatricula(matricula);
        vehiculo.setMarca("Seat");
        vehiculo.setModelo("Leon");
        vehiculo.setVersion("1.5 TSI");
        vehiculo.setAnio(2019);
        vehiculo.setKilometros(50000);
        vehiculo.setActivo(true);
        return vehiculoRepository.save(vehiculo);
    }

    private OrdenCreateRequest solicitud(
            Vehiculo vehiculo,
            TipoServicio tipoServicio,
            String sintoma,
            LocalDate fechaPreferida) {
        return new OrdenCreateRequest(
                vehiculo.getId(),
                tipoServicio,
                sintoma,
                null,
                ConduccionSegura.si,
                Urgencia.media,
                fechaPreferida,
                FranjaPreferida.manana,
                ContactoPreferido.email,
                true);
    }

    private int crearOrden(Vehiculo vehiculo, Usuario usuario) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/ordenes")
                        .header("Authorization", "Bearer " + tokenDe(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                solicitud(vehiculo, TipoServicio.motor,
                                        "El motor vibra al arrancar y pierde potencia",
                                        LocalDate.now().plusDays(1)))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode respuesta = objectMapper.readTree(resultado.getResponse().getContentAsByteArray());
        return respuesta.get("id").asInt();
    }

    private String tokenDe(Usuario usuario) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new LoginRequest(usuario.getEmail(), PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode respuesta = objectMapper.readTree(resultado.getResponse().getContentAsByteArray());
        return respuesta.get("token").asText();
    }
}
