package com.Bulldog.Burger.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Bulldog.Burger.Entity.Pedido;
import com.Bulldog.Burger.Entity.Producto;
import com.Bulldog.Burger.Repository.PedidoRepository;
import com.Bulldog.Burger.Repository.ProductoRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;


    @GetMapping
    public String adminDashboard(Model model) {
        List<Pedido> pedidos = pedidoRepository.findAll();
        Double totalIngresosHoy = pedidoRepository.obtenerTotalIngresosHoy();

        // Calcular el total de pedidos
        int totalPedidos = pedidos.size();

        // Contar pedidos completados y pendientes
        long pedidosCompletados = pedidos.stream()
                .filter(p -> p.getEstado() != null && p.getEstado().trim().equalsIgnoreCase("Completado"))
                .count();

        long pedidosPendientes = pedidos.stream()
                .filter(p -> p.getEstado() != null && p.getEstado().trim().equalsIgnoreCase("Pendiente"))
                .count();

        // Obtener ingresos por día de la semana
        List<Object[]> ingresosPorDiaRaw = pedidoRepository.obtenerIngresosPorDia();

        // Crear un mapa con los días de la semana ordenados
        Map<String, Double> ingresosPorDia = new LinkedHashMap<>();
        List<String> diasSemana = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

        for (String dia : diasSemana) {
            ingresosPorDia.put(dia, 0.0); // Inicializar en 0
        }

        for (Object[] row : ingresosPorDiaRaw) {
            String dia = (String) row[0];
            Double total = (Double) row[1];
            ingresosPorDia.put(dia, total);
        }

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("totalIngresosHoy", totalIngresosHoy);
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("pedidosCompletados", pedidosCompletados);
        model.addAttribute("pedidosPendientes", pedidosPendientes);
        model.addAttribute("ingresosPorDia", ingresosPorDia);

        return "admin/dashboard";
    }

    @GetMapping("/productos")
    public String gestionarProductos(Model model) {
            List<Producto> productos = productoRepository.findAll();
            model.addAttribute("productos", productos);
            return "admin/gestionar_productos";  // Vista para gestionar productos
        }

    @PostMapping("/productos")
    public String agregarProducto(
        @ModelAttribute Producto producto,
        @RequestParam("imagen") MultipartFile imagen,  // Nuevo parámetro para la imagen
        RedirectAttributes redirectAttributes) {

        try {
            // 1. Subir la imagen y obtener su URL
            String imagenUrl = subirImagen(imagen); 
            producto.setImagenUrl(imagenUrl);
            
            // 2. Guardar el producto
            productoRepository.save(producto);
            
            redirectAttributes.addFlashAttribute("exito", "Producto creado correctamente");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen");
        }
        
        return "redirect:/admin/productos";
    }
     
     @GetMapping("/productos/editar/{id}")
     public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
         Producto producto = productoRepository.findById(id)
                 .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con el ID: " + id));
         model.addAttribute("producto", producto);
         return "admin/editar_producto";  // Vista para editar el producto
     }

     @PostMapping("/productos/editar/{id}")
     public String editarProducto(@PathVariable Long id, Producto productoActualizado) {
         Producto productoExistente = productoRepository.findById(id)
                 .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con el ID: " + id));

         productoExistente.setNombre(productoActualizado.getNombre());
         productoExistente.setPrecio(productoActualizado.getPrecio());
         productoExistente.setDescripcion(productoActualizado.getDescripcion());
         productoExistente.setCategoria(productoActualizado.getCategoria());
         productoExistente.setImagenUrl(productoActualizado.getImagenUrl());
         productoRepository.save(productoExistente);
         return "redirect:/admin/productos";
     }
     
     @GetMapping("/productos/eliminar/{id}")
     public String eliminarProducto(@PathVariable Long id) {
         productoRepository.deleteById(id);
         return "redirect:/admin/productos";
     }
     
     @GetMapping("/logout")
     public String logout(@RequestParam(value = "logout", required = false) String logout, Model model) {
         if (logout != null) {
             model.addAttribute("message", "Has cerrado sesión correctamente.");
         }
         return "auth/login"; // Página de login
     }
     
     @GetMapping("/pedidos/{id}")
     public String verDetallePedido(@PathVariable Long id, Model model) {
         Pedido pedido = pedidoRepository.findById(id)
             .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

         model.addAttribute("pedido", pedido);
         return "admin/detalles-pedido";
     }
     
     // Método para subir la imagen
     private String subirImagen(MultipartFile imagen) throws IOException {
         String nombreUnico = UUID.randomUUID().toString() + "_" + imagen.getOriginalFilename();
         Path directorioUploads = Paths.get("uploads");
         
         if (!Files.exists(directorioUploads)) {
             Files.createDirectories(directorioUploads);
         }
         
         Files.copy(
             imagen.getInputStream(),
             directorioUploads.resolve(nombreUnico),
             StandardCopyOption.REPLACE_EXISTING
         );
         
         return "/uploads/" + nombreUnico;
     }

    @GetMapping("/estadisticas")
    public String verEstadisticas(Model model) {
        // Consultar ingresos por mes
        List<Object[]> ingresosPorMesRaw = pedidoRepository.obtenerIngresosPorMes();
        Map<String, Double> ingresosPorMes = new LinkedHashMap<>();

        for (Object[] row : ingresosPorMesRaw) {
            String mes = (String) row[0];
            Double total = (Double) row[1];
            ingresosPorMes.put(mes, total);
        }

        // Consultar los productos más vendidos
        List<Object[]> productosMasVendidosRaw = pedidoRepository.obtenerProductosMasVendidos();
        List<String> productos = new ArrayList<>();
        List<Integer> cantidades = new ArrayList<>();

        for (Object[] row : productosMasVendidosRaw) {
            productos.add((String) row[0]);
            cantidades.add(((Number) row[1]).intValue());
        }

        model.addAttribute("meses", new ArrayList<>(ingresosPorMes.keySet()));
        model.addAttribute("ingresosPorMes", new ArrayList<>(ingresosPorMes.values()));
        model.addAttribute("productosMasVendidos", productos);
        model.addAttribute("cantidadesVendidas", cantidades);

        return "admin/estadisticas";
    }



}
