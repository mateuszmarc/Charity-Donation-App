package pl.mateuszmarcyk.charity_donation_app.config.security;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @InjectMocks
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Test
    void givenCustomAccessDeniedHandler_whenHandle_thenDispatcherForwardRequestAndResponse() throws ServletException, IOException {
//        Arrange
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        when(request.getRequestDispatcher("/error/403")).thenReturn(requestDispatcher);

//        Act
        customAccessDeniedHandler.handle(request, response, exception);

//        Assert
        verify(request, times(1)).getRequestDispatcher("/error/403");
        verify(requestDispatcher, times(1)).forward(request, response);
    }
}