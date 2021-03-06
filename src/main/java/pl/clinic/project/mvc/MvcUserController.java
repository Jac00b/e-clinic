package pl.clinic.project.mvc;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pl.clinic.project.exception.NotFoundException;
import pl.clinic.project.model.User;
import pl.clinic.project.password_generator.PasswordGenerator;
import pl.clinic.project.service.UserService;

import javax.validation.Valid;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RequestMapping("/users")
public class MvcUserController {

    private final Logger logger = Logger.getLogger(MvcUserController.class.getName());
    private JavaMailSender mailSender;
    private UserService userService;

    public MvcUserController(UserService userService, JavaMailSender mailSender) {
        this.userService = userService;
        this.mailSender = mailSender;
    }

    @GetMapping("/add")
    ModelAndView addUserPage() {
        ModelAndView mav = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            mav.setViewName("users/registerUser.html");
            mav.addObject("user", new User());
        } else {
            mav.setViewName("redirect:/login");
        }
        return mav;
    }

    @PostMapping("/add")
    String addNewUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "error.html";
        }
        userService.registerUserAsPatient(user);

//        configure smtp client to send emails to users with confirmation
//        sendSimpleMail(user.getEmail(), "Aktywacja konta", "Twoje konto w e-przychodni zostało aktywowane.");
        return "redirect:/login";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    String showAdminPanel() {
        return "/admin/adminPanel.html";
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    void deleteUser(Integer id) {
        userService.deleteUser(id);
    }

    @GetMapping("/resetPassword")
    ModelAndView resetPasswordPage() {
        ModelAndView mav = new ModelAndView("users/resetPassword.html");
        String username = "";
        mav.addObject("user", new User());
        return mav;
    }

    @PostMapping("/resetPassword")
    String resetPassword(@Valid @ModelAttribute("user") User user, BindingResult bindingResult) {
        try {
            User userFromDB = userService.getByEmail(user.getEmail());
            String password = PasswordGenerator.generate();
            logger.log(Level.INFO, "Nowe hasło: " +password);
//        configure smtp client to send email with new password
//        sendSimpleMail(username, "Reset hasła", "Hasło zresetowane. Twoje nowe hasło: " +password);
            userFromDB.setPassword(password);
            userService.setPassword(userFromDB);
        } catch (NotFoundException e) {
            bindingResult.addError(new ObjectError("email", "Niepoprawny email"));
            user.setEmail("");
            return "/users/resetPassword.html";
        }

        return "redirect:/login";
    }

    private void sendSimpleMail(String to, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailSender.send(mailMessage);
    }

}
