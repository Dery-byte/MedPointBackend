package com.medpoint.dto.request;

import com.medpoint.enums.TxModule;
import lombok.Data;

import java.time.LocalDate;

/** Query parameters for admin report generation. */
@Data
public class ReportRequest {
    private TxModule  module;    // null = all modules
    private Long      staffId;   // null = all staff
    private LocalDate fromDate;
    private LocalDate toDate;
    /** daily | monthly | staff | category | product  (default: daily) */
    private String    groupBy;
}
