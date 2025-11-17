package com.rpay.infrastructure.persistence.repository;

import com.rpay.domain.customer.Customer;
import com.rpay.domain.customer.CustomerId;
import com.rpay.domain.customer.CustomerRepository;
import com.rpay.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomerRepositoryImpl implements CustomerRepository {
    private final JpaCustomerRepository jpaRepository;

    public CustomerRepositoryImpl(JpaCustomerRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        var entity = toEntity(customer);
        jpaRepository.save(entity);
        return customer;
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        return jpaRepository.findById(customerId.getValue())
            .map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone)
            .map(this::toDomain);
    }

    @Override
    public boolean existsById(CustomerId customerId) {
        return jpaRepository.existsById(customerId.getValue());
    }

    private CustomerEntity toEntity(Customer customer) {
        var entity = new CustomerEntity();
        entity.setId(customer.getId().getValue());
        entity.setEmail(customer.getEmail());
        entity.setPhone(customer.getPhone());
        entity.setName(customer.getName());
        entity.setActive(customer.isActive());
        return entity;
    }

    private Customer toDomain(CustomerEntity entity) {
        return Customer.builder()
            .id(CustomerId.of(entity.getId()))
            .email(entity.getEmail())
            .phone(entity.getPhone())
            .name(entity.getName())
            .build();
    }
}
