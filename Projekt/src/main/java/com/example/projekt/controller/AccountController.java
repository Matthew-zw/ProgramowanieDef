package com.example.projekt.controller;



import com.example.projekt.dto.TwoFactorSetupDto;
import com.example.projekt.dto.VerifyTotpCodeDto;
import com.example.projekt.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    @GetMapping("/settings")
    public String showAccountSettings(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        TwoFactorSetupDto twoFactorSetupDto = userService.setupTwoFactorAuthentication(username);
        model.addAttribute("twoFactorSetup", twoFactorSetupDto);
        model.addAttribute("verifyCodeDto", new VerifyTotpCodeDto()); // Do formularza weryfikacji kodu
        return "account/settings"; // Widok: templates/account/settings.html
    }

    @PostMapping("/enable-2fa")
    public String enableTwoFactor(@ModelAttribute("verifyCodeDto") @Valid VerifyTotpCodeDto verifyCodeDto,
                                  BindingResult result,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();

        if (result.hasErrors()) {
            // Jeśli są błędy walidacji DTO, musimy ponownie załadować dane dla strony
            TwoFactorSetupDto twoFactorSetupDto = userService.setupTwoFactorAuthentication(username);
            model.addAttribute("twoFactorSetup", twoFactorSetupDto);
            // verifyCodeDto już jest w modelu z powodu @ModelAttribute
            return "account/settings";
        }

        boolean success = userService.enableTwoFactorAuthentication(username, verifyCodeDto.getCode());
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Uwierzytelnianie dwuskładnikowe zostało włączone.");
        } else {
            // Ponownie załaduj dane i dodaj błąd
            TwoFactorSetupDto twoFactorSetupDto = userService.setupTwoFactorAuthentication(username);
            model.addAttribute("twoFactorSetup", twoFactorSetupDto);
            model.addAttribute("verifyCodeDto", verifyCodeDto); // Zachowaj wpisany kod
            model.addAttribute("twoFactorError", "Nieprawidłowy kod weryfikacyjny.");
            return "account/settings";
        }
        return "redirect:/account/settings";
    }

    @PostMapping("/disable-2fa")
    public String disableTwoFactor(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        userService.disableTwoFactorAuthentication(username);
        redirectAttributes.addFlashAttribute("successMessage", "Uwierzytelnianie dwuskładnikowe zostało wyłączone.");
        return "redirect:/account/settings";
    }
}