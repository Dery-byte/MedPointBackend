package com.medpoint.dto.request;
import com.medpoint.enums.TransactionStatus;
import com.medpoint.enums.TxModule;
import lombok.Data;
import java.time.LocalDate;

/** Query parameters for the transaction history / revenue filter endpoints. */
@Data
public class TransactionFilterRequest {
    private TxModule module;
    private Long staffId;
    private TransactionStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
}
