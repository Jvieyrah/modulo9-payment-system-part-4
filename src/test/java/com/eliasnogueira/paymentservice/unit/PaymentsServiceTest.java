package com.eliasnogueira.paymentservice.unit;

import com.eliasnogueira.paymentservice.dto.PaymentRequest;
import com.eliasnogueira.paymentservice.exceptions.PaymentLimitException;
import com.eliasnogueira.paymentservice.model.Payment;
import com.eliasnogueira.paymentservice.model.enums.PaymentSource;
import com.eliasnogueira.paymentservice.repository.PaymentRepository;
import com.eliasnogueira.paymentservice.service.PaymentService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentsServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Should save the payment when daily limit is not reached")
    public void testCreatePaymentSuccess() {
        when(paymentRepository.sumPaymentsByPayerIdAndDate(any(), any(), any()))
                .thenReturn(new BigDecimal("200.00"));

        when(paymentRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var paymentRequest = PaymentRequest.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.CREDIT_CARD)
                .amount(BigDecimal.valueOf(200.00)).build();

        var savedPayment = paymentService.createPayment(paymentRequest);

        assertThat(savedPayment.getPayerId()).isEqualTo(paymentRequest.getPayerId());
        verify(paymentRepository).save(any());


    }


    @Test
    @DisplayName("Should thrown an exception when the daily limit exceed")
    public void testCreatePaymentOverLimit() {
        when(paymentRepository.sumPaymentsByPayerIdAndDate(any(), any(), any()))
                .thenReturn(new BigDecimal("8000.00"));
        var paymentRequest = PaymentRequest.builder().payerId(UUID.randomUUID()).paymentSource(PaymentSource.CREDIT_CARD)
                .amount(BigDecimal.valueOf(2100.00)).build();

        assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessageContaining("Daily payment limit exceeded");
    }

    @Test
    @DisplayName("Should thrown an exception the amount is zero")
    void shouldThrownExceptionWhenAmountIsZero() {
        var paymentRequest = PaymentRequest.builder().payerId(UUID.randomUUID()).paymentSource(PaymentSource.CREDIT_CARD)
                .amount(BigDecimal.ZERO).build();

        assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }

}
