package com.rotacerta.api.repository;

import com.rotacerta.api.model.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByCustomerIdOrderByPrimaryAddressDescCreatedAtAsc(Long customerId);
}
