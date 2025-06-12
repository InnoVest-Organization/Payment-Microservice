package com.example.Payment_Microservice.Repository;

import com.example.Payment_Microservice.Entity.Payment;
import com.stripe.model.AccountSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Custom query method to find Payment by paymentIntentId
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

//    AccountSession.Components.PaymentDetails findBySessionId(String paymentIntentId);
}
