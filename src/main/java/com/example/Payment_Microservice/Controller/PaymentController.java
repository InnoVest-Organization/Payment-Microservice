package com.example.Payment_Microservice.Controller;

import com.example.Payment_Microservice.Service.PaymentService;
import com.example.Payment_Microservice.Entity.Payment;
import com.example.Payment_Microservice.Repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public PaymentController(PaymentRepository paymentRepository, PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    private static final Map<String, Integer> PACKAGE_PRICES = new HashMap<>() {{
        put("Basic", 100);
        put("Standard", 250);
        put("Premium", 500);
    }};

    @PostMapping("/{invention_id}")
    public ResponseEntity<Map<String, String>> createPayment(
            @PathVariable("invention_id") String inventionId,
            @RequestBody Map<String, Object> request) {

        Stripe.apiKey = stripeSecretKey;

//        String inventionId = (String) request.get("Invention_ID");
        String packageName = (String) request.get("Payment_Package");
        String inventorEmail = (String) request.get("Inventor_Email");

        if (!PACKAGE_PRICES.containsKey(packageName)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid package name."));
        }

        int amount = PACKAGE_PRICES.get(packageName) * 100; // Convert to cents

        try {
            // Create Stripe Checkout session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:5003/api/payments/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:5003/api/payments/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount((long) amount)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(packageName + " Package")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            // Create and save payment record
            Payment payment = new Payment();
            payment.setInventionId(inventionId);
            payment.setPackageName(packageName);
            payment.setAmount(amount / 100.0);
            payment.setStatus("PENDING");
            payment.setPaymentIntentId(session.getId());
            payment.setPaymentDatetime(LocalDateTime.now());
            payment.setInventorEmail(inventorEmail);

            paymentRepository.save(payment);

            // Return session URL for frontend redirection
            return ResponseEntity.ok(Map.of(
                    "session_id", session.getId(),
                    "session_url", session.getUrl()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    //Handle successful payment redirection
    @GetMapping("/success")
    public ResponseEntity<Map<String, String>> handlePaymentSuccess(@RequestParam("session_id") String sessionId) {
        try {
            Stripe.apiKey = stripeSecretKey;
            Session session = Session.retrieve(sessionId);

            if (session == null || !"complete".equals(session.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or incomplete session."));
            }

            boolean updated = paymentService.updatePaymentStatus(sessionId, "SUCCESS");
            if (!updated) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment status update failed."));
            }

            return ResponseEntity.ok(Map.of("message", "Payment successful!", "status", "SUCCESS"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }

    //Handle canceled payment redirection
    @GetMapping("/cancel")
    public ResponseEntity<Map<String, String>> handlePaymentCancel(@RequestParam(value = "session_id", required = false) String sessionId) {
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "Payment was cancelled!", "status", "CANCELLED_NO_SESSION"));
            }

            Stripe.apiKey = stripeSecretKey;
            Session session = Session.retrieve(sessionId);

            boolean updated = paymentService.updatePaymentStatus(sessionId, "CANCELLED");

            if (!updated) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid session or status already updated."));
            }

            return ResponseEntity.ok(Map.of("message", "Payment was cancelled!", "status", "CANCELLED"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Cancellation failed: " + e.getMessage()));
        }
    }
}
