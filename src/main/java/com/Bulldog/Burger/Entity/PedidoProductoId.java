package com.Bulldog.Burger.Entity;

import java.io.Serializable;
import java.util.Objects;

public class PedidoProductoId implements Serializable {
	
	private Long pedido;
    private Long producto;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PedidoProductoId that = (PedidoProductoId) o;
        return Objects.equals(pedido, that.pedido) &&
               Objects.equals(producto, that.producto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pedido, producto);
    }

	public Long getPedido() {
		return pedido;
	}

	public void setPedido(Long pedido) {
		this.pedido = pedido;
	}

	public Long getProducto() {
		return producto;
	}

	public void setProducto(Long producto) {
		this.producto = producto;
	}

	public PedidoProductoId() {
		super();
	}

	public PedidoProductoId(Long pedido, Long producto) {
		super();
		this.pedido = pedido;
		this.producto = producto;
	}

	

}
