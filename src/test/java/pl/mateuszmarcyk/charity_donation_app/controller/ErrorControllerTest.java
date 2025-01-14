package pl.mateuszmarcyk.charity_donation_app.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.request.WebRequest;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static pl.mateuszmarcyk.charity_donation_app.ErrorMessages.*;
import static pl.mateuszmarcyk.charity_donation_app.GlobalTestMethodVerifier.assertModelAndViewAttributes;
import static pl.mateuszmarcyk.charity_donation_app.GlobalTestMethodVerifier.assertMvcResult;
import static pl.mateuszmarcyk.charity_donation_app.UrlTemplates.ACCESS_DENIED_URL;
import static pl.mateuszmarcyk.charity_donation_app.UrlTemplates.ERROR_URL;
import static pl.mateuszmarcyk.charity_donation_app.ViewNames.ERROR_PAGE_VIEW;

@WebMvcTest(ErrorController.class)
class ErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ErrorAttributes errorAttributes;

    @Test
    @WithMockCustomUser
    void givenErrorController_whenAccessDenied_thenStatusIsOkAndModelAttributesAdded() throws Exception {
        // Arrange
        String urlTemplate = ACCESS_DENIED_URL;
        String expectedViewName = ERROR_PAGE_VIEW;
        String errorTitle = ACCESS_DENIED_EXCEPTION_TITLE;
        String errorMessage = ACCESS_DENIED_EXCEPTION_MESSAGE;

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> assertModelAndViewAttributes(mvcResult, new HashMap<>(Map.of("errorTitle", errorTitle, "errorMessage", errorMessage)) )
        );
    }

    @Test
    @WithMockCustomUser
    void givenErrorController_whenHandle404statusCodeThenStatusIsOkAndModelAttributesAdded() throws Exception {
        // Arrange
        String urlTemplate = ERROR_URL;
        String expectedViewName = ERROR_PAGE_VIEW;
        String errorTitle = PAGE_DOES_NOT_EXIST_TITLE;
        String errorMessage = PAGE_DOES_NOT_EXIST_MESSAGE;
        Integer pageNotFoundStatusCode = 404;

        Map<String, Object> errorDetails = new HashMap<>(Map.of("status", pageNotFoundStatusCode, "message", "errorMessage"));
        when(errorAttributes.getErrorAttributes(ArgumentMatchers.any(WebRequest.class), ArgumentMatchers.any(ErrorAttributeOptions.class))).thenReturn(errorDetails);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        // Assert
        assertAll(
                () -> verify(errorAttributes, times(1)).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)),
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> assertModelAndViewAttributes(mvcResult, new HashMap<>(Map.of("errorTitle", errorTitle, "errorMessage", errorMessage)))
        );
    }

    @Test
    @WithMockCustomUser
    void givenErrorController_whenHandleOtherErrorStatusCodeThenStatusIsOkAndModelAttributesAdded() throws Exception {
//        Arrange
        String urlTemplate = ERROR_URL;
        String expectedViewName = ERROR_PAGE_VIEW;
        String errorTitle = UNKNOWN_ERROR_TITLE;
        String errorMessage = UNKNOWN_ERROR_MESSAGE;
        Integer serverErrorStatusCode = 500;

        Map<String, Object> errorDetails = new HashMap<>(Map.of("status", serverErrorStatusCode, "message", "errorMessage"));
        when(errorAttributes.getErrorAttributes(ArgumentMatchers.any(WebRequest.class), ArgumentMatchers.any(ErrorAttributeOptions.class))).thenReturn(errorDetails);

        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        assertAll(
                () -> verify(errorAttributes, times(1)).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)),
                () -> verify(errorAttributes, times(1)).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)),
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> assertModelAndViewAttributes(mvcResult, new HashMap<>(Map.of("errorTitle", errorTitle, "errorMessage", errorMessage)))
        );
    }
}