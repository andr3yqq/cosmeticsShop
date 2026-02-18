package com.andr3yqq.cosmeticsshop.payment;

import java.util.List;

public interface PaymentService {
    Payment creetePayment(PaymentDTO paymentDTO);
    Payment updatePayment(PaymentDTO paymentDTO);
    Payment getPaymentById(Long id);
    List<Payment> getAllPayments();
}
