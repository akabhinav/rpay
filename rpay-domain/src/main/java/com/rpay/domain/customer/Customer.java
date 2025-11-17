package com.rpay.domain.customer;

import com.rpay.domain.common.BaseEntity;

/**
 * Customer entity represents an end-user making payments.
 */
public class Customer extends BaseEntity<CustomerId> {
    private String email;
    private String phone;
    private String name;
    private boolean active;

    private Customer(Builder builder) {
        super(builder.id != null ? builder.id : CustomerId.generate());
        this.email = builder.email;
        this.phone = builder.phone;
        this.name = builder.name;
        this.active = true;

        validate();
    }

    private void validate() {
        if (email == null && phone == null) {
            throw new IllegalArgumentException("Customer must have either email or phone");
        }
    }

    public void updateContact(String email, String phone) {
        if (email == null && phone == null) {
            throw new IllegalArgumentException("At least one contact method required");
        }
        this.email = email;
        this.phone = phone;
        markAsUpdated();
    }

    public void deactivate() {
        this.active = false;
        markAsUpdated();
    }

    public void activate() {
        this.active = true;
        markAsUpdated();
    }

    // Getters
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getName() { return name; }
    public boolean isActive() { return active; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CustomerId id;
        private String email;
        private String phone;
        private String name;

        public Builder id(CustomerId id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Customer build() {
            return new Customer(this);
        }
    }
}
