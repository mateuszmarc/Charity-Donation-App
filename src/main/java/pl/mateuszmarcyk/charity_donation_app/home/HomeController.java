package pl.mateuszmarcyk.charity_donation_app.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenAlreadyConsumedException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;

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

    @GetMapping("/testError")
    public String error() {
        throw  new ResourceNotFoundException("Error", "This resource doesn't exist");
    }

    @GetMapping("/tokenNotfound")
    public String tokenNotFound() {
        throw  new TokenNotFoundException("Error", "Token resource doesn't exist");
    }

    @GetMapping("/tokenConsumed")
    public String tokenConsumed() {
        throw  new TokenAlreadyConsumedException("Error", "Token is consumed");
    }
}
