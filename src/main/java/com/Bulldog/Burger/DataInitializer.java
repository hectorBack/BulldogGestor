package com.Bulldog.Burger;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.Bulldog.Burger.Entity.Usuario;
import com.Bulldog.Burger.Repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.findByCorreo("admin@example.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setCorreo("admin@example.com");
            admin.setContrasena(new BCryptPasswordEncoder().encode("admin123")); // Contraseña
            admin.setRole("ADMIN"); // Rol
            usuarioRepository.save(admin);
        }
    }
}
