package com.medpoint.repository;
import com.medpoint.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrueOrderByNameAsc();
    List<Product> findByCategoryAndActiveTrue(String category);
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stock <= :threshold ORDER BY p.stock ASC")
    List<Product> findLowStock(@Param("threshold") int threshold);
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true ORDER BY p.category")
    List<String> findDistinctCategories();


//    List<Product> findByActiveTrueOrderByNameAsc();
//
//    // Used by MartServiceImpl.getCategories() — kept for backwards compat
//    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true ORDER BY p.category")
//    List<String> findDistinctCategories();

    // Used by CategoryServiceImpl for rename + delete guards
    List<Product> findByCategory(String category);
    boolean existsByCategory(String category);
}
