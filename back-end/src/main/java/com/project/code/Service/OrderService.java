package com.project.code.Service;

import com.project.code.Model.*;
import com.project.code.Repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest) {
        Customer customer = retrieveOrCreateCustomer(placeOrderRequest);
        Store store = storeRepository.findById(placeOrderRequest.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + placeOrderRequest.getStoreId()));

        OrderDetails orderDetails = createNewOrderDetails(customer, store, placeOrderRequest);
        createOrderItems(placeOrderRequest.getPurchaseProduct(), orderDetails);

    }

    private void createOrderItems(List<PurchaseProductDTO> productsDTO, OrderDetails orderDetails) {
        for (PurchaseProductDTO productDTO : productsDTO) {
            Inventory inventoryItem = inventoryRepository.findByProductIdandStoreId(productDTO.getId(), orderDetails.getStore().getId())
                    .orElseThrow(() -> new RuntimeException("Inventory not found for product ID: " + productDTO.getId() + " in store ID: " + orderDetails.getStore().getId()));

            reduceStockLevelOfThrow(inventoryItem, productDTO);
            createOrderItem(orderDetails, productDTO);

        }
    }

    private void createOrderItem(OrderDetails orderDetails, PurchaseProductDTO productDTO) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(orderDetails);
        orderItem.setProduct(productRepository.findById(productDTO.getId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productDTO.getId())));
        orderItem.setQuantity(productDTO.getQuantity());
        orderItem.setPrice(productDTO.getPrice());
        orderItemRepository.save(orderItem);
    }

    private void reduceStockLevelOfThrow(Inventory inventoryItem, PurchaseProductDTO productDTO) {
        if ( inventoryItem.getStockLevel() < productDTO.getQuantity()) {
            throw new RuntimeException("Insufficient stock for product ID: " + productDTO.getId());
        }

        inventoryItem.setStockLevel(inventoryItem.getStockLevel() - productDTO.getQuantity());
        inventoryRepository.save(inventoryItem);
    }

    private OrderDetails createNewOrderDetails(Customer customer, Store store, PlaceOrderRequestDTO placeOrderRequest) {
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setTotalPrice(placeOrderRequest.getTotalPrice());
        orderDetails.setDate(LocalDateTime.now());
        return orderDetailsRepository.save(orderDetails);
    }

    private Customer retrieveOrCreateCustomer(PlaceOrderRequestDTO placeOrderRequest) {
        if (customerRepository.existsByEmail(placeOrderRequest.getCustomerEmail())) {
            return customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());
        }
        Customer newCustomer = new Customer();
        newCustomer.setName(placeOrderRequest.getCustomerName());
        newCustomer.setEmail(placeOrderRequest.getCustomerEmail());
        newCustomer.setPhone(placeOrderRequest.getCustomerPhone());
        return  customerRepository.save(newCustomer);
    }
   
}
