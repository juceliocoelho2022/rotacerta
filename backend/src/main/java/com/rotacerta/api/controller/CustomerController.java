package com.rotacerta.api.controller;

import com.rotacerta.api.dto.AuthorizedRecipientCreateRequest;
import com.rotacerta.api.dto.AuthorizedRecipientResponse;
import com.rotacerta.api.dto.CustomerAddressCreateRequest;
import com.rotacerta.api.dto.CustomerAddressResponse;
import com.rotacerta.api.dto.CustomerCreateRequest;
import com.rotacerta.api.dto.CustomerDetailResponse;
import com.rotacerta.api.dto.CustomerListResponse;
import com.rotacerta.api.dto.CustomerOrderResponse;
import com.rotacerta.api.dto.CustomerUpdateRequest;
import com.rotacerta.api.dto.DeliveryPreferenceResponse;
import com.rotacerta.api.dto.DeliveryPreferenceUpdateRequest;
import com.rotacerta.api.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerListResponse> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public CustomerDetailResponse findById(@PathVariable Long id) {
        return customerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDetailResponse create(@Valid @RequestBody CustomerCreateRequest request) {
        return customerService.create(request);
    }

    @PutMapping("/{id}")
    public CustomerDetailResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return customerService.update(id, request);
    }

    @GetMapping("/{id}/addresses")
    public List<CustomerAddressResponse> addresses(@PathVariable Long id) {
        return customerService.addresses(id);
    }

    @PostMapping("/{id}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerAddressResponse addAddress(
            @PathVariable Long id,
            @Valid @RequestBody CustomerAddressCreateRequest request
    ) {
        return customerService.addAddress(id, request);
    }

    @GetMapping("/{id}/authorized-recipients")
    public List<AuthorizedRecipientResponse> authorizedRecipients(@PathVariable Long id) {
        return customerService.authorizedRecipients(id);
    }

    @PostMapping("/{id}/authorized-recipients")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorizedRecipientResponse addAuthorizedRecipient(
            @PathVariable Long id,
            @Valid @RequestBody AuthorizedRecipientCreateRequest request
    ) {
        return customerService.addAuthorizedRecipient(id, request);
    }

    @GetMapping("/{id}/preferences")
    public DeliveryPreferenceResponse preference(@PathVariable Long id) {
        return customerService.preference(id);
    }

    @PutMapping("/{id}/preferences")
    public DeliveryPreferenceResponse updatePreference(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryPreferenceUpdateRequest request
    ) {
        return customerService.updatePreference(id, request);
    }

    @GetMapping("/{id}/orders")
    public List<CustomerOrderResponse> orders(@PathVariable Long id) {
        return customerService.orders(id);
    }
}
