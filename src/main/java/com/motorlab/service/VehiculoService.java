package com.motorlab.service;

import java.time.Year;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.dto.common.PageResponse;
import com.motorlab.dto.vehiculo.VehiculoRequest;
import com.motorlab.dto.vehiculo.VehiculoResponse;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.Vehiculo;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.exception.ConflictoException;
import com.motorlab.exception.MatriculaDuplicadaException;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.exception.SolicitudInvalidaException;
import com.motorlab.mapper.VehiculoMapper;
import com.motorlab.repository.OrdenTrabajoRepository;
import com.motorlab.repository.UsuarioRepository;
import com.motorlab.repository.VehiculoRepository;
import com.motorlab.security.SecurityUtils;
import com.motorlab.util.MatriculaUtils;

@Service
public class VehiculoService {

    private static final List<EstadoOrden> ESTADOS_CERRADOS = List.of(
            EstadoOrden.entregado,
            EstadoOrden.rechazada,
            EstadoOrden.cancelada);

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final VehiculoMapper vehiculoMapper;

    public VehiculoService(
            VehiculoRepository vehiculoRepository,
            UsuarioRepository usuarioRepository,
            OrdenTrabajoRepository ordenTrabajoRepository,
            VehiculoMapper vehiculoMapper) {
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.vehiculoMapper = vehiculoMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<VehiculoResponse> listar(Integer pagina, Integer limite) {
        Pageable pageable = PageResponse.pageable(pagina, limite);
        Page<Vehiculo> vehiculos = SecurityUtils.esAdmin()
                ? vehiculoRepository.findAll(pageable)
                : vehiculoRepository.findByUsuarioIdAndActivoTrue(SecurityUtils.idActual(), pageable);
        return PageResponse.de(vehiculos.map(vehiculoMapper::aResponse));
    }

    @Transactional
    public VehiculoResponse crear(VehiculoRequest solicitud) {
        validarAnio(solicitud.anio());
        String matricula = MatriculaUtils.normalizar(solicitud.matricula());
        if (vehiculoRepository.existsByMatricula(matricula)) {
            throw new MatriculaDuplicadaException();
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setUsuario(resolverPropietario(solicitud.usuarioId()));
        vehiculo.setMatricula(matricula);
        copiarDatos(solicitud, vehiculo);
        return vehiculoMapper.aResponse(vehiculoRepository.save(vehiculo));
    }

    @Transactional(readOnly = true)
    public VehiculoResponse obtener(Integer id) {
        return vehiculoMapper.aResponse(obtenerVisible(id));
    }

    @Transactional
    public VehiculoResponse actualizar(Integer id, VehiculoRequest solicitud) {
        Vehiculo vehiculo = obtenerVisible(id);
        String matricula = MatriculaUtils.normalizar(solicitud.matricula());
        if (!matricula.equals(vehiculo.getMatricula())) {
            throw new SolicitudInvalidaException("La matrícula no se puede modificar");
        }
        validarAnio(solicitud.anio());
        copiarDatos(solicitud, vehiculo);
        return vehiculoMapper.aResponse(vehiculo);
    }

    @Transactional
    public void darDeBaja(Integer id) {
        Vehiculo vehiculo = obtenerVisible(id);
        if (ordenTrabajoRepository.existsByVehiculoIdAndEstadoNotIn(id, ESTADOS_CERRADOS)) {
            throw new ConflictoException("El vehículo tiene órdenes de trabajo abiertas");
        }
        vehiculo.setActivo(false);
    }

    private Vehiculo obtenerVisible(Integer id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado"));
        if (!SecurityUtils.esAdmin() && !vehiculo.getUsuario().getId().equals(SecurityUtils.idActual())) {
            throw new RecursoNoEncontradoException("Vehículo no encontrado");
        }
        return vehiculo;
    }

    private Usuario resolverPropietario(Integer usuarioId) {
        Integer propietarioId = usuarioId;
        if (propietarioId != null && !SecurityUtils.esAdmin()) {
            throw new SolicitudInvalidaException("Solo un administrador puede indicar usuarioId");
        }
        if (propietarioId == null) {
            propietarioId = SecurityUtils.idActual();
        }
        return usuarioRepository.findById(propietarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private void validarAnio(Integer anio) {
        if (anio > Year.now().getValue()) {
            throw new SolicitudInvalidaException("El año no puede ser posterior al año actual");
        }
    }

    private void copiarDatos(VehiculoRequest solicitud, Vehiculo vehiculo) {
        vehiculo.setMarca(solicitud.marca().trim());
        vehiculo.setModelo(solicitud.modelo().trim());
        vehiculo.setVersion(solicitud.version() == null ? null : solicitud.version().trim());
        vehiculo.setAnio(solicitud.anio());
        vehiculo.setKilometros(solicitud.kilometros());
    }
}
