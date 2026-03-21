package com.medpoint.config;
import com.medpoint.entity.*;
import com.medpoint.enums.*;
import com.medpoint.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Seeds the database with the same initial data as the React frontend constants.js.
 * Only runs when the tables are empty (safe for production re-deploys).
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(
            UserRepository userRepo,
            DrugRepository drugRepo,
            MedicalServiceRepository svcRepo,
            NonDrugItemRepository ndRepo,
            ProductRepository prodRepo,
            RoomCategoryRepository roomCatRepo,
            RoomRepository roomRepo,
            RoomExtraRepository roomExtraRepo,
            RestaurantTableRepository tableRepo,
            MenuItemRepository menuRepo,
            PasswordEncoder encoder) {

        return args -> {
            if (userRepo.count() > 0) {
                log.info("Database already seeded — skipping.");
                return;
            }
            log.info("Seeding database...");

            // ── Users ──────────────────────────────────────────────────────────
            userRepo.saveAll(List.of(
                User.builder().name("Super Admin").email("admin@medpoint.com")
                    .password(encoder.encode("admin123")).role(StaffRole.SUPERADMIN)
                    .accessModules(Set.of(AccessModule.values()))
                    .manageModules(Set.of(ManageModule.values())).active(true).build(),
                User.builder().name("Pharma Manager").email("pharma@medpoint.com")
                    .password(encoder.encode("pass123")).role(StaffRole.MANAGER)
                    .accessModules(Set.of(AccessModule.DRUGSTORE))
                    .manageModules(Set.of(ManageModule.DRUGSTORE)).active(true).build(),
                User.builder().name("Mart Attendant").email("mart@medpoint.com")
                    .password(encoder.encode("pass123")).role(StaffRole.STAFF)
                    .accessModules(Set.of(AccessModule.MART)).active(true).build(),
                User.builder().name("Hotel Receptionist").email("hotel@medpoint.com")
                    .password(encoder.encode("pass123")).role(StaffRole.MANAGER)
                    .accessModules(Set.of(AccessModule.HOTEL))
                    .manageModules(Set.of(ManageModule.HOTEL)).active(true).build(),
                User.builder().name("Restaurant Staff").email("resto@medpoint.com")
                    .password(encoder.encode("pass123")).role(StaffRole.STAFF)
                    .accessModules(Set.of(AccessModule.RESTAURANT)).active(true).build()
            ));

            // ── Drugs ──────────────────────────────────────────────────────────
            drugRepo.saveAll(List.of(
                Drug.builder().name("Paracetamol 500mg").category("Analgesic").price(new BigDecimal("0.50")).stock(120).build(),
                Drug.builder().name("Amoxicillin 250mg").category("Antibiotic").price(new BigDecimal("1.20")).stock(80).build(),
                Drug.builder().name("Ibuprofen 400mg").category("Anti-inflammatory").price(new BigDecimal("0.80")).stock(95).build(),
                Drug.builder().name("Metformin 500mg").category("Antidiabetic").price(new BigDecimal("0.90")).stock(60).build(),
                Drug.builder().name("Omeprazole 20mg").category("Antacid").price(new BigDecimal("1.50")).stock(45).build(),
                Drug.builder().name("Chloroquine 250mg").category("Antimalarial").price(new BigDecimal("1.00")).stock(8).expiryDate(LocalDate.now().plusDays(25)).build(),
                Drug.builder().name("Ciprofloxacin 500mg").category("Antibiotic").price(new BigDecimal("2.00")).stock(55).build(),
                Drug.builder().name("Vitamin C 1000mg").category("Supplement").price(new BigDecimal("0.60")).stock(200).build(),
                Drug.builder().name("Zinc Sulfate 20mg").category("Supplement").price(new BigDecimal("0.70")).stock(150).build(),
                Drug.builder().name("Folic Acid 5mg").category("Supplement").price(new BigDecimal("0.40")).stock(180).build(),
                Drug.builder().name("Doxycycline 100mg").category("Antibiotic").price(new BigDecimal("1.80")).stock(7).expiryDate(LocalDate.now().plusDays(60)).build(),
                Drug.builder().name("Aspirin 75mg").category("Analgesic").price(new BigDecimal("0.30")).stock(300).build(),
                Drug.builder().name("Metronidazole 200mg").category("Antibiotic").price(new BigDecimal("0.85")).stock(90).build(),
                Drug.builder().name("Artemether 20mg").category("Antimalarial").price(new BigDecimal("2.50")).stock(5).build(),
                Drug.builder().name("Lisinopril 5mg").category("Antihypertensive").price(new BigDecimal("1.10")).stock(40).build(),
                Drug.builder().name("Atenolol 50mg").category("Antihypertensive").price(new BigDecimal("0.95")).stock(35).build()
            ));

            // ── Medical Services ───────────────────────────────────────────────
            svcRepo.saveAll(List.of(
                MedicalService.builder().name("General Consultation").category("Consultation").price(new BigDecimal("10.00")).build(),
                MedicalService.builder().name("Blood Pressure Check").category("Diagnostic").price(new BigDecimal("5.00")).build(),
                MedicalService.builder().name("Blood Sugar Test").category("Diagnostic").price(new BigDecimal("8.00")).build(),
                MedicalService.builder().name("Malaria RDT Test").category("Diagnostic").price(new BigDecimal("6.00")).build(),
                MedicalService.builder().name("Pregnancy Test").category("Diagnostic").price(new BigDecimal("5.00")).build(),
                MedicalService.builder().name("Wound Dressing").category("Treatment").price(new BigDecimal("15.00")).build(),
                MedicalService.builder().name("Injection Administration").category("Treatment").price(new BigDecimal("3.00")).build(),
                MedicalService.builder().name("IV Drip Setup").category("Treatment").price(new BigDecimal("20.00")).build(),
                MedicalService.builder().name("Eye Examination").category("Consultation").price(new BigDecimal("12.00")).build(),
                MedicalService.builder().name("Ear Irrigation").category("Treatment").price(new BigDecimal("8.00")).build()
            ));

            // ── Non-Drug Items ─────────────────────────────────────────────────
            ndRepo.saveAll(List.of(
                NonDrugItem.builder().name("Gloves (pair)").category("Consumable").price(new BigDecimal("0.50")).build(),
                NonDrugItem.builder().name("Syringe 5ml").category("Consumable").price(new BigDecimal("0.30")).build(),
                NonDrugItem.builder().name("IV Cannula").category("Consumable").price(new BigDecimal("1.00")).build(),
                NonDrugItem.builder().name("Gauze Roll").category("Consumable").price(new BigDecimal("0.80")).build(),
                NonDrugItem.builder().name("Bandage 5cm").category("Consumable").price(new BigDecimal("0.60")).build(),
                NonDrugItem.builder().name("Alcohol Swabs (10)").category("Consumable").price(new BigDecimal("0.50")).build(),
                NonDrugItem.builder().name("IV Fluid 500ml").category("Fluid").price(new BigDecimal("3.50")).build(),
                NonDrugItem.builder().name("Blood Collection Tube").category("Diagnostic").price(new BigDecimal("1.20")).build()
            ));

            // ── Mart Products ──────────────────────────────────────────────────
            prodRepo.saveAll(List.of(
                Product.builder().name("Jasmine Rice 5kg").category("Groceries").price(new BigDecimal("8.50")).stock(40).build(),
                Product.builder().name("Cooking Oil 2L").category("Groceries").price(new BigDecimal("4.20")).stock(25).build(),
                Product.builder().name("White Sugar 1kg").category("Groceries").price(new BigDecimal("1.80")).stock(60).build(),
                Product.builder().name("Table Salt 500g").category("Groceries").price(new BigDecimal("0.70")).stock(8).build(),
                Product.builder().name("Wheat Flour 2kg").category("Groceries").price(new BigDecimal("3.50")).stock(30).build(),
                Product.builder().name("Coca-Cola 1.5L").category("Beverages").price(new BigDecimal("2.50")).stock(48).build(),
                Product.builder().name("Malt Drink 330ml").category("Beverages").price(new BigDecimal("1.20")).stock(9).build(),
                Product.builder().name("Bottled Water 1.5L").category("Beverages").price(new BigDecimal("0.80")).stock(100).build(),
                Product.builder().name("Fruit Juice 1L").category("Beverages").price(new BigDecimal("2.80")).stock(35).build(),
                Product.builder().name("Bar Soap 200g").category("Household").price(new BigDecimal("1.50")).stock(6).build(),
                Product.builder().name("Washing Powder 1kg").category("Household").price(new BigDecimal("3.20")).stock(22).build(),
                Product.builder().name("Bleach 1L").category("Household").price(new BigDecimal("1.80")).stock(18).build(),
                Product.builder().name("Toothpaste 150g").category("Personal Care").price(new BigDecimal("2.20")).stock(30).build(),
                Product.builder().name("Shampoo 400ml").category("Personal Care").price(new BigDecimal("4.50")).stock(4).build(),
                Product.builder().name("Body Lotion 400ml").category("Personal Care").price(new BigDecimal("5.00")).stock(15).build(),
                Product.builder().name("USB Charger").category("Electronics").price(new BigDecimal("6.00")).stock(12).build(),
                Product.builder().name("Earphones").category("Electronics").price(new BigDecimal("8.00")).stock(7).build(),
                Product.builder().name("Power Bank 10000mAh").category("Electronics").price(new BigDecimal("22.00")).stock(5).build()
            ));

            // ── Room Categories & Rooms ────────────────────────────────────────
            RoomCategory standard = roomCatRepo.save(RoomCategory.builder().name("Standard").pricePerNight(new BigDecimal("50.00")).build());
            RoomCategory deluxe   = roomCatRepo.save(RoomCategory.builder().name("Deluxe").pricePerNight(new BigDecimal("80.00")).build());
            RoomCategory suite    = roomCatRepo.save(RoomCategory.builder().name("Suite").pricePerNight(new BigDecimal("150.00")).build());

            roomRepo.saveAll(List.of(
                Room.builder().roomNumber("101").category(standard).build(),
                Room.builder().roomNumber("102").category(standard).build(),
                Room.builder().roomNumber("103").category(standard).build(),
                Room.builder().roomNumber("104").category(standard).build(),
                Room.builder().roomNumber("201").category(deluxe).build(),
                Room.builder().roomNumber("202").category(deluxe).build(),
                Room.builder().roomNumber("203").category(deluxe).build(),
                Room.builder().roomNumber("204").category(deluxe).build(),
                Room.builder().roomNumber("301").category(suite).build(),
                Room.builder().roomNumber("302").category(suite).build()
            ));

            // ── Room Extras ────────────────────────────────────────────────────
            roomExtraRepo.saveAll(List.of(
                RoomExtra.builder().name("Room Service Meal").price(new BigDecimal("12.00")).build(),
                RoomExtra.builder().name("Laundry Service").price(new BigDecimal("8.00")).build(),
                RoomExtra.builder().name("Airport Shuttle").price(new BigDecimal("25.00")).build(),
                RoomExtra.builder().name("Minibar Drinks").price(new BigDecimal("15.00")).build(),
                RoomExtra.builder().name("Spa Treatment").price(new BigDecimal("40.00")).build(),
                RoomExtra.builder().name("Extra Towels").price(new BigDecimal("3.00")).build(),
                RoomExtra.builder().name("Breakfast (1 person)").price(new BigDecimal("10.00")).build(),
                RoomExtra.builder().name("Late Checkout Fee").price(new BigDecimal("20.00")).build()
            ));

            // ── Restaurant Tables ──────────────────────────────────────────────
            for (int i = 1; i <= 8; i++) {
                tableRepo.save(RestaurantTable.builder().tableNumber(i).build());
            }

            // ── Menu Items ─────────────────────────────────────────────────────
            menuRepo.saveAll(List.of(
                MenuItem.builder().name("Spring Rolls").category("Starters").type(MenuItemType.FOOD).price(new BigDecimal("3.50")).build(),
                MenuItem.builder().name("Chicken Wings").category("Starters").type(MenuItemType.FOOD).price(new BigDecimal("5.00")).build(),
                MenuItem.builder().name("Garden Salad").category("Starters").type(MenuItemType.FOOD).price(new BigDecimal("4.00")).build(),
                MenuItem.builder().name("Jollof Rice & Chicken").category("Main Course").type(MenuItemType.FOOD).price(new BigDecimal("8.50")).build(),
                MenuItem.builder().name("Grilled Tilapia & Rice").category("Main Course").type(MenuItemType.FOOD).price(new BigDecimal("10.00")).build(),
                MenuItem.builder().name("Fried Rice & Beef").category("Main Course").type(MenuItemType.FOOD).price(new BigDecimal("9.00")).build(),
                MenuItem.builder().name("Banku & Okro Stew").category("Main Course").type(MenuItemType.FOOD).price(new BigDecimal("7.00")).build(),
                MenuItem.builder().name("Fufu & Light Soup").category("Main Course").type(MenuItemType.FOOD).price(new BigDecimal("6.50")).build(),
                MenuItem.builder().name("Club Sandwich").category("Snacks").type(MenuItemType.FOOD).price(new BigDecimal("5.50")).build(),
                MenuItem.builder().name("Meat Pie").category("Snacks").type(MenuItemType.FOOD).price(new BigDecimal("2.50")).build(),
                MenuItem.builder().name("Chocolate Cake").category("Desserts").type(MenuItemType.FOOD).price(new BigDecimal("4.00")).build(),
                MenuItem.builder().name("Ice Cream (2 scoops)").category("Desserts").type(MenuItemType.FOOD).price(new BigDecimal("3.50")).build(),
                MenuItem.builder().name("Coca-Cola").category("Soft Drinks").type(MenuItemType.DRINK).price(new BigDecimal("2.00")).build(),
                MenuItem.builder().name("Sprite").category("Soft Drinks").type(MenuItemType.DRINK).price(new BigDecimal("2.00")).build(),
                MenuItem.builder().name("Bottled Water").category("Soft Drinks").type(MenuItemType.DRINK).price(new BigDecimal("1.00")).build(),
                MenuItem.builder().name("Malt Drink").category("Soft Drinks").type(MenuItemType.DRINK).price(new BigDecimal("2.50")).build(),
                MenuItem.builder().name("Fresh Fruit Juice").category("Soft Drinks").type(MenuItemType.DRINK).price(new BigDecimal("4.00")).build(),
                MenuItem.builder().name("Beer (330ml)").category("Alcoholic").type(MenuItemType.DRINK).price(new BigDecimal("5.00")).build(),
                MenuItem.builder().name("Red Wine (glass)").category("Alcoholic").type(MenuItemType.DRINK).price(new BigDecimal("8.00")).build(),
                MenuItem.builder().name("Whiskey (shot)").category("Alcoholic").type(MenuItemType.DRINK).price(new BigDecimal("10.00")).build(),
                MenuItem.builder().name("Gin & Tonic").category("Alcoholic").type(MenuItemType.DRINK).price(new BigDecimal("7.00")).build(),
                MenuItem.builder().name("Espresso").category("Hot Drinks").type(MenuItemType.DRINK).price(new BigDecimal("3.00")).build(),
                MenuItem.builder().name("English Tea").category("Hot Drinks").type(MenuItemType.DRINK).price(new BigDecimal("2.00")).build(),
                MenuItem.builder().name("Hot Chocolate").category("Hot Drinks").type(MenuItemType.DRINK).price(new BigDecimal("3.50")).build()
            ));

            log.info("Database seeding complete.");
        };
    }
}
