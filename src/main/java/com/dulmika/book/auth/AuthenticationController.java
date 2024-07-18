package com.dulmika.book.auth;

import  io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.ACCEPTED;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {
    private final AuthenticationService authService;

    @PostMapping("/register")
    @ResponseStatus(ACCEPTED)
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) throws MessagingException {
        authService.register(request);
        return ResponseEntity.accepted().build();
    }
}
