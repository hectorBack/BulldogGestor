package com.Bulldog.Burger.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

@Entity
@IdClass(PedidoProductoId.class)
public class PedidoProducto {
	
	@Id
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

	@Id
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private int cantidad;

    private double subtotal;
    

	public PedidoProducto(Pedido pedido, Producto producto, int cantidad, double subtotal) {
		super();
		this.pedido = pedido;
		this.producto = producto;
		this.cantidad = cantidad;
		this.subtotal = subtotal;
	}

	public PedidoProducto() {
		super();
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public double getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(double subtotal) {
		this.subtotal = subtotal;
	}

}
