package com.Bulldog.Burger.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Bulldog.Burger.Entity.Pedido;
import com.Bulldog.Burger.Entity.PedidoProducto;
import com.Bulldog.Burger.Entity.Producto;
import com.Bulldog.Burger.Entity.Usuario;
import com.Bulldog.Burger.Repository.PedidoRepository;
import com.Bulldog.Burger.Repository.ProductoRepository;
import com.Bulldog.Burger.Repository.UsuarioRepository;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String listarPedidos(Model model) {
        LocalDate hoy = LocalDate.now();

        List<Pedido> todosLosPedidos = pedidoRepository.findAll();

        List<Pedido> pedidosHoy = todosLosPedidos.stream()
                .filter(p -> p.getFecha() != null && p.getFecha().toLocalDate().isEqual(hoy))
                .collect(Collectors.toList());

        List<Pedido> pedidosAnteriores = todosLosPedidos.stream()
                .filter(p -> p.getFecha() != null && p.getFecha().toLocalDate().isBefore(hoy))
                .collect(Collectors.toList());

        // Calcular el total de ventas del día
        double totalVentasHoy = pedidosHoy.stream()
                .mapToDouble(Pedido::getTotal)
                .sum();

        model.addAttribute("pedidosHoy", pedidosHoy);
        model.addAttribute("pedidosAnteriores", pedidosAnteriores);
        model.addAttribute("totalVentasHoy", totalVentasHoy); // Enviar total a la vista

        return "user/pedidos";
    }


    @GetMapping("/crear")
    public String mostrarFormularioCrearPedido(Model model) {
        List<Producto> productos = productoRepository.findAll(); // Obtener todos los productos
        model.addAttribute("productos", productos); // Enviar productos a la vista
        return "user/crear_pedido";
    }

    @PostMapping("/crear")
    public String crearPedido(
            @RequestParam(required = false) List<Long> productoIds,
            @RequestParam(required = false) List<Integer> cantidades,
            @RequestParam String tipoPedido,
            @RequestParam(required = false) String clienteIdentificador, // <-- Nuevo parámetro
            @RequestParam(required = false) String notas,
            Model model
    ) {
        // Validaciones iniciales
        if (productoIds == null || productoIds.isEmpty() || cantidades == null || cantidades.isEmpty()) {
            model.addAttribute("error", "Debe seleccionar al menos un producto y una cantidad.");
            return "error";
        }

        if (productoIds.size() != cantidades.size()) {
            model.addAttribute("error", "El número de productos no coincide con el número de cantidades.");
            return "error";
        }

        // Obtener los productos seleccionados por su ID
        List<Producto> productos = productoRepository.findAllById(productoIds);

        // Crear lista de detalles del pedido (PedidoProducto)
        List<PedidoProducto> pedidoProductos = new ArrayList<>();
        double total = 0;

        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            int cantidad = cantidades.get(i);

            if (cantidad <= 0) continue; // Ignorar productos con cantidad 0 o negativa

            double subtotal = producto.getPrecio() * cantidad;
            total += subtotal;

            PedidoProducto pedidoProducto = new PedidoProducto();
            pedidoProducto.setProducto(producto);
            pedidoProducto.setCantidad(cantidad);
            pedidoProducto.setSubtotal(subtotal);
            pedidoProductos.add(pedidoProducto);
        }

        if (pedidoProductos.isEmpty()) {
            model.addAttribute("error", "Debe seleccionar al menos un producto con cantidad válida.");
            return "error";
        }

        // Crear un nuevo pedido
        Pedido pedido = new Pedido();
        pedido.setTipoPedido(tipoPedido);
        pedido.setTotal(total);
        pedido.setEstado("Pendiente");
        pedido.setNotas(notas);
        pedido.setProductos(pedidoProductos);

        // Si es un pedido a Domicilio, guardar el identificador del cliente
        if ("Domicilio".equals(tipoPedido) && clienteIdentificador != null && !clienteIdentificador.isEmpty()) {
            pedido.setClienteIdentificador(clienteIdentificador);
        }

        // Establecer relación entre PedidoProducto y Pedido
        for (PedidoProducto pp : pedidoProductos) {
            pp.setPedido(pedido);
        }

        // Obtener el usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        pedido.setUsuario(usuario);

        // Guardar el pedido y sus productos
        pedidoRepository.save(pedido);

        return "redirect:/pedidos";
    }


    @GetMapping("/historial")
    public String verHistorialPedidos(Model model) {
        // Obtener el usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName())
                                           .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Obtener los pedidos realizados por el usuario
        List<Pedido> pedidos = pedidoRepository.findByUsuario(usuario);
        model.addAttribute("pedidos", pedidos);

        return "user/historialPedidos";
    }

    @GetMapping("/{id}/detalles")
    public String verDetallesPedido(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoRepository.findById(id)
                                        .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        model.addAttribute("pedido", pedido);
        return "user/detallePedido";
    }

    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Si está "Pendiente", pasa a "En Proceso"
        if ("Pendiente".equals(pedido.getEstado())) {
            pedido.setEstado("En Proceso");
        }
        // Si está "En Proceso", pasa a "Completado"
        else if ("En Proceso".equals(pedido.getEstado())) {
            pedido.setEstado("Completado");
        }
        // Si está "Completado", pasa a "Pendiente"
        else if ("Completado".equals(pedido.getEstado())) {
            pedido.setEstado("Pendiente");
        }
        // Si está "Cancelado", no hacer nada (opcional)
        else if ("Cancelado".equals(pedido.getEstado())) {
            return "redirect:/pedidos";
        }

        pedidoRepository.save(pedido);
        return "redirect:/pedidos";
    }
    
    @PostMapping("/eliminar/{id}")
    public String eliminarPedido(@PathVariable Long id) {
        // Elimina el pedido por ID
        pedidoRepository.deleteById(id);

        // Redirige a la lista de pedidos
        return "redirect:/pedidos";
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        // Busca el pedido por ID
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Obtener todos los productos disponibles para agregar
        List<Producto> productosDisponibles = productoRepository.findAll();

        // Agrega el pedido al modelo
        model.addAttribute("pedido", pedido);
        model.addAttribute("productosDisponibles", productosDisponibles);

        // Muestra el formulario de edición
        return "user/formulario-editar-pedido"; // Vista Thymeleaf
    }

    @PostMapping("/editar/{id}")
    public String actualizarPedido(@PathVariable Long id, @ModelAttribute Pedido pedidoActualizado) {
        // Busca el pedido original por ID
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Actualiza los campos del pedido
        pedido.setTipoPedido(pedidoActualizado.getTipoPedido());
        pedido.setTotal(pedidoActualizado.getTotal());
        pedido.setEstado(pedidoActualizado.getEstado());
        pedido.setNotas(pedidoActualizado.getNotas());
        // (Actualiza otros campos según sea necesario)

        // Guarda el pedido actualizado
        pedidoRepository.save(pedido);

        System.out.println("Pedido recibido: " + pedidoActualizado);

        // Redirige a la lista de pedidos
        return "redirect:/pedidos";
    }

    @PostMapping("/eliminar-producto/{pedidoId}/{productoId}")
    public String eliminarProductoDePedido(@PathVariable Long pedidoId, @PathVariable Long productoId) {
        // Buscar el pedido por ID
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Filtrar y eliminar el producto del pedido
        pedido.getProductos().removeIf(pedidoProducto ->
                pedidoProducto.getProducto().getId().equals(productoId)
        );

        // Guardar el pedido actualizado
        pedidoRepository.save(pedido);

        // Redirigir a la edición del pedido
        return "redirect:/pedidos/editar/" + pedidoId;
    }

    //nuevo
    @PostMapping("/agregar-producto/{id}")
    public String agregarProductoAPedido(@PathVariable Long id,
                                         @RequestParam Long productoId,
                                         @RequestParam int cantidad) {
        // Verificar que la cantidad sea válida
        if (cantidad <= 0) {
            return "redirect:/pedidos/editar/" + id + "?error=La cantidad debe ser mayor a 0";
        }

        // Buscar el pedido
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Buscar el producto
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        // Verificar si el producto ya está en el pedido
        Optional<PedidoProducto> productoExistente = pedido.getProductos()
                .stream()
                .filter(pp -> pp.getProducto().getId().equals(productoId))
                .findFirst();

        if (productoExistente.isPresent()) {
            // Si el producto ya está en el pedido, actualizar la cantidad y subtotal
            PedidoProducto pedidoProducto = productoExistente.get();
            pedidoProducto.setCantidad(pedidoProducto.getCantidad() + cantidad);
            pedidoProducto.setSubtotal(pedidoProducto.getCantidad() * producto.getPrecio());
        } else {
            // Si no está, crear un nuevo PedidoProducto
            PedidoProducto nuevoPedidoProducto = new PedidoProducto();
            nuevoPedidoProducto.setPedido(pedido);
            nuevoPedidoProducto.setProducto(producto);
            nuevoPedidoProducto.setCantidad(cantidad);
            nuevoPedidoProducto.setSubtotal(producto.getPrecio() * cantidad);
            pedido.getProductos().add(nuevoPedidoProducto);
        }

        // Actualizar el total del pedido
        double nuevoTotal = pedido.getProductos().stream().mapToDouble(PedidoProducto::getSubtotal).sum();
        pedido.setTotal(nuevoTotal);

        // Guardar los cambios
        pedidoRepository.save(pedido);

        return "redirect:/editar/" + id;
    }

    @GetMapping("/historico")
    public String mostrarPedidosHistoricos(Model model) {
        // Obtiene los pedidos anteriores (por ejemplo, los pedidos que no son de hoy)
        List<Pedido> pedidosAnteriores = pedidoRepository.findPedidosAnteriores();
        model.addAttribute("pedidosAnteriores", pedidosAnteriores);
        return "user/pedidos-historicos"; // Redirige a la vista correcta
    }

}
