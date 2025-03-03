package com.Bulldog.Burger.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.Bulldog.Burger.Entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

}
