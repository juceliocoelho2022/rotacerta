package com.rotacerta.api.service;

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
import com.rotacerta.api.model.AuthorizedRecipient;
import com.rotacerta.api.model.Customer;
import com.rotacerta.api.model.CustomerAddress;
import com.rotacerta.api.model.DeliveryPreference;
import com.rotacerta.api.model.DeliveryStatus;
import com.rotacerta.api.model.OrderEntity;
import com.rotacerta.api.repository.AuthorizedRecipientRepository;
import com.rotacerta.api.repository.CustomerAddressRepository;
import com.rotacerta.api.repository.CustomerRepository;
import com.rotacerta.api.repository.DeliveryPreferenceRepository;
import com.rotacerta.api.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class CustomerService {

    private static final Set<DeliveryStatus> TERMINAL_STATUSES = Set.of(
            DeliveryStatus.DELIVERED,
            DeliveryStatus.DELIVERY_FAILED,
            DeliveryStatus.RETURNED,
            DeliveryStatus.CANCELLED
    );

    private static final Set<DeliveryStatus> OCCURRENCE_STATUSES = Set.of(
            DeliveryStatus.DELIVERY_FAILED,
            DeliveryStatus.RETURNED
    );

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final AuthorizedRecipientRepository recipientRepository;
    private final DeliveryPreferenceRepository preferenceRepository;
    private final OrderRepository orderRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerAddressRepository addressRepository,
            AuthorizedRecipientRepository recipientRepository,
            DeliveryPreferenceRepository preferenceRepository,
            OrderRepository orderRepository
    ) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.recipientRepository = recipientRepository;
        this.preferenceRepository = preferenceRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerListResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(this::toListResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerDetailResponse findById(Long id) {
        return toDetailResponse(getCustomer(id));
    }

    @Transactional
    public CustomerDetailResponse create(CustomerCreateRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Já existe um cliente cadastrado com este e-mail.");
        }

        Customer customer = customerRepository.save(new Customer(
                request.name().trim(),
                normalizedEmail,
                normalizeNullable(request.phone())
        ));

        preferenceRepository.save(new DeliveryPreference(customer));
        return toDetailResponse(customer);
    }

    @Transactional
    public CustomerDetailResponse update(Long id, CustomerUpdateRequest request) {
        Customer customer = getCustomer(id);
        String normalizedEmail = normalizeEmail(request.email());

        customerRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(other -> !other.getId().equals(customer.getId()))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Já existe outro cliente cadastrado com este e-mail.");
                });

        customer.updateProfile(
                request.name().trim(),
                normalizedEmail,
                normalizeNullable(request.phone())
        );
        customer.setActive(request.active());
        customerRepository.save(customer);
        return toDetailResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> addresses(Long customerId) {
        getCustomer(customerId);
        return addressRepository.findByCustomerIdOrderByPrimaryAddressDescCreatedAtAsc(customerId)
                .stream()
                .map(this::toAddressResponse)
                .toList();
    }

    @Transactional
    public CustomerAddressResponse addAddress(Long customerId, CustomerAddressCreateRequest request) {
        Customer customer = getCustomer(customerId);
        List<CustomerAddress> currentAddresses = addressRepository
                .findByCustomerIdOrderByPrimaryAddressDescCreatedAtAsc(customerId);

        boolean makePrimary = request.primaryAddress() || currentAddresses.isEmpty();
        if (makePrimary) {
            currentAddresses.forEach(address -> address.setPrimaryAddress(false));
            addressRepository.saveAllAndFlush(currentAddresses);
        }

        CustomerAddress address = addressRepository.save(new CustomerAddress(
                customer,
                request.label().trim(),
                request.street().trim(),
                request.number().trim(),
                normalizeNullable(request.complement()),
                normalizeNullable(request.district()),
                request.city().trim(),
                request.state().trim().toUpperCase(Locale.ROOT),
                normalizeNullable(request.zipCode()),
                request.latitude(),
                request.longitude(),
                makePrimary
        ));

        return toAddressResponse(address);
    }

    @Transactional(readOnly = true)
    public List<AuthorizedRecipientResponse> authorizedRecipients(Long customerId) {
        getCustomer(customerId);
        return recipientRepository.findByCustomerIdOrderByActiveDescNameAsc(customerId)
                .stream()
                .map(this::toRecipientResponse)
                .toList();
    }

    @Transactional
    public AuthorizedRecipientResponse addAuthorizedRecipient(
            Long customerId,
            AuthorizedRecipientCreateRequest request
    ) {
        Customer customer = getCustomer(customerId);
        AuthorizedRecipient recipient = recipientRepository.save(new AuthorizedRecipient(
                customer,
                request.name().trim(),
                request.relationship().trim(),
                normalizeNullable(request.phone())
        ));
        return toRecipientResponse(recipient);
    }

    @Transactional(readOnly = true)
    public DeliveryPreferenceResponse preference(Long customerId) {
        getCustomer(customerId);
        return toPreferenceResponse(preferenceRepository.findByCustomerId(customerId).orElse(null));
    }

    @Transactional
    public DeliveryPreferenceResponse updatePreference(
            Long customerId,
            DeliveryPreferenceUpdateRequest request
    ) {
        Customer customer = getCustomer(customerId);
        validateTimeWindow(request);

        DeliveryPreference preference = preferenceRepository.findByCustomerId(customerId)
                .orElseGet(() -> new DeliveryPreference(customer));

        preference.update(
                request.notificationsEnabled(),
                request.notificationChannel().trim().toUpperCase(Locale.ROOT),
                request.preferredStartTime(),
                request.preferredEndTime(),
                normalizeNullable(request.deliveryInstructions())
        );

        return toPreferenceResponse(preferenceRepository.save(preference));
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderResponse> orders(Long customerId) {
        getCustomer(customerId);
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    private Customer getCustomer(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do cliente é obrigatório.");
        }
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado. ID: " + id));
    }

    private CustomerListResponse toListResponse(Customer customer) {
        List<OrderEntity> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        CustomerStats stats = stats(orders);
        CustomerAddress primaryAddress = addressRepository
                .findByCustomerIdOrderByPrimaryAddressDescCreatedAtAsc(customer.getId())
                .stream()
                .findFirst()
                .orElse(null);

        return new CustomerListResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getRating(),
                primaryAddress == null ? null : primaryAddress.getCity(),
                primaryAddress == null ? null : primaryAddress.getState(),
                stats.totalOrders(),
                stats.activeDeliveries(),
                stats.occurrences(),
                orders.isEmpty() ? null : orders.getFirst().getCreatedAt(),
                stats.totalSpent()
        );
    }

    private CustomerDetailResponse toDetailResponse(Customer customer) {
        List<OrderEntity> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        CustomerStats stats = stats(orders);

        List<CustomerAddressResponse> addresses = addressRepository
                .findByCustomerIdOrderByPrimaryAddressDescCreatedAtAsc(customer.getId())
                .stream()
                .map(this::toAddressResponse)
                .toList();

        List<AuthorizedRecipientResponse> recipients = recipientRepository
                .findByCustomerIdOrderByActiveDescNameAsc(customer.getId())
                .stream()
                .map(this::toRecipientResponse)
                .toList();

        return new CustomerDetailResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getRating(),
                stats.totalOrders(),
                stats.activeDeliveries(),
                stats.occurrences(),
                stats.totalSpent(),
                addresses,
                recipients,
                toPreferenceResponse(preferenceRepository.findByCustomerId(customer.getId()).orElse(null)),
                orders.stream().map(this::toOrderResponse).toList()
        );
    }

    private CustomerStats stats(List<OrderEntity> orders) {
        long activeDeliveries = orders.stream()
                .filter(order -> !TERMINAL_STATUSES.contains(order.getStatus()))
                .count();

        long occurrences = orders.stream()
                .filter(order -> OCCURRENCE_STATUSES.contains(order.getStatus()))
                .count();

        BigDecimal totalSpent = orders.stream()
                .filter(order -> order.getStatus() != DeliveryStatus.CANCELLED)
                .map(OrderEntity::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CustomerStats(orders.size(), activeDeliveries, occurrences, totalSpent);
    }

    private CustomerAddressResponse toAddressResponse(CustomerAddress address) {
        return new CustomerAddressResponse(
                address.getId(),
                address.getLabel(),
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getDistrict(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getLatitude(),
                address.getLongitude(),
                address.isPrimaryAddress()
        );
    }

    private AuthorizedRecipientResponse toRecipientResponse(AuthorizedRecipient recipient) {
        return new AuthorizedRecipientResponse(
                recipient.getId(),
                recipient.getName(),
                recipient.getRelationship(),
                recipient.getPhone(),
                recipient.isActive()
        );
    }

    private DeliveryPreferenceResponse toPreferenceResponse(DeliveryPreference preference) {
        if (preference == null) {
            return new DeliveryPreferenceResponse(true, "EMAIL", null, null, null);
        }
        return new DeliveryPreferenceResponse(
                preference.isNotificationsEnabled(),
                preference.getNotificationChannel(),
                preference.getPreferredStartTime(),
                preference.getPreferredEndTime(),
                preference.getDeliveryInstructions()
        );
    }

    private CustomerOrderResponse toOrderResponse(OrderEntity order) {
        return new CustomerOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTrackingCode(),
                order.getTotal(),
                order.getCreatedAt()
        );
    }

    private void validateTimeWindow(DeliveryPreferenceUpdateRequest request) {
        boolean startMissing = request.preferredStartTime() == null;
        boolean endMissing = request.preferredEndTime() == null;

        if (startMissing != endMissing) {
            throw new IllegalArgumentException("Informe início e fim da janela de recebimento, ou deixe ambos vazios.");
        }

        if (!startMissing && !request.preferredStartTime().isBefore(request.preferredEndTime())) {
            throw new IllegalArgumentException("O início da janela deve ser anterior ao horário final.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private record CustomerStats(
            long totalOrders,
            long activeDeliveries,
            long occurrences,
            BigDecimal totalSpent
    ) {}
}
