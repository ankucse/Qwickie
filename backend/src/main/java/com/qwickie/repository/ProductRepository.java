package com.qwickie.repository;

import com.qwickie.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Ankit Sinha
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
