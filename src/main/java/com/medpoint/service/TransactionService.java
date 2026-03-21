package com.medpoint.service;
import com.medpoint.dto.request.DispenseIssueRequest;
import com.medpoint.dto.request.ServiceIssueRequest;
import com.medpoint.dto.request.TransactionFilterRequest;
import com.medpoint.dto.response.ServiceReceiptResponse;
import com.medpoint.dto.response.TransactionResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TransactionService {
    List<TransactionResponse> getAll(TransactionFilterRequest filter);
    TransactionResponse getById(Long id);
    TransactionResponse cancel(Long id, Long cancelledByUserId);

    @Transactional
    ServiceReceiptResponse issue(ServiceIssueRequest req, Long staffId);

    @Transactional
    ServiceReceiptResponse dispense(DispenseIssueRequest request, Long staffId); // new

}
