package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.Model.Usuario;
import com.TPI.Programacion.IV.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario crear(Usuario usuario){

        if(usuario.getEmail() == null || usuario.getEmail().isEmpty()){
            throw new RuntimeException("El email es obligatorio");
        }

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listar(){
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id){
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public Usuario editar(Long id, Usuario usuarioActualizado){
        Usuario usuario = buscarPorId(id);
        usuario.setNombreUsuario(usuarioActualizado.getNombreUsuario());
        usuario.setEmail(usuarioActualizado.getEmail());
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id){
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
    }

    public Usuario bloquearUsuario(Long id){
        Usuario usuario = buscarPorId(id);
        usuario.setEstaBloqueado(true);
        return usuarioRepository.save(usuario);
    }

    public Usuario desbloquearUsuario(Long id){
        Usuario usuario = buscarPorId(id);
        usuario.setEstaBloqueado(false);
        return usuarioRepository.save(usuario);
    }
}