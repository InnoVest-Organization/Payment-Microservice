package com.example.Payment_Microservice.Service;

import com.example.Payment_Microservice.Entity.Payment;
import com.example.Payment_Microservice.Repository.PaymentRepository;
import com.stripe.model.AccountSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.emailService = emailService;
    }

    public boolean updatePaymentStatus(String sessionId, String status) {
        Payment payment = paymentRepository.findByPaymentIntentId(sessionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(status);
        paymentRepository.save(payment);

        sendEmailNotification(payment, status);

        return true;
    }


    private void sendEmailNotification(Payment payment, String status) {
        String email = payment.getInventorEmail();

        String subject = status.equalsIgnoreCase("SUCCESS") ? "Payment Successful" : "Payment Failed";
        String body = "Dear Customer, \n\nYour payment for the " + payment.getPackageName() +
                " package has been " + status + ".\n\nThank you for using our service!";

        emailService.sendEmail(email, subject, body);
    }

    public Payment getPaymentDetailsBySessionId(String sessionId) {
        return paymentRepository.findByPaymentIntentId(sessionId).orElse(null);
    }
}
