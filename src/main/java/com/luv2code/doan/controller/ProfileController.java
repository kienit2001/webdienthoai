package com.luv2code.doan.controller;

import com.luv2code.doan.bean.ChangePassword;
import com.luv2code.doan.entity.*;
import com.luv2code.doan.exceptions.UserNotFoundException;
import com.luv2code.doan.principal.UserPrincipal;
import com.luv2code.doan.service.CartService;
import com.luv2code.doan.service.OrderService;
import com.luv2code.doan.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;
import java.util.List;


@Controller
public class ProfileController {

    private final Logger log = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;

    @GetMapping("/profile/info")
    public String profileInfo(@AuthenticationPrincipal UserPrincipal loggedUser, Model model,
                              RedirectAttributes redirectAttributes) {
        Integer id = loggedUser.getId();
        try {
            User user = userService.getUserByID(id);
            List<Cart> listCarts = cartService.findCartByUser(loggedUser.getId());

            double estimatedTotal = 0;

            for (Cart item : listCarts) {
                estimatedTotal += item.getSubtotal();
            }

            model.addAttribute("listCarts", listCarts);
            model.addAttribute("estimatedTotal", estimatedTotal);
            model.addAttribute("user", user);
            return "profile-user/profile";
        }
        catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("messageError", e.getMessage());
            return "profile-user/profile";
        }

    }

    @GetMapping("/profile/edit")
    public String profileEdit(@AuthenticationPrincipal UserPrincipal loggedUser, Model model,
                              RedirectAttributes redirectAttributes) {
        Integer id = loggedUser.getId();
        try {
            List<Cart> listCarts = cartService.findCartByUser(loggedUser.getId());

            double estimatedTotal = 0;

            for (Cart item : listCarts) {
                estimatedTotal += item.getSubtotal();
            }

            model.addAttribute("listCarts", listCarts);
            model.addAttribute("estimatedTotal", estimatedTotal);
            if (!model.containsAttribute("user")) {
                User user = userService.getUserByID(id);
                model.addAttribute("user", user);
            }
            return "profile-user/edit-profile";
        }
        catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("messageError", e.getMessage());
            return "profile-user/edit-profile";
        }
    }

    @PostMapping("/profile/edit")
    public String editProfile(User user, BindingResult errors, @AuthenticationPrincipal UserPrincipal loggedUser,
                              RedirectAttributes redirectAttributes) {
        Integer idLoggedUser = loggedUser.getId();
        if(!idLoggedUser.equals(user.getId())) {
            redirectAttributes.addFlashAttribute("messageError", "Loi xac thuc nguoi dung!");
            return "redirect:/profile/edit";
        }
        try {
            User existUser = userService.getUserByID(user.getId());
            if (user.getLastName().matches(".*\\d+.*")) {
                errors.rejectValue("lastName", "user", "H??? kh??ng ???????c ch???a s???!");
            }
            if (user.getLastName().matches(".*[:;/{}*<>=()!.#$@_+,?-]+.*")) {
                errors.rejectValue("lastName", "user", "H??? kh??ng ???????c ch???a k?? t??? ?????c bi???t!");
            }
            if (user.getFirstName().matches(".*\\d+.*")) {
                errors.rejectValue("firstName", "user", "T??n kh??ng ???????c ch???a s???!");
            }
            if (user.getFirstName().matches(".*[:;/{}*<>=()!.#$@_+,?-]+.*")) {
                errors.rejectValue("firstName", "user", "T??n kh??ng ???????c ch???a k?? t??? ?????c bi???t!");
            }
            if (user.getLastName().length() > 100) {
                errors.rejectValue("lastName", "user", "H??? kh??ng ???????c d??i qu?? 100 k?? t???!");
            }
            if (user.getFirstName().length() > 50) {
                errors.rejectValue("firstName", "user", "T??n kh??ng ???????c d??i qu?? 100 k?? t???!");
            }
            if (!user.getPhone().matches("\\d{10,}")) {
                errors.rejectValue("phone", "user", "S??? ??i???n tho???i kh??ng h???p l???!");
            }
            if ((userService.getUserByPhone(user.getPhone()) != null) && !user.getPhone().equals(existUser.getPhone())) {
                errors.rejectValue("phone", "user", "S??? ??i???n tho???i ???? ???????c s??? d???ng!");
            }
            if (errors.hasErrors()) {
                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", errors);
                redirectAttributes.addFlashAttribute("user", user);
                return "redirect:/profile/edit";
            } else{
                existUser.setFirstName(user.getFirstName());
                existUser.setLastName(user.getLastName());
                existUser.setPhone(user.getPhone());
                log.info(existUser.toString());
                userService.saveEditUser(existUser);
                redirectAttributes.addFlashAttribute("messageSuccess", "Ng?????i d??ng ???? ???????c ch???nh s???a th??nh c??ng.");
                return "redirect:/profile/info";
            }
        }
        catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("messageError", e.getMessage());
            return "redirect:/profile/edit";
        }

    }

    @GetMapping("/profile/change-password")
    public String profileChangePassword(@AuthenticationPrincipal UserPrincipal loggedUser, Model model,
                                        RedirectAttributes redirectAttributes, @ModelAttribute("password") ChangePassword changePassword) {
        Integer id = loggedUser.getId();
        try {
            User user = userService.getUserByID(id);
            List<Cart> listCarts = cartService.findCartByUser(loggedUser.getId());

            double estimatedTotal = 0;

            for (Cart item : listCarts) {
                estimatedTotal += item.getSubtotal();
            }

            model.addAttribute("listCarts", listCarts);
            model.addAttribute("estimatedTotal", estimatedTotal);
            model.addAttribute("user", user);
            return "profile-user/change-password";
        }
        catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("messageError", e.getMessage());
            return "profile-user/change-password";
        }

    }

    @PostMapping("/profile/change-password")
    public String postProfileChangePassword(@ModelAttribute("password") @Valid ChangePassword password, BindingResult errors,
                                            RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserPrincipal loggedUser,
                                            Model model) {
        Integer id = loggedUser.getId();
        try {
            User user = userService.getUserByID(id);
            if (!BCrypt.checkpw(password.getOldPass(), user.getPassword())) {
                errors.rejectValue("oldPass", "password", "M???t kh???u hi???n t???i kh??ng ????ng!");
            }
            if (BCrypt.checkpw(password.getNewPass(), user.getPassword())) {
                errors.rejectValue("newPass", "password", "M???t kh???u m???i tr??ng v???i m???t kh???u c??!");
            }
            if (!password.getConfirmPass().equalsIgnoreCase(password.getNewPass())) {
                errors.rejectValue("confirmPass", "password", "M???t kh???u x??c nh???n kh??ng ????ng!");
            }
            if(errors.hasErrors()) {
                model.addAttribute("user", user);
                return "profile-user/change-password";
            }
            else {
                log.info("working");
                String encodePassword = passwordEncoder.encode(password.getNewPass());

                user.setPassword(encodePassword);
                userService.saveEditUser(user);
                redirectAttributes.addFlashAttribute("messageSuccess", "Password ???? ???????c ch???nh s???a th??nh c??ng.");
                return "redirect:/profile/change-password";
            }

        }
        catch (UserNotFoundException e) {
            log.info("error");
            redirectAttributes.addFlashAttribute("messageError", e.getMessage());
            return "redirect:/profile/change-password";
        }
    }

}
