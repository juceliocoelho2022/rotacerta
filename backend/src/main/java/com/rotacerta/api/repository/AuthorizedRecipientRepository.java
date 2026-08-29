package com.rotacerta.api.repository;

import com.rotacerta.api.model.AuthorizedRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorizedRecipientRepository extends JpaRepository<AuthorizedRecipient, Long> {
    List<AuthorizedRecipient> findByCustomerIdOrderByActiveDescNameAsc(Long customerId);
}
