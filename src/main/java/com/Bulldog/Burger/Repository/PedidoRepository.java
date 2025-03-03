package com.Bulldog.Burger.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.Bulldog.Burger.Entity.Pedido;
import com.Bulldog.Burger.Entity.Usuario;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
	@Query("SELECT p FROM Pedido p JOIN FETCH p.productos WHERE p.usuario = :usuario")
	List<Pedido> findByUsuario(Usuario usuario);

	@Query("SELECT p FROM Pedido p WHERE p.fecha < CURRENT_DATE")
	List<Pedido> findPedidosAnteriores();


		
}
