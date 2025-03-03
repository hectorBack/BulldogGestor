package com.Bulldog.Burger.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.Bulldog.Burger.Entity.Usuario;
import com.Bulldog.Burger.Repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	
	@Autowired
	private UsuarioRepository usuarioRepository;

	@Override
	public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
	    // Manejar el Optional con orElseThrow
	    Usuario usuario = usuarioRepository.findByCorreo(correo)
	        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + correo));

	    // Asignar el rol del usuario
	    return new User(usuario.getCorreo(), usuario.getContrasena(),
	            AuthorityUtils.createAuthorityList("ROLE_" + usuario.getRole()));
	}

}
