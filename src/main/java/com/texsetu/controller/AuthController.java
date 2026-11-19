package com.texsetu.controller;

import com.texsetu.dto.UserRegistrationDto;
import com.texsetu.entity.enums.Role;
import com.texsetu.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registrationDto")) {
            UserRegistrationDto dto = new UserRegistrationDto();
            dto.setRole(Role.BUYER);
            dto.setCity("Surat");
            dto.setState("Gujarat");
            model.addAttribute("registrationDto", dto);
        }
        return "auth/register";
    }

    @PostMapping("/register-process")
    public String processRegistration(@Valid @ModelAttribute("registrationDto") UserRegistrationDto dto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerUser(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now log in to your account.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
}
