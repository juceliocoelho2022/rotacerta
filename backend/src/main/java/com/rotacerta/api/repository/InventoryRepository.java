package com.rotacerta.api.repository;

import com.rotacerta.api.model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Query("select i from Inventory i join fetch i.product p order by p.name")
    List<Inventory> findAllWithProduct();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i join fetch i.product p where upper(p.sku) = upper(:sku)")
    Optional<Inventory> findBySkuForUpdate(@Param("sku") String sku);

    @Query("select i from Inventory i join fetch i.product p where (i.totalQuantity - i.reservedQuantity) <= i.minimumQuantity order by (i.totalQuantity - i.reservedQuantity)")
    List<Inventory> findLowStock();
}
