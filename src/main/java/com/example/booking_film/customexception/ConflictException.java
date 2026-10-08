package com.example.booking_film.customexception;

public class ConflictException extends RuntimeException{
    public ConflictException(String message) {
        super(message);
    }
}
