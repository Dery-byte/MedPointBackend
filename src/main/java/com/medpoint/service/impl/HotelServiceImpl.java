package com.medpoint.service.impl;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.entity.*;
import com.medpoint.enums.*;
import com.medpoint.exception.*;
import com.medpoint.repository.*;
import com.medpoint.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final RoomCategoryRepository catRepo;
    private final RoomRepository roomRepo;
    private final BookingRepository bookingRepo;
    private final RoomExtraRepository extraRepo;
    private final UserRepository userRepo;
    private final TransactionRepository txRepo;

    // ── Room Categories ───────────────────────────────────────────────────────

    @Override
    public List<RoomCategoryResponse> getAllCategories() {
        return catRepo.findAll().stream().map(cat -> {
            List<Room> rooms = roomRepo.findByCategory(cat);
            long occ = rooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
            return RoomCategoryResponse.builder()
                    .id(cat.getId()).name(cat.getName()).pricePerNight(cat.getPricePerNight())
                    .totalRooms(rooms.size()).occupiedRooms((int) occ)
                    .availableRooms(rooms.size() - (int) occ).build();
        }).toList();
    }

    @Override @Transactional
    public RoomCategoryResponse createCategory(RoomCategoryRequest req) {
        if (catRepo.existsByName(req.getName()))
            throw new BusinessException("Category already exists: " + req.getName());
        RoomCategory cat = catRepo.save(RoomCategory.builder()
                .name(req.getName()).pricePerNight(req.getPricePerNight()).build());
        return RoomCategoryResponse.builder()
                .id(cat.getId()).name(cat.getName()).pricePerNight(cat.getPricePerNight())
                .totalRooms(0).occupiedRooms(0).availableRooms(0).build();
    }

    @Override @Transactional
    public RoomCategoryResponse updateCategoryPrice(Long id, UpdateRoomCategoryRequest req) {
        RoomCategory cat = catRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomCategory", id));
        cat.setPricePerNight(req.getPricePerNight());
        catRepo.save(cat);
        List<Room> rooms = roomRepo.findByCategory(cat);
        long occ = rooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
        return RoomCategoryResponse.builder()
                .id(cat.getId()).name(cat.getName()).pricePerNight(cat.getPricePerNight())
                .totalRooms(rooms.size()).occupiedRooms((int) occ)
                .availableRooms(rooms.size() - (int) occ).build();
    }

    @Override @Transactional
    public void deleteCategory(Long id) {
        RoomCategory cat = catRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomCategory", id));
        if (!roomRepo.findByCategory(cat).isEmpty())
            throw new BusinessException("Cannot delete a category that has rooms assigned.");
        catRepo.delete(cat);
    }

    // ── Rooms ─────────────────────────────────────────────────────────────────

    @Override
    public List<RoomResponse> getAllRooms() {
        return roomRepo.findAllByOrderByRoomNumberAsc().stream().map(this::toRoomResponse).toList();
    }

    @Override @Transactional
    public RoomResponse addRoom(AddRoomRequest req) {
        if (roomRepo.existsByRoomNumber(req.getRoomNumber()))
            throw new BusinessException("Room already exists: " + req.getRoomNumber());
        RoomCategory cat = catRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("RoomCategory", req.getCategoryId()));
        return toRoomResponse(roomRepo.save(Room.builder().roomNumber(req.getRoomNumber()).category(cat).build()));
    }

    @Override @Transactional
    public RoomResponse updateRoom(Long id, UpdateRoomRequest req) {
        Room room = roomRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room", id));
        if (req.getRoomNumber() != null && !req.getRoomNumber().equals(room.getRoomNumber())) {
            if (roomRepo.existsByRoomNumber(req.getRoomNumber()))
                throw new BusinessException("Room number already exists: " + req.getRoomNumber());
            room.setRoomNumber(req.getRoomNumber());
        }
        if (req.getCategoryId() != null) {
            RoomCategory cat = catRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("RoomCategory", req.getCategoryId()));
            room.setCategory(cat);
        }
        if (req.getStatus() != null) {
            try {
                room.setStatus(RoomStatus.valueOf(req.getStatus().toUpperCase()));
            } catch (IllegalArgumentException ignored) { }
        }
        return toRoomResponse(roomRepo.save(room));
    }

    @Override @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room", id));
        if (room.getStatus() == RoomStatus.OCCUPIED)
            throw new BusinessException("Cannot delete an occupied room.");
        roomRepo.delete(room);
    }

    // ── Extras & Bookings ─────────────────────────────────────────────────────

    @Override
    public List<RoomExtraResponse> getAllExtras() {
        return extraRepo.findByActiveTrue().stream().map(e -> RoomExtraResponse.builder()
                .id(e.getId()).name(e.getName()).price(e.getPrice()).active(e.isActive()).build()).toList();
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepo.findByPaidFalse().stream()
                .map(b -> toBookingResponse(b, b.getRoom())).toList();
    }

    @Override @Transactional
    public BookingResponse addExtrasToBooking(Long bookingId, AddBookingExtrasRequest req) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (booking.isPaid())
            throw new BusinessException("Cannot modify a checked-out booking.");

        for (Long extraId : req.getExtraIds()) {
            RoomExtra extra = extraRepo.findById(extraId)
                    .orElseThrow(() -> new ResourceNotFoundException("RoomExtra", extraId));
            booking.getExtras().add(BookingExtra.builder()
                    .name(extra.getName()).price(extra.getPrice()).build());
        }
        BigDecimal extrasTotal = booking.getExtras().stream()
                .map(BookingExtra::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long nights = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
        if (nights < 1) nights = 1;
        BigDecimal base = booking.getRoom().getCategory().getPricePerNight().multiply(BigDecimal.valueOf(nights));
        booking.setTotalCharged(base.add(extrasTotal));
        bookingRepo.save(booking);

        return toBookingResponse(booking, booking.getRoom());
    }

    @Override @Transactional
    public BookingResponse updateBooking(Long bookingId, UpdateBookingRequest req) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (booking.isPaid())
            throw new BusinessException("Cannot modify a checked-out booking.");
        if (!req.getCheckOut().isAfter(booking.getCheckIn()))
            throw new BusinessException("Check-out date must be after check-in date.");
        booking.setCheckOut(req.getCheckOut());

        long nights = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckIn(), req.getCheckOut());
        if (nights < 1) nights = 1;
        BigDecimal base = booking.getRoom().getCategory().getPricePerNight().multiply(BigDecimal.valueOf(nights));
        BigDecimal extrasTotal = booking.getExtras().stream()
                .map(BookingExtra::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        booking.setTotalCharged(base.add(extrasTotal));
        bookingRepo.save(booking);

        return toBookingResponse(booking, booking.getRoom());
    }

    /**
     * Check-in using roomId from request body (matches frontend CheckInRequest).
     */
    @Override @Transactional
    public BookingResponse checkIn(CheckInRequest req) {
        Room room = roomRepo.findById(req.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", req.getRoomId()));
        if (room.getStatus() == RoomStatus.OCCUPIED)
            throw new BusinessException("Room " + room.getRoomNumber() + " is already occupied.");
        if (!req.getCheckOut().isAfter(req.getCheckIn()))
            throw new BusinessException("Check-out date must be after check-in date.");

        Booking booking = Booking.builder()
                .room(room).guestName(req.getGuestName()).phone(req.getPhone())
                .nationality(req.getNationality()).address(req.getAddress())
                .idType(req.getIdType()).idNumber(req.getIdNumber())
                .checkIn(req.getCheckIn()).checkOut(req.getCheckOut()).build();
        bookingRepo.save(booking);

        room.setStatus(RoomStatus.OCCUPIED);
        roomRepo.save(room);

        return toBookingResponse(booking, room);
    }

    /**
     * Check-out using bookingId from request body (matches frontend CheckOutRequest).
     */
    @Override @Transactional
    public TransactionResponse checkOut(CheckOutRequest req, Long staffId) {
        Booking booking = bookingRepo.findById(req.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", req.getBookingId()));
        if (booking.isPaid())
            throw new BusinessException("Booking already checked out.");
        Room room = booking.getRoom();
        User staff = userRepo.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User", staffId));

        long nights = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
        if (nights < 1) nights = 1;
        BigDecimal baseAmount = room.getCategory().getPricePerNight().multiply(BigDecimal.valueOf(nights));

        List<TransactionLineItem> lineItems = new ArrayList<>();
        BigDecimal total = baseAmount;

        lineItems.add(TransactionLineItem.builder()
                .name("Room " + room.getRoomNumber() + " (" + room.getCategory().getName() + ")")
                .category("Accommodation").kind(LineItemKind.ITEM)
                .quantity((int) nights).unitPrice(room.getCategory().getPricePerNight())
                .subtotal(baseAmount).build());

        if (req.getExtraIds() != null) {
            for (Long extraId : req.getExtraIds()) {
                RoomExtra extra = extraRepo.findById(extraId)
                        .orElseThrow(() -> new ResourceNotFoundException("RoomExtra", extraId));
                total = total.add(extra.getPrice());
                booking.getExtras().add(BookingExtra.builder()
                        .name(extra.getName()).price(extra.getPrice()).build());
                lineItems.add(TransactionLineItem.builder()
                        .name(extra.getName()).category("Extra").kind(LineItemKind.ITEM)
                        .quantity(1).unitPrice(extra.getPrice()).subtotal(extra.getPrice()).build());
            }
        }

        booking.setPaid(true);
        booking.setTotalCharged(total);
        bookingRepo.save(booking);

        room.setStatus(RoomStatus.AVAILABLE);
        roomRepo.save(room);

        String ref  = "HTL-" + (txRepo.count() + 1001);
        String desc = "Room " + room.getRoomNumber() + " checkout – " + booking.getGuestName();
        Transaction tx = Transaction.builder()
                .reference(ref).module(TxModule.HOTEL).amount(total)
                .staff(staff).description(desc).build();
        lineItems.forEach(li -> li.setTransaction(tx));
        tx.setLineItems(lineItems);
        Transaction saved = txRepo.save(tx);

        return toTxResponse(saved);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RoomResponse toRoomResponse(Room r) {
        Booking activeBooking = bookingRepo.findByRoomAndPaidFalse(r).orElse(null);
        return RoomResponse.builder()
                .id(r.getId())
                .roomNumber(r.getRoomNumber())
                .category(RoomCategoryResponse.builder()
                        .id(r.getCategory().getId())
                        .name(r.getCategory().getName())
                        .build())
                .pricePerNight(r.getCategory().getPricePerNight())
                .status(r.getStatus())
                .activeBooking(activeBooking != null ? toBookingResponse(activeBooking, r) : null)
                .build();
    }

    private BookingResponse toBookingResponse(Booking b, Room r) {
        long nights = ChronoUnit.DAYS.between(b.getCheckIn(), b.getCheckOut());
        if (nights < 1) nights = 1;
        BigDecimal base = r.getCategory().getPricePerNight().multiply(BigDecimal.valueOf(nights));
        return BookingResponse.builder()
                .id(b.getId()).roomNumber(r.getRoomNumber())
                .guestName(b.getGuestName()).phone(b.getPhone())
                .nationality(b.getNationality()).address(b.getAddress())
                .idType(b.getIdType()).idNumber(b.getIdNumber())
                .checkIn(b.getCheckIn()).checkOut(b.getCheckOut())
                .nights((int) nights).baseAmount(base).paid(b.isPaid())
                .totalCharged(b.getTotalCharged()).createdAt(b.getCreatedAt())
                .extras(b.getExtras().stream().map(e -> BookingResponse.ExtraDto.builder()
                        .name(e.getName()).price(e.getPrice()).build()).toList())
                .build();
    }

    private TransactionResponse toTxResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId()).reference(t.getReference()).module(t.getModule())
                .amount(t.getAmount()).staffName(t.getStaff().getName())
                .description(t.getDescription()).status(t.getStatus())
                .lineItems(t.getLineItems().stream().map(li -> TransactionResponse.LineItemDto.builder()
                        .id(li.getId()).name(li.getName()).category(li.getCategory())
                        .kind(li.getKind()).quantity(li.getQuantity())
                        .unitPrice(li.getUnitPrice()).subtotal(li.getSubtotal()).build()).toList())
                .createdAt(t.getCreatedAt()).build();
    }
}
