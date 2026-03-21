package com.medpoint.controller;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.entity.User;
import com.medpoint.service.DrugstoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/drugstore")
@RequiredArgsConstructor
public class DrugstoreController {

    private final DrugstoreService drugstoreService;

    // ── Drugs ─────────────────────────────────────────────────────────────────

    @GetMapping("/drugs")
    public ResponseEntity<List<DrugResponse>> getAllDrugs() {
        return ResponseEntity.ok(drugstoreService.getAllDrugs());
    }

    @PostMapping("/drugs")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
    public ResponseEntity<DrugResponse> createDrug(@Valid @RequestBody DrugRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(drugstoreService.createDrug(request));
    }

    @PutMapping("/drugs/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
    public ResponseEntity<DrugResponse> updateDrug(@PathVariable Long id,
                                                    @Valid @RequestBody DrugRequest request) {
        return ResponseEntity.ok(drugstoreService.updateDrug(id, request));
    }

//    @DeleteMapping("/drugs/{id}")
//    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
//    public ResponseEntity<ApiResponse<D>> deleteDrug(@PathVariable Long id) {
//        drugstoreService.deleteDrug(id);
//        return ResponseEntity.ok(ApiResponse.ok("Drug deleted successfully."));
//    }

    @DeleteMapping("/drugs/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteDrug(@PathVariable Long id) {
        drugstoreService.deleteDrug(id);
        return ResponseEntity.ok(ApiResponse.ok("Drug deleted successfully."));
    }

    /** PATCH /drugstore/drugs/{id}/restock — matches frontend: restockDrug() */
    @PatchMapping("/drugs/{id}/restock")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
    public ResponseEntity<DrugResponse> restockDrug(@PathVariable Long id,
                                                     @Valid @RequestBody RestockRequest request) {
        return ResponseEntity.ok(drugstoreService.restockDrug(id, request));
    }

    @PatchMapping("/drugs/{id}/price")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
    public ResponseEntity<DrugResponse> updateDrugPrice(@PathVariable Long id,
                                                         @Valid @RequestBody PriceUpdateRequest request) {
        return ResponseEntity.ok(drugstoreService.updateDrugPrice(id, request));
    }

    // ── Non-Drug Items ────────────────────────────────────────────────────────
    // URL: /drugstore/non-drug-items — matches frontend drugstoreService paths

    @GetMapping("/non-drug-items")
    public ResponseEntity<List<NonDrugItemResponse>> getAllNonDrugItems() {
        return ResponseEntity.ok(drugstoreService.getAllNonDrugItems());
    }

    @PostMapping("/non-drug-items")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
    public ResponseEntity<NonDrugItemResponse> createNonDrugItem(@Valid @RequestBody NonDrugItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(drugstoreService.createNonDrugItem(request));
    }

    @PutMapping("/non-drug-items/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
    public ResponseEntity<NonDrugItemResponse> updateNonDrugItem(@PathVariable Long id,
                                                                   @Valid @RequestBody NonDrugItemRequest request) {
        return ResponseEntity.ok(drugstoreService.updateNonDrugItem(id, request));
    }

//    @DeleteMapping("/non-drug-items/{id}")
//    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
//    public ResponseEntity<ApiResponse<D>> deleteNonDrugItem(@PathVariable Long id) {
//        drugstoreService.deleteNonDrugItem(id);
//        return ResponseEntity.ok(ApiResponse.ok("Item deleted successfully."));
//    }

    @DeleteMapping("/non-drug-items/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteNonDrugItem(@PathVariable Long id) {
        drugstoreService.deleteNonDrugItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Item deleted successfully."));
    }
    // ── Medical Services ──────────────────────────────────────────────────────

    @GetMapping("/services")
    public ResponseEntity<List<MedicalServiceResponse>> getAllServices() {
        return ResponseEntity.ok(drugstoreService.getAllServices());
    }

    @PostMapping("/services")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
    public ResponseEntity<MedicalServiceResponse> createService(@Valid @RequestBody MedicalServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(drugstoreService.createService(request));
    }

    @PutMapping("/services/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
    public ResponseEntity<MedicalServiceResponse> updateService(@PathVariable Long id,
                                                                 @Valid @RequestBody MedicalServiceRequest request) {
        return ResponseEntity.ok(drugstoreService.updateService(id, request));
    }

//    @DeleteMapping("/services/{id}")
//    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
//    public ResponseEntity<ApiResponse<D>> deleteService(@PathVariable Long id) {
//        drugstoreService.deleteService(id);
//        return ResponseEntity.ok(ApiResponse.ok("Service deleted successfully."));
//    }


    @DeleteMapping("/services/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageDrugstore(authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        drugstoreService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.ok("Service deleted successfully."));
    }

    // ── Dispense ──────────────────────────────────────────────────────────────

    /** POST /drugstore/dispense — matches frontend: dispenseDrugs() */
    @PostMapping("/dispense")
    public ResponseEntity<TransactionResponse> dispense(@Valid @RequestBody DrugDispenseRequest request,
                                                        @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(drugstoreService.dispense(request, currentUser.getId()));
    }

    /** POST /drugstore/dispense-service — matches frontend: dispenseService() */
    @PostMapping("/dispense-service")
    public ResponseEntity<TransactionResponse> dispenseService(@Valid @RequestBody ServiceDispenseRequest request,
                                                                @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(drugstoreService.dispenseService(request, currentUser.getId()));
    }
}
