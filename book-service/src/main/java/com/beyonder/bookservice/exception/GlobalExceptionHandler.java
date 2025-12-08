package com.beyonder.bookservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
//@RestControllerAdvice:
//Ini adalah anotasi gabungan (shortcut) dari @ControllerAdvice dan @ResponseBody.
//@ControllerAdvice: Memberi tahu Spring bahwa kelas ini adalah advice (saran) yang dapat diterapkan secara global ke beberapa controller.
//@ResponseBody: Menunjukkan bahwa metode di dalam kelas ini akan mengembalikan data langsung (biasanya JSON/XML) dan bukan view.
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }
}
