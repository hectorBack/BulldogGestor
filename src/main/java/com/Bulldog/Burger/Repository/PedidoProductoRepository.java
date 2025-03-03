package com.Bulldog.Burger.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Bulldog.Burger.Entity.Pedido;
import com.Bulldog.Burger.Entity.PedidoProducto;
import com.Bulldog.Burger.Entity.PedidoProductoId;

public interface PedidoProductoRepository extends JpaRepository<PedidoProducto, PedidoProductoId> {
	List<PedidoProducto> findByPedido(Pedido pedido);
	
	

}
