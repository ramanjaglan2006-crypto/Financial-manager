package com.financemanager.controller;

import com.financemanager.dto.GenericResponse;
import com.financemanager.dto.LoginRequest;
import com.financemanager.dto.RegisterRequest;
import com.financemanager.dto.UserResponse;
import com.financemanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<GenericResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GenericResponse.success(user, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<GenericResponse<String>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, jakarta.servlet.http.HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("FMSESSIONID", session.getId());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        httpResponse.addCookie(cookie);

        return ResponseEntity.ok(GenericResponse.success("Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericResponse<String>> logout(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse httpResponse) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("FMSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        httpResponse.addCookie(cookie);

        return ResponseEntity.ok(GenericResponse.success("Logout successful"));
    }
}
