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

	@Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE DATE(p.fecha) = CURRENT_DATE")
	Double obtenerTotalIngresosHoy();

	@Query(value = "SELECT DAYNAME(p.fecha) AS dia, COALESCE(SUM(p.total), 0) " +
			"FROM pedido p " +
			"WHERE p.fecha >= CURDATE() - INTERVAL 6 DAY " +
			"GROUP BY dia " +
			"ORDER BY FIELD(dia, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')",
			nativeQuery = true)
	List<Object[]> obtenerIngresosPorDia();

	@Query("SELECT FUNCTION('MONTHNAME', p.fecha), SUM(p.total) FROM Pedido p GROUP BY FUNCTION('MONTHNAME', p.fecha) ORDER BY FUNCTION('MONTH', p.fecha)")
	List<Object[]> obtenerIngresosPorMes();

	@Query("SELECT pp.producto.nombre, SUM(pp.cantidad) FROM PedidoProducto pp GROUP BY pp.producto.nombre ORDER BY SUM(pp.cantidad) DESC")
	List<Object[]> obtenerProductosMasVendidos();







}
