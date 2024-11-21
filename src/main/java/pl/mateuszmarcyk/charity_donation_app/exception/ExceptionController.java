package pl.mateuszmarcyk.charity_donation_app.exception;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExceptionController {

    @GetMapping("/notFound")
    public String showNotFoundPage() {
        return "error-page";
    }
}
