package com.Bulldog.Burger.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        model.addAttribute("pedidos", pedidos);
        return "admin/dashboard";  // Vista para el administrador
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
     
}
