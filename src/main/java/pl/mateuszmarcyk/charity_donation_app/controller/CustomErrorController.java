package pl.mateuszmarcyk.charity_donation_app.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
public class CustomErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private static final String ERROR_TITLE_MODEL_ATTRIBUTE_KEY = "errorTitle";
    private static final String ERROR_MESSAGE_MODEL_ATTRIBUTE_KEY = "errorMessage";

    private final ErrorAttributes errorAttributes;

    @GetMapping("/error/403")
    public String accessDenied(Model model) {

        model.addAttribute(ERROR_TITLE_MODEL_ATTRIBUTE_KEY, "Odmowa dostępu");
        model.addAttribute(ERROR_MESSAGE_MODEL_ATTRIBUTE_KEY, "Nie masz uprawnień aby wejść na stronę");
        return "error-page";
    }

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        WebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> errorDetails = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        Integer statusCode = (Integer) errorDetails.get("status");
        String message = (String) errorDetails.get("message");

        log.info(message);
        if (statusCode == 404) {
            model.addAttribute(ERROR_TITLE_MODEL_ATTRIBUTE_KEY, "Ooops.... Mamy problem");
            model.addAttribute(ERROR_MESSAGE_MODEL_ATTRIBUTE_KEY, "Taka strona nie istnieje");
        } else {
            model.addAttribute(ERROR_TITLE_MODEL_ATTRIBUTE_KEY, "Wystąpił błąd");
            model.addAttribute(ERROR_MESSAGE_MODEL_ATTRIBUTE_KEY, "Nieznany błąd serwera");
        }

        return "error-page";

    }
}
