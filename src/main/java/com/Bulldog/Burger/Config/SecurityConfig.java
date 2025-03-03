package com.Bulldog.Burger.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.Bulldog.Burger.Service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
    private CustomUserDetailsService  userDetailsService;  // Servicio para cargar el usuario
	
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        	.csrf().disable()  // Deshabilitar temporalmente CSRF para pruebas
            .authorizeRequests()
            .requestMatchers("/admin/**").hasRole("ADMIN")  // Solo los administradores pueden acceder a rutas que comienzan con /admin
                .requestMatchers("/pedidos/**").hasAnyRole("USER", "ADMIN") // Usuarios y admin
                .requestMatchers("/auth/**").permitAll()  // Permite acceso público a otras rutas
            .and()
            .formLogin()
            .loginPage("/auth/login")
            .successHandler((request, response, authentication) -> {
                // Verificar roles y redirigir según sea necesario
                if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    response.sendRedirect("/admin");
                } else {
                    response.sendRedirect("/pedidos");
                }
            })
            .permitAll()
            .and()
            .logout()
            .logoutUrl("/admin/logout") // URL para cerrar sesión
            .logoutSuccessUrl("/auth/login?logout") // Redirección después de cerrar sesión
            .invalidateHttpSession(true) // Invalida la sesión
            .deleteCookies("JSESSIONID") // Borra cookies
                .permitAll();

        return http.build();
    }
	
	 @Bean
	    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
	        return http.getSharedObject(AuthenticationManagerBuilder.class)
	                .userDetailsService(userDetailsService)
	                .passwordEncoder(passwordEncoder())  // Usa el codificador de contraseñas que prefieras
	                .and()
	                .build();
	    }
	 
	 @Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();  // Codificador de contraseñas, puedes cambiarlo si lo prefieres
	    }

}
