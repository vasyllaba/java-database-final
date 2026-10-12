package com.project.code.Repo;

import com.project.code.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    public List<Product> findAll();

    public List<Product> findByCategory(String category);

    public List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    public Product findBySku(String sku);

    public Product findByName(String name);

    @Query("SELECT i.product FROM Inventory i WHERE i.store.id = :storeId")
    List<Product> findProductsByStoreId(Long storeId);

    @Query("SELECT i.product FROM Inventory i WHERE i.store.id = :storeId AND LOWER(i.product.category) LIKE LOWER(CONCAT('%', :category, '%'))")
    List<Product> findByCategoryAndStoreId(String category, Long storeId);

    @Query("SELECT i.product FROM Inventory i WHERE i.store.id = :storeId AND LOWER(i.product.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameLike(Long storeId, String name);

    @Query("SELECT i.product FROM Inventory i WHERE i.store.id = :storeId AND LOWER(i.product.name) LIKE LOWER(CONCAT('%', :pname, '%')) AND LOWER(i.product.category) LIKE LOWER(CONCAT('%', :category, '%'))")
    public List<Product> findByNameAndCategory(Long storeId, String pname, String category);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :subname, '%'))")
    public List<Product> findProductBySubName(String subname);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :subname, '%')) AND p.category = :category")
    List<Product> findProductBySubNameAndCategory(String subname, String category);

    default List<Product> findProductByCategory(String category, Long storeId) {
        return findByCategoryAndStoreId(category, storeId);
    }

}
