package com.project.code.Repo;

import com.project.code.Model.Inventory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.store.id = :storeId")
    public Optional<Inventory> findByProductIdandStoreId(Long productId, Long storeId);

    public List<Inventory> findByStore_Id(Long storeId);

    @Modifying
    @Transactional
    public void deleteByProductId(Long productId);

}
