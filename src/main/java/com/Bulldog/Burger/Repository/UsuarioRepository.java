package com.Bulldog.Burger.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Bulldog.Burger.Entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByCorreo(String correo);

}
