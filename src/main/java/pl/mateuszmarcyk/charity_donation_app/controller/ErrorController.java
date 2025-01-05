package pl.mateuszmarcyk.charity_donation_app.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;


@RequiredArgsConstructor
@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private final ErrorAttributes errorAttributes;

    @GetMapping("/error/403")
    public String accessDenied(Model model) {

        model.addAttribute("errorTitle", "Odmowa dostępu");
        model.addAttribute("errorMessage", "Nie masz uprawnień aby wejść na stronę");
        return "error-page";
    }

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        WebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> errorDetails = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        Integer statusCode = (Integer) errorDetails.get("status");
        String message = (String) errorDetails.get("message");

        if (statusCode != null && statusCode == 404) {
            model.addAttribute("errorTitle", "Ooops.... Mamy problem");
            model.addAttribute("errorMessage", "Taka strona nie istnieje");
        } else {
            model.addAttribute("errorTitle", "Wystąpił błąd");
            model.addAttribute("errorMessage", message != null ? message : "Nieznany błąd serwera");
        }

        return "error-page";

    }
}
