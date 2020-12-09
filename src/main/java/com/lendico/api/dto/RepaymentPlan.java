package com.lendico.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepaymentPlan {
    private final List<BorrowerPayment> borrowerPayments;
}
