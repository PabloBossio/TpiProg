package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.RolRequestDTO;
import com.TPI.Programacion.IV.DTO.RolResponseDTO;
import com.TPI.Programacion.IV.Model.Rol;
import com.TPI.Programacion.IV.Repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    @Transactional
    public RolResponseDTO crear(RolRequestDTO request) {
        Rol rol = new Rol();
        rol.setNombreRol(request.nombreRol().toUpperCase()); // Forzamos mayúsculas (ej: ROLE_ADMIN)

        Rol guardado = rolRepository.save(rol);
        return new RolResponseDTO(guardado.getId(), guardado.getNombreRol());
    }

    @Transactional(readOnly = true)
    public List<RolResponseDTO> listar() {
        return rolRepository.findAll().stream()
                .map(r -> new RolResponseDTO(r.getId(), r.getNombreRol()))
                .collect(Collectors.toList());
    }
}