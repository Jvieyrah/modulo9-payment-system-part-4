package com.eliasnogueira.paymentservice.integration;

import com.eliasnogueira.paymentservice.model.Payment;
import com.eliasnogueira.paymentservice.model.enums.PaymentSource;
import com.eliasnogueira.paymentservice.model.enums.PaymentStatus;
import com.eliasnogueira.paymentservice.repository.PaymentRepository;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    public void cleanUpDatabase() {
        paymentRepository.deleteAll();
    }


    @Test
    @DisplayName("Should create a payment successfully")
    void shouldPostPaymentSuccessfully() throws Exception {
        String json = """
        {
            "payerId": "123e4567-e89b-12d3-a456-426614174000",
            "paymentSource": "PIX",
            "amount": 100.00
        }
        """ ;

     mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(json))
             .andExpect(status().isCreated())
             .andExpect(jsonPath("$.payerId").value("123e4567-e89b-12d3-a456-426614174000"))
             .andExpect(jsonPath("$.amount").value(100.00))
             .andExpect(jsonPath("$.status").value(PaymentStatus.PENDING.toString()))
             .andExpect(jsonPath("$.paymentSource").value(PaymentSource.PIX.toString()));
    }


    @Test
    @DisplayName("Should get a payment successfully")
    void shouldGetePaymentSuccessfully() throws Exception {
        var payment = Payment.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.CREDIT_CARD)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .build();

        var savedPayment = paymentRepository.save(payment);

        mockMvc.perform(get("/api/payments/" + savedPayment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payerId").value(notNullValue()))
                .andExpect(jsonPath("$.payerId").value(savedPayment.getPayerId().toString()))
                .andExpect(jsonPath("$.amount").value(savedPayment.getAmount().doubleValue()))
                .andExpect(jsonPath("$.status").value(savedPayment.getStatus().toString()))
                .andExpect(jsonPath("$.paymentSource").value(savedPayment.getPaymentSource().toString()));


    }


    @Test
    @DisplayName("Should return 404 when a payment is not found")
    void shouldntGetePaymentSuccessfully() throws Exception {


        mockMvc.perform(get("/api/payments/1010" ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Payment not found with ID: 1010"));
    }

    @Test
    @DisplayName("Should return  a payment by payerId")
    void shouldGetPaymentsByPayerIdSuccessfully() throws Exception {
        var payment = Payment.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.CREDIT_CARD)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .build();

        var savedPayment = paymentRepository.save(payment);

        mockMvc.perform(get("/api/payments/payer/" + savedPayment.getPayerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].payerId").value(notNullValue()))
                .andExpect(jsonPath("$[0].payerId").value(savedPayment.getPayerId().toString()))
                .andExpect(jsonPath("$[0].amount").value(savedPayment.getAmount().doubleValue()))
                .andExpect(jsonPath("$[0].status").value(savedPayment.getStatus().toString()))
                .andExpect(jsonPath("$[0].paymentSource").value(savedPayment.getPaymentSource().toString()));

    }

    @Test
    @DisplayName("Should update a paayment status to paid")
    void shouldUpdatePaymentStatusToPaidSuccessfully() throws Exception {
        var payment = Payment.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.CREDIT_CARD)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .build();

        var savedPayment = paymentRepository.save(payment);

        String json = """
        {
            "status": "PAID"
        }
        """;

        mockMvc.perform(put("/api/payments/" + savedPayment.getId()).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.id").value(savedPayment.getId().toString()))
                .andExpect(jsonPath("$.status").value(PaymentStatus.PAID.toString()));

    }

    @Test
    @DisplayName("Should return all payments")
    void shouldGetAllPaymentsSuccessfully() throws Exception {
        UUID payerId = UUID.randomUUID();

        var payment1 = Payment.builder()
                .payerId(payerId)
                .paymentSource(PaymentSource.CREDIT_CARD)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .build();

        var payment2 =  Payment.builder()
                .payerId(payerId)
                .paymentSource(PaymentSource.PIX)
                .amount(new BigDecimal("200.00"))
                .status(PaymentStatus.PAID)
                .build();

        var payment3 = Payment.builder()
                .payerId(payerId)
                .paymentSource(PaymentSource.DEBIT_CARD)
                .amount(new BigDecimal("300.00"))
                .status(PaymentStatus.PAID)
                .build();

        var savedPayment1 = paymentRepository.save(payment1);
        var savedPayment2 = paymentRepository.save(payment2);
        var savedPayment3 = paymentRepository.save(payment3);

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$",hasSize(3)))
                .andExpect(jsonPath("$[*].payerId", everyItem(is(payerId.toString()))))
                .andExpect(jsonPath("$[*].id", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].amount", containsInAnyOrder(
                        100.0, 200.0, 300.0)));
    }
}
