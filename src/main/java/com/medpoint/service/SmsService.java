package com.medpoint.service;

public interface SmsService {
    /**
     * Send an SMS message to the given phone number.
     *
     * @param phone   recipient phone (local GH format like 024XXXXXXX or international 233XXXXXXX)
     * @param message message text
     */
    void send(String phone, String message);
}
