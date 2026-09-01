package com.paulo.independentschooldata.dto;

public record EnquiryRequest(String message, String email, String name, String subject, Long schoolId) {
}
