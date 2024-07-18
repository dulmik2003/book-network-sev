package com.dulmika.book.auth;


import com.dulmika.book.email.EmailService;
import com.dulmika.book.role.Role;
import com.dulmika.book.role.RoleRepository;
import com.dulmika.book.user.Token;
import com.dulmika.book.user.TokenRepository;
import com.dulmika.book.user.User;
import com.dulmika.book.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
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
    // send the validation email
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
    // generate and save the activation token
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
}
