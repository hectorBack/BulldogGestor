package com.Bulldog.Burger.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Pedido {
	
	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne
	    @JoinColumn(name = "usuario_id")
	    private Usuario usuario;
	    
	    
	    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
	    private List<PedidoProducto> productos = new ArrayList<>();
	    
	    private String tipoPedido; // "Para llevar" o "Para mesa"
	    private double total;
	    private String estado; // "Pendiente", "Completado"
	    private String notas; // Notas específicas para personalización
		private String clienteIdentificador;
		private LocalDateTime fecha= LocalDateTime.now();

	public Pedido(Long id, Usuario usuario, List<PedidoProducto> productos, String tipoPedido, double total, String estado, String notas, String clienteIdentificador, LocalDateTime fecha) {
		this.id = id;
		this.usuario = usuario;
		this.productos = productos;
		this.tipoPedido = tipoPedido;
		this.total = total;
		this.estado = estado;
		this.notas = notas;
		this.clienteIdentificador = clienteIdentificador;
		this.fecha = fecha;
	}

	public Pedido() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<PedidoProducto> getProductos() {
		return productos;
	}

	public void setProductos(List<PedidoProducto> productos) {
		this.productos = productos;
	}

	public String getTipoPedido() {
		return tipoPedido;
	}

	public void setTipoPedido(String tipoPedido) {
		this.tipoPedido = tipoPedido;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getNotas() {
		return notas;
	}

	public void setNotas(String notas) {
		this.notas = notas;
	}

	public String getClienteIdentificador() {
		return clienteIdentificador;
	}

	public void setClienteIdentificador(String clienteIdentificador) {
		this.clienteIdentificador = clienteIdentificador;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}
}
