package com.zencode.book.auth;

import com.zencode.book.email.EmailDto;
import com.zencode.book.email.EmailService;
import com.zencode.book.email.EmailTemplateName;
import com.zencode.book.role.RoleRepository;
import com.zencode.book.security.JwtService;
import com.zencode.book.user.Token;
import com.zencode.book.user.TokenRepository;
import com.zencode.book.user.User;
import com.zencode.book.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register(RegisterRequest registerRequest) throws MessagingException {
        var role = roleRepository.findByName("USER").orElseThrow(() -> new IllegalStateException("Role USER was not found"));

        var user = User.builder()
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(role))
                .build();
        userRepository.save(user);

        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var token = generateAndSaveActivationToken(user);

        EmailDto emailDto = new EmailDto();
        emailDto.setTo(user.getEmail());
        emailDto.setSubject("Account Activation");
        emailDto.setEmailTemplateName(EmailTemplateName.ACTIVATE_ACCOUNT);

        Map<String, Object> properties = new HashMap<>();
        properties.put("username", user.fullname());
        properties.put("activationUrl", activationUrl);
        properties.put("activationCode", token);
        emailDto.setProperties(properties);

        emailService.sendEmail(emailDto);
    }

    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationCode(6);

        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);

        return generatedToken;
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );

        var claims = new HashMap<String, Object>();
        var user = (User) auth.getPrincipal();

        claims.put("fullName", user.fullname());

        var jwtToken = jwtService.generateToken(claims, user);

        return AuthenticationResponse.builder()
                .token(jwtToken).build();
    }

//    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been sent to the same email");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }
}
