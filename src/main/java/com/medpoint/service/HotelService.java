package com.medpoint.service;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import java.util.List;

public interface HotelService {
    List<RoomCategoryResponse> getAllCategories();
    RoomCategoryResponse createCategory(RoomCategoryRequest req);
    RoomCategoryResponse updateCategoryPrice(Long id, UpdateRoomCategoryRequest req);
    void deleteCategory(Long id);

    List<RoomResponse> getAllRooms();
    RoomResponse addRoom(AddRoomRequest req);
    void deleteRoom(Long id);

    List<RoomExtraResponse> getAllExtras();
    List<BookingResponse> getAllBookings();

    /** Check in using roomId from request body */
    BookingResponse checkIn(CheckInRequest req);

    /** Check out using bookingId from request body */
    TransactionResponse checkOut(CheckOutRequest req, Long staffId);
}
