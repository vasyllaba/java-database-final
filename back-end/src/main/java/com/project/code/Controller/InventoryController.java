package com.project.code.Controller;

import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ServiceClass serviceClass;

    @Autowired
    public InventoryController(ProductRepository productRepository,
                               InventoryRepository inventoryRepository,
                               ServiceClass serviceClass) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.serviceClass = serviceClass;
    }

    @PutMapping
    @Transactional
    public Map<String, String> updateInventory(@RequestBody CombinedRequest request) {
        Product product = request.getProduct();
        Inventory inventory = request.getInventory();
        if (product == null || inventory == null || inventory.getStore() == null) {
            return Map.of("message", "Product, inventory and store are required");
        }
        if (!serviceClass.ValidateProductId(product.getId())) {
            return Map.of("message", "Product not present in database");
        }

        inventory.setProduct(product);
        Inventory existingInventory = serviceClass.getInventoryId(inventory);
        if (existingInventory == null) {
            return Map.of("message", "No data available");
        }

        Product existingProduct = existingInventory.getProduct();
        existingProduct.setName(product.getName());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setSku(product.getSku());
        productRepository.save(existingProduct);

        existingInventory.setStockLevel(inventory.getStockLevel());
        inventoryRepository.save(existingInventory);
        return Map.of("message", "Successfully updated product");
    }

    @PostMapping
    public Map<String, String> saveInventory(@RequestBody Inventory inventory) {
        if (inventory.getProduct() == null || inventory.getStore() == null) {
            return Map.of("message", "Product and store are required");
        }
        if (!serviceClass.validateInventory(inventory)) {
            return Map.of("message", "Data already present");
        }
        inventoryRepository.save(inventory);
        return Map.of("message", "Data saved successfully");
    }

    @GetMapping("/{storeid}")
    public Map<String, Object> getAllProducts(@PathVariable("storeid") Long storeId) {
        return Map.of("products", productRepository.findProductsByStoreId(storeId));
    }

    @GetMapping("filter/{category}/{name}/{storeid}")
    public Map<String, Object> getProductName(@PathVariable("category") String category,
                                              @PathVariable("name") String name,
                                              @PathVariable("storeid") Long storeId) {
        List<Product> products;
        if ("null".equals(category) && "null".equals(name)) {
            products = productRepository.findProductsByStoreId(storeId);
        } else if ("null".equals(category)) {
            products = productRepository.findByNameLike(storeId, name);
        } else if ("null".equals(name)) {
            products = productRepository.findByCategoryAndStoreId(category, storeId);
        } else {
            products = productRepository.findByNameAndCategory(storeId, name, category);
        }
        return Map.of("product", products);
    }

    @GetMapping("search/{name}/{storeId}")
    public Map<String, Object> searchProduct(@PathVariable("name") String name,
                                             @PathVariable("storeId") Long storeId) {
        return Map.of("product", productRepository.findByNameLike(storeId, name));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> removeProduct(@PathVariable("id") long id) {
        if (!serviceClass.ValidateProductId(id)) {
            return Map.of("message", "Product not present in database");
        }
        inventoryRepository.deleteByProductId(id);
        inventoryRepository.flush();
        productRepository.deleteById(id);
        return Map.of("message", "Product deleted successfully");
    }

    @GetMapping("validate/{quantity}/{storeId}/{productId}")
    public boolean validateQuantity(@PathVariable("quantity") int quantity,
                                    @PathVariable("storeId") Long storeId,
                                    @PathVariable("productId") Long productId) {
        if (quantity < 0) {
            return false;
        }
        return inventoryRepository.findByProductIdandStoreId(productId, storeId)
                .map(inventory -> inventory.getStockLevel() != null
                        && inventory.getStockLevel() >= quantity)
                .orElse(false);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Map<String, String> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return Map.of("message", "Unable to save or delete data because of a data integrity violation");
    }
}
