package com.example.Payment_Microservice.Entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invention_id", nullable = false)
    private String inventionId;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "currency", nullable = false)
    private String currency = "USD";

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    @Column(name = "inventor_email")
    private String inventorEmail;

    @Column(name = "payment_datetime", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDatetime;

}
