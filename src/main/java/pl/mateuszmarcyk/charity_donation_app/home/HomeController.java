package pl.mateuszmarcyk.charity_donation_app.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

//    @GetMapping("/register")
//    public String register() {
//        return "register";
//    }

    @GetMapping("/confirm")
    public String confirmation() {
        return "form-confirmation";
    }

    @GetMapping("/form")
    public String form() {
        return "form";
    }
}
