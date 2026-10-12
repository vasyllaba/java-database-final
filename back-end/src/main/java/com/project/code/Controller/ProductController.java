package com.project.code.Controller;

import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductRepository productRepository;
    private final ServiceClass serviceClass;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public ProductController(ProductRepository productRepository,
                             ServiceClass serviceClass,
                             InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.serviceClass = serviceClass;
        this.inventoryRepository = inventoryRepository;
    }

    @PostMapping
    public Map<String, String> addProduct(@RequestBody Product product) {
        if (!serviceClass.validateProduct(product)) {
            return Map.of("message", "Product already present in database");
        }
        productRepository.save(product);
        return Map.of("message", "Product saved successfully");
    }

    @GetMapping("/product/{id}")
    public Map<String, Object> getProductbyId(@PathVariable("id") long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("products", productRepository.findById(id).orElse(null));
        return response;
    }

    @PutMapping
    public Map<String, String> updateProduct(@RequestBody Product product) {
        productRepository.save(product);
        return Map.of("message", "Successfully updated product");
    }

    @GetMapping("/category/{name}/{category}")
    public Map<String, Object> filterbyCategoryProduct(@PathVariable("name") String name,
                                                      @PathVariable("category") String category) {
        List<Product> products;
        if ("null".equals(name) && "null".equals(category)) {
            products = productRepository.findAll();
        } else if ("null".equals(name)) {
            products = productRepository.findByCategory(category);
        } else if ("null".equals(category)) {
            products = productRepository.findProductBySubName(name);
        } else {
            products = productRepository.findProductBySubNameAndCategory(name, category);
        }
        return Map.of("products", products);
    }

    @GetMapping
    public Map<String, Object> listProduct() {
        return Map.of("products", productRepository.findAll());
    }

    @GetMapping("filter/{category}/{storeid}")
    public Map<String, Object> getProductbyCategoryAndStoreId(
            @PathVariable("category") String category,
            @PathVariable("storeid") Long storeId) {
        return Map.of("product", productRepository.findProductByCategory(category, storeId));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> deleteProduct(@PathVariable("id") long id) {
        if (!serviceClass.ValidateProductId(id)) {
            return Map.of("message", "Product not present in database");
        }
        inventoryRepository.deleteByProductId(id);
        inventoryRepository.flush();
        productRepository.deleteById(id);
        return Map.of("message", "Product deleted successfully");
    }

    @GetMapping("/searchProduct/{name}")
    public Map<String, Object> searchProduct(@PathVariable("name") String name) {
        return Map.of("products", productRepository.findProductBySubName(name));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Map<String, String> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return Map.of("message", "Unable to save or delete product because of a data integrity violation");
    }

    @ExceptionHandler(DataAccessException.class)
    public Map<String, String> handleDataAccessException(DataAccessException exception) {
        return Map.of("message", "Unable to complete product operation because of a database error");
    }
}
