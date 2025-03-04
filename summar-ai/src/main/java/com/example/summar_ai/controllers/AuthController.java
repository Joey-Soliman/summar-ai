package com.example.summar_ai.controllers;

import com.example.summar_ai.services.AuthService;
import com.example.summar_ai.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")  // Grouping all auth-related endpoints under "/auth"
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthService authService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    // GET request to show the login page
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password. Please try again.");
        }
        return "login";  // Returns login.html
    }

    // GET request to show the registration page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User()); // Adding an empty user object for the form
        return "register";  // Returns register.html
    }

    // POST request to handle user registration
    @PostMapping("/register")
    public String registerUser(User user, Model model) {
        // Debug Log for the username from the form
        if (authService.userExists(user.getUsername())) {
            model.addAttribute("error", "Username already taken!");
            return "register";  // Redirect back to the registration page with error
        }

        // Register user and save it to the database
        authService.registerUser(user);

        return "redirect:/login";  // After successful registration, redirect to the login page
    }
}
