package com.eliasnogueira.paymentservice.unit;

import com.eliasnogueira.paymentservice.exceptions.PaymentLimitException;
import com.eliasnogueira.paymentservice.validator.PaymentLimitValidator;
import java.math.BigDecimal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class PaymentLimitValidatorTest {

  @Test
  public void testIsWithinLimitValidValue() {
    BigDecimal amount = new BigDecimal("1999.00");

    boolean result = PaymentLimitValidator.isWithinLimit(amount);

    Assertions.assertThat(result).isTrue();
  }

  @Test
  public void testIsWithinLimitExceedsLimit() {
    BigDecimal amount = new BigDecimal("2000.01");

    boolean result = PaymentLimitValidator.isWithinLimit(amount);

    Assertions.assertThat(result).isFalse();
  }

  @Test
  public void testIsWithinLimitNegativeValue() {
    BigDecimal amount = new BigDecimal("-5.00");

    Assertions.assertThatThrownBy(() -> PaymentLimitValidator.isWithinLimit(amount))
        .isInstanceOf(PaymentLimitException.class)
        .hasMessage("Amount must be greater than zero");
  }

   @Test
  public void testIsWithinLimitZeroValue() {
     BigDecimal amount = BigDecimal.ZERO;

     Assertions.assertThatThrownBy(() -> PaymentLimitValidator.isWithinLimit(amount))
         .isInstanceOf(PaymentLimitException.class)
         .hasMessage("Amount must be greater than zero");
   }

}
