package com.Bulldog.Burger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.Bulldog.Burger.Entity.Usuario;
import com.Bulldog.Burger.Repository.UsuarioRepository;

@Controller
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Página de login
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";  // Vista para el login
    }

    // Página de registro
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/register";  // Vista para el registro
    }

    // Proceso de registro de un nuevo usuario
    @PostMapping("/register")
    public String register(@ModelAttribute("usuario") Usuario usuario) {
        // Verificamos si el usuario ya existe en la base de datos
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
        	return "auth/register";  // Si el correo ya existe, redirige de nuevo a la página de registro
        }

        // Codificamos la contraseña antes de guardarla
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setRole("USER");  // Todos los usuarios nuevos tienen el rol USER por defecto
        usuarioRepository.save(usuario);
        return "redirect:/auth/login";  // Redirige al login después del registro exitoso
    }

}