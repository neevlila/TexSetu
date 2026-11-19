package com.texsetu.payment;

import com.texsetu.entity.JobOrder;
import com.texsetu.entity.PaymentRecord;
import com.texsetu.entity.User;
import com.texsetu.entity.enums.PaymentStatus;

import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentRecord initiateSimulatedPayment(JobOrder order, User payer, BigDecimal amount);
    PaymentRecord completeSimulatedPayment(String transactionReference, boolean shouldSucceed);
    PaymentRecord processSimulatedRefund(String transactionReference, String refundReason);
}
