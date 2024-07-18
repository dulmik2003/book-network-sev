package com.dulmika.book.auth;


import com.dulmika.book.email.EmailService;
import com.dulmika.book.role.Role;
import com.dulmika.book.role.RoleRepository;
import com.dulmika.book.security.JwtService;
import com.dulmika.book.user.Token;
import com.dulmika.book.user.TokenRepository;
import com.dulmika.book.user.User;
import com.dulmika.book.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static com.dulmika.book.email.EmailTemplateName.ACTIVATE_ACCOUNT;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;


    //todo
    // register a user
    public void register(RegisterRequest request) throws MessagingException {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER was not initialized"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);
        sendValidationEmail(user);
    }


    //todo
    // Send the validation email
    private void sendValidationEmail(User user) throws MessagingException {
        String newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                ACTIVATE_ACCOUNT,
                newToken,
                "Account activation"
        );
    }


    //todo
    // Generate and save the Activation token
    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationToken();

        Token token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        tokenRepository.save(token);
        return generatedToken;
    }


    //todo
    // generate the activation token
    private String generateActivationToken() {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < 6; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }


    //todo
    // authenticate a user
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User authenticatedUser = (User) authentication.getPrincipal();
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("fullName", authenticatedUser.getFullName());

        String generatedToken = jwtService.generateToken(claims, authenticatedUser);
        return AuthenticationResponse.builder()
                .token(generatedToken)
                .build();
    }


    //todo
    // Activate a user account
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        //todo
        // Resend a validation email to the same user
        // if activation token has been expired
        if (isTokenExpired(savedToken)) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException(
                    "Activation token has been expired. A new token has been sent to the same email address"
            );
        }

        //todo
        // Enable the User Account
        // if that token's User is presenting in the database
        User user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        //todo
        // set validated time for Activation token
        // and save that token to the database
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }


    //todo
    // check if activation token has been expired or not
    private boolean isTokenExpired(Token token) {
        return LocalDateTime.now().isAfter(token.getExpiredAt());
    }
}
