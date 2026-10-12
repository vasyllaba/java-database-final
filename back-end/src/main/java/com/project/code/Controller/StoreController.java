package com.project.code.Controller;

import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.StoreRepository;
import com.project.code.Service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/store")
public class StoreController {
    private final StoreRepository storeRepository;
    private final OrderService orderService;

    public StoreController(StoreRepository storeRepository, OrderService orderService) {
        this.storeRepository = storeRepository;
        this.orderService = orderService;
    }

    @PostMapping
    public Map<String, String> addStore(@RequestBody Store store) {
        storeRepository.save(store);
        return Map.of("message", "Store created successfully");
    }


    @GetMapping("validate/{storeId}")
    public boolean validateStore(@PathVariable Long storeId) {
        return storeRepository.existsById(storeId);
    }

    @PostMapping("/placeOrder")
    public Map<String, String> placeOrder(@RequestBody PlaceOrderRequestDTO placeOrderRequestDTO) {
        try {
            orderService.saveOrder(placeOrderRequestDTO);
            return Map.of("message", "Order placed successfully");
        } catch (Exception e) {
            return Map.of("Error", "Failed to place order: " + e.getMessage());
        }

    }

}
