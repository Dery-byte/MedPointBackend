package com.medpoint.service;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import java.util.List;

public interface DrugstoreService {
    // Drugs
    List<DrugResponse> getAllDrugs();
    DrugResponse getDrugById(Long id);
    DrugResponse createDrug(DrugRequest request);
    DrugResponse updateDrug(Long id, DrugRequest request);
    void deleteDrug(Long id);
    DrugResponse restockDrug(Long id, RestockRequest request);
    DrugResponse updateDrugPrice(Long id, PriceUpdateRequest request);

    // Medical Services
    List<MedicalServiceResponse> getAllServices();
    MedicalServiceResponse createService(MedicalServiceRequest request);
    MedicalServiceResponse updateService(Long id, MedicalServiceRequest request);
    void deleteService(Long id);
    MedicalServiceResponse updateServicePrice(Long id, PriceUpdateRequest request);

    // Non-Drug Items
    List<NonDrugItemResponse> getAllNonDrugItems();
    NonDrugItemResponse createNonDrugItem(NonDrugItemRequest request);
    NonDrugItemResponse updateNonDrugItem(Long id, NonDrugItemRequest request);
    void deleteNonDrugItem(Long id);
    NonDrugItemResponse updateNonDrugItemPrice(Long id, PriceUpdateRequest request);

    // Dispense operations
    TransactionResponse dispense(DrugDispenseRequest request, Long staffId);
    TransactionResponse dispenseService(ServiceDispenseRequest request, Long staffId);
}
