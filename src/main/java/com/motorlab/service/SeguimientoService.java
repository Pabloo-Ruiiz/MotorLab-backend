package com.motorlab.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.entity.EventoSeguimiento;
import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.repository.EventoSeguimientoRepository;

@Service
public class SeguimientoService {

    private final EventoSeguimientoRepository eventoSeguimientoRepository;

    public SeguimientoService(EventoSeguimientoRepository eventoSeguimientoRepository) {
        this.eventoSeguimientoRepository = eventoSeguimientoRepository;
    }

    @Transactional
    public EventoSeguimiento registrar(
            OrdenTrabajo orden, EstadoOrden estado, String descripcion, Usuario autor) {
        EventoSeguimiento evento = new EventoSeguimiento();
        evento.setOrden(orden);
        evento.setEstado(estado);
        evento.setDescripcion(descripcion);
        evento.setFechaHora(LocalDateTime.now());
        evento.setUsuario(autor);
        return eventoSeguimientoRepository.save(evento);
    }

    @Transactional(readOnly = true)
    public List<EventoSeguimiento> listar(Integer ordenId) {
        return eventoSeguimientoRepository.findByOrdenIdOrderByFechaHoraAscIdAsc(ordenId);
    }
}
