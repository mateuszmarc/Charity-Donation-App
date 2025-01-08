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
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ErrorController.class)
class ErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ErrorAttributes errorAttributes;

    @Test
    @WithMockCustomUser
    void givenErrorController_whenAccessDenied_thenStatusIsOkAndModelAttributesAdded() throws Exception {
//        Arrange
        String errorTitle = "Odmowa dostępu";
        String errorMessage = "Nie masz uprawnień aby wejść na stronę";

//
        MvcResult mvcResult = mockMvc.perform(get("/error/403"))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertAll(
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(errorTitle),
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(errorMessage)
        );
    }

    @Test
    @WithMockCustomUser
    void givenErrorController_whenHandle404statusCodeThenStatusIsOkAndModelAttributesAdded() throws Exception {
//        Arrange
        String errorTitle = "Ooops.... Mamy problem";
        String errorMessage = "Taka strona nie istnieje";
        Integer pageNotFoundStatusCode = 404;

        Map<String, Object> errorDetails = new HashMap<>(Map.of("status", pageNotFoundStatusCode, "message", "errorMessage"));
        when(errorAttributes.getErrorAttributes(ArgumentMatchers.any(WebRequest.class), ArgumentMatchers.any(ErrorAttributeOptions.class))).thenReturn(errorDetails);

        MvcResult mvcResult = mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(errorAttributes, times(1)).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));

        assertAll(
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(errorTitle),
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(errorMessage)
        );
    }

    @Test
    @WithMockCustomUser
    void givenErrorController_whenHandleOtherErrorStatusCodeThenStatusIsOkAndModelAttributesAdded() throws Exception {
//        Arrange
        String errorTitle = "Wystąpił błąd";
        String errorMessage = "Nieznany błąd serwera";
        Integer serverErrorStatusCode = 500;

        Map<String, Object> errorDetails = new HashMap<>(Map.of("status", serverErrorStatusCode, "message", "errorMessage"));
        when(errorAttributes.getErrorAttributes(ArgumentMatchers.any(WebRequest.class), ArgumentMatchers.any(ErrorAttributeOptions.class))).thenReturn(errorDetails);

        MvcResult mvcResult = mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(errorAttributes, times(1)).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));

        assertAll(
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(errorTitle),
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(errorMessage)
        );
    }
}