package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.*;
import com.TPI.Programacion.IV.Exception.RecursoNoEncontradoException;
import com.TPI.Programacion.IV.Model.*;
import com.TPI.Programacion.IV.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubastaService {

    @Autowired private SubastaRepository subastaRepository;
    @Autowired private CategoriaRepository categoryRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // ─── Consultas ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SubastaResponseDTO> listarSubastas(String estado) {
        List<Subasta> subastas = (estado != null && !estado.isBlank())
                ? subastaRepository.findByEstado(EstadoSubasta.valueOf(estado.toUpperCase()))
                : subastaRepository.findAll();
        return subastas.stream().map(this::mapearADto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubastaResponseDTO obtenerPorId(Long id) {
        Subasta subasta = subastaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada con id: " + id));
        return mapearADto(subasta);
    }

    @Transactional(readOnly = true)
    public List<SubastaResponseDTO> listarPorVendedor(Long vendedorId) {
        return subastaRepository.findByVendedorIdOrderByFechaCierreDesc(vendedorId)
                .stream().map(this::mapearADto).collect(Collectors.toList());
    }

    // ─── Crear ────────────────────────────────────────────────────────────────

    @Transactional
    public SubastaResponseDTO crearSubasta(SubastaRequestDTO request, Long vendedorId) {
        Categoria categoria = categoryRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vendedor no encontrado"));

        Producto producto = new Producto();
        producto.setNombre(request.producto().nombre());
        producto.setDescripcion(request.producto().descripcion());
        producto.setImagenUrl(request.producto().imagenUrl());

        Subasta subasta = new Subasta();
        subasta.setPrecioBase(request.precioBase());
        subasta.setMontoActual(request.precioBase());
        // Si no se especifica fecha de inicio, arranca "ya": se usa el reloj del servidor,
        // nunca uno provisto (ni inferido) por el cliente.
        subasta.setFechaInicio(request.fechaInicio() != null
                ? request.fechaInicio()
                : LocalDateTime.now(ZoneOffset.UTC));
        subasta.setFechaCierre(request.fechaCierre());
        subasta.setIncrementoMinimo(request.incrementoMinimo());
        subasta.setDescripcion(request.descripcion());
        subasta.setEstado(EstadoSubasta.BORRADOR); // Estado inicial del flujo BORRADOR → PUBLICADA → ACTIVA
        subasta.setCategoria(categoria);
        subasta.setVendedor(vendedor);
        subasta.setProducto(producto);

        validarVentanaTemporal(subasta);

        // El botón "Publicar subasta" del vendedor crea Y publica en un mismo paso;
        // la transición BORRADOR → PUBLICADA queda registrada en el historial igual.
        subasta.cambiarEstado(EstadoSubasta.PUBLICADA, vendedor, "Subasta publicada por el vendedor al momento de la creación.");
        activarSiLlegoLaHora(subasta, vendedor);

        Subasta guardada = subastaRepository.save(subasta);
        return mapearADto(guardada);
    }

    // ─── Publicar (BORRADOR → PUBLICADA) ────────────────────────────────────────

    @Transactional
    public SubastaResponseDTO publicarSubasta(Long subastaId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));

        if (!esAdmin && !subasta.getVendedor().getNombreUsuario().equals(username)) {
            throw new SecurityException("No tenés permiso para publicar esta subasta.");
        }

        validarVentanaTemporal(subasta);

        Usuario responsable = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        subasta.cambiarEstado(EstadoSubasta.PUBLICADA, responsable, "Subasta publicada manualmente por el vendedor.");
        activarSiLlegoLaHora(subasta, responsable);
        return mapearADto(subastaRepository.save(subasta));
    }

    private void validarVentanaTemporal(Subasta subasta) {
        if (!subasta.validarPeriodoMaximo()) {
            throw new IllegalArgumentException("El período de la subasta excede el máximo permitido de 2 semanas.");
        }
        if (!subasta.getFechaCierre().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new IllegalArgumentException("La fecha de cierre debe ser posterior al momento actual del servidor.");
        }
    }

    /**
     * Si la fecha de inicio ya se alcanzó al momento de publicar (típicamente porque
     * se dejó vacía para arrancar "ya", útil en demos en vivo), activa la subasta
     * en el acto en lugar de esperar al próximo barrido del scheduler.
     */
    private void activarSiLlegoLaHora(Subasta subasta, Usuario responsable) {
        if (!LocalDateTime.now(ZoneOffset.UTC).isBefore(subasta.getFechaInicio())) {
            subasta.cambiarEstado(EstadoSubasta.ACTIVA, responsable, "Inicio inmediato: la fecha de inicio ya se había alcanzado.");
        }
    }

    /**
     * Corrige el estado de la subasta según el reloj del servidor si el scheduler todavía
     * no pasó por ella (evita mostrar/operar sobre un estado desactualizado entre ticks).
     * Misma lógica que SubastaSchedulerService, aplicada puntualmente sobre una instancia.
     */
    private void sincronizarEstadoAutomatico(Subasta subasta) {
        LocalDateTime ahora = LocalDateTime.now(ZoneOffset.UTC);

        if (subasta.getEstado() == EstadoSubasta.PUBLICADA && !ahora.isBefore(subasta.getFechaInicio())) {
            subasta.cambiarEstado(EstadoSubasta.ACTIVA, null, "Inicio automático programado");
            subastaRepository.save(subasta);
        } else if (subasta.getEstado() == EstadoSubasta.ACTIVA && ahora.isAfter(subasta.getFechaCierre())) {
            if (subasta.getGanador() != null) {
                subasta.setFechaAdjudicacion(ahora);
                subasta.cambiarEstado(EstadoSubasta.ADJUDICADA, null,
                        "Adjudicada automáticamente. Precio final: " + subasta.getMontoActual());
            } else {
                subasta.cambiarEstado(EstadoSubasta.FINALIZADA, null, "Finalizada sin pujas");
            }
            subastaRepository.save(subasta);
        }
    }

    // ─── Pujar ────────────────────────────────────────────────────────────────

    @Transactional
    public SubastaResponseDTO procesarPuja(Long subastaId, Long usuarioOferenteId, PujaRequestDTO pujaRequest) {
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));
        Usuario oferente = usuarioRepository.findById(usuarioOferenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario oferente no encontrado"));

        sincronizarEstadoAutomatico(subasta);

        if (!subasta.validarNuevaPuja(pujaRequest.monto(), oferente)) {
            throw new IllegalStateException("La puja no cumple con los requisitos mínimos o la subasta no está activa.");
        }

        Puja nuevaPuja = new Puja();
        nuevaPuja.setMonto(pujaRequest.monto());
        nuevaPuja.setFechaPuja(LocalDateTime.now(ZoneOffset.UTC));
        nuevaPuja.setUsuario(oferente);
        nuevaPuja.setSubasta(subasta);

        subasta.getPujas().add(nuevaPuja);
        subasta.setMontoActual(pujaRequest.monto());
        subasta.setGanador(oferente);

        return mapearADto(subastaRepository.save(subasta));
    }

    // ─── Cancelar ─────────────────────────────────────────────────────────────

    @Transactional
    public SubastaResponseDTO cancelarSubasta(Long subastaId, String motivo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));

        // CANCELADA solo es una transición válida desde PUBLICADA, ACTIVA o EN_DISPUTA (ver Subasta.cambiarEstado)
        if (subasta.getEstado() != EstadoSubasta.PUBLICADA
                && subasta.getEstado() != EstadoSubasta.ACTIVA
                && subasta.getEstado() != EstadoSubasta.EN_DISPUTA) {
            throw new IllegalStateException("La subasta no puede ser cancelada en su estado actual.");
        }

        if (!esAdmin) {
            boolean esVendedor = subasta.getVendedor().getNombreUsuario().equals(username);
            if (!esVendedor) {
                throw new SecurityException("No tenés permiso para cancelar esta subasta.");
            }
            if (!subasta.getPujas().isEmpty()) {
                throw new IllegalStateException("No podés cancelar una subasta que ya tiene pujas. Solo un ADMIN puede hacerlo.");
            }
        }

        Usuario responsable = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        subasta.cambiarEstado(EstadoSubasta.CANCELADA, responsable, motivo);
        return mapearADto(subastaRepository.save(subasta));
    }

    // ─── Mapeo a DTO ──────────────────────────────────────────────────────────

    public SubastaResponseDTO mapearADto(Subasta s) {
        boolean esAdmin = esAdmin();
        // Las identidades se revelan solo después de que la subasta termina o si es admin
        boolean mostrarNombres = esAdmin
                || s.getEstado() != EstadoSubasta.ACTIVA
                && s.getEstado() != EstadoSubasta.PUBLICADA
                && s.getEstado() != EstadoSubasta.BORRADOR;

        ProductoResponseDTO prodDto = new ProductoResponseDTO(
                s.getProducto().getId(), s.getProducto().getNombre(),
                s.getProducto().getDescripcion(), s.getProducto().getImagenUrl());
        CategoriaResponseDTO catDto = new CategoriaResponseDTO(s.getCategoria().getId(), s.getCategoria().getNombre());

        Long ganadorId = s.getGanador() != null ? s.getGanador().getId() : null;
        String ganadorNombre = s.getGanador() != null ? s.getGanador().getNombreUsuario() : "Sin pujas";

        List<PujaResponseDTO> pujasDto = s.getPujas() == null ? List.of() :
                s.getPujas().stream()
                        .sorted(Comparator.comparing(Puja::getMonto).reversed())
                        .map(p -> {
                            String nombre = mostrarNombres
                                    ? p.getUsuario().getNombreUsuario()
                                    : iniciales(p.getUsuario().getNombreUsuario());
                            Long oid = mostrarNombres ? p.getUsuario().getId() : null;
                            return new PujaResponseDTO(p.getId(), p.getMonto(), p.getFechaPuja(), oid, nombre);
                        })
                        .collect(Collectors.toList());

        return new SubastaResponseDTO(
                s.getId(), s.getPrecioBase(), s.getMontoActual(), s.getFechaInicio(), s.getFechaCierre(),
                s.getFechaAdjudicacion(),
                s.getIncrementoMinimo(), s.getDescripcion(), s.getEstado(),
                prodDto, catDto, s.getVendedor().getId(), s.getVendedor().getNombreUsuario(),
                ganadorId, ganadorNombre, pujasDto);
    }

    private boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "***";
        String[] partes = nombre.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : partes) sb.append(Character.toUpperCase(p.charAt(0)));
        return sb + "***";
    }
}
