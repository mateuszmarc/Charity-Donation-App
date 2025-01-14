package pl.mateuszmarcyk.charity_donation_app.controller;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.mateuszmarcyk.charity_donation_app.ErrorMessages;
import pl.mateuszmarcyk.charity_donation_app.UrlTemplates;
import pl.mateuszmarcyk.charity_donation_app.ViewNames;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.util.*;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.mateuszmarcyk.charity_donation_app.GlobalTestMethodVerifier.*;
import static pl.mateuszmarcyk.charity_donation_app.TestDataFactory.*;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    private final String INDEX_URL = UrlTemplates.HOME_URL;
    private final String MESSAGE_URL = UrlTemplates.MESSAGE_URL;

    private final String HOME_VIEW = ViewNames.INDEX_VIEW;
    private final String ERROR_VIEW = ViewNames.ERROR_PAGE_VIEW;

    private static final String MAIL_EXCEPTION_TITLE = ErrorMessages.MAIL_EXCEPTION_TITLE;
    private static final String MAIL_EXCEPTION_MESSAGE = ErrorMessages.MAIL_EXCEPTION_MESSAGE;

//    TEST DATA AVAILABLE FOR ALL METHODS
    private final String ERROR_INFO_TEST_MESSAGE = "Error info test";
    private final String SUCCESS_INFO_TEST_MESSAGE = "Success info test";

    private final int COUNTED_BAGS = 100;
    private final int COUNTED_DONATIONS = 10;
    private final List<Institution> INSTITUTIONS = new ArrayList<>(List.of(getInstitution(), getInstitution()));
    private final String MAIL_MESSAGE_CONTENT = "Test mail message content";
    private final Mail TEST_MAIL = new Mail("Subject", "Sender", MAIL_MESSAGE_CONTENT);
    private final User USER = getUser();
    private MessageDTO messageDTO = new MessageDTO("first name test", "last name test", "test message", "email@email.com");
    private Map<String, Object> expectedAttributes;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private  DonationService donationService;

    @MockBean
    private  InstitutionService institutionService;

    @MockBean
    private  AppMailSender appMailSender;

    @MockBean
    private  MailMessage mailMessageHelper;

    @MockBean
    private  LoggedUserModelHandler loggedUserModelHandler;

    @MockBean
    private MailFactory mailFactory;

    @MockBean
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        expectedAttributes = new HashMap<>(Map.of(
                "institutions", INSTITUTIONS,
                "allDonations", COUNTED_DONATIONS,
                "allDonationBags", COUNTED_BAGS));
    }

    private void stubMailMessageMethodsInvocation() {
        when(messageSource.getMessage("mail.message.success.info", null, Locale.getDefault())).thenReturn(SUCCESS_INFO_TEST_MESSAGE);
        when(messageSource.getMessage("mail.message.error.info", null, Locale.getDefault())).thenReturn(ERROR_INFO_TEST_MESSAGE);
    }

    private void stubMailMessageHelperAndMailMessageFactoryMethods() {
        when(mailMessageHelper.getMailMessage(any(MessageDTO.class))).thenReturn(MAIL_MESSAGE_CONTENT);
        when(mailFactory.createMail(any(), any(), any())).thenReturn(TEST_MAIL);
    }

    private void stubDonationAndInstitutionServiceMethods() {
        when(institutionService.findAll()).thenReturn(INSTITUTIONS);
        when(donationService.countAllBags()).thenReturn(COUNTED_BAGS);
        when(donationService.countAllDonations()).thenReturn(COUNTED_DONATIONS);
    }

    private void verifyMessageSourceMethodsInvocation() {
        verify(messageSource, times(1)).getMessage("mail.message.success.info", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("mail.message.error.info", null, Locale.getDefault());
    }

    private void verifyDonationAndInstitutionServiceMethodsInvocation() {
        verify(institutionService, times(1)).findAll();
        verify(donationService, times(1)).countAllBags();
        verify(donationService, times(1)).countAllDonations();
    }

    private void verifyMailMessageHelperGetMailMessageInvocation() {
        ArgumentCaptor<MessageDTO> messageDTOArgumentCaptor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(mailMessageHelper, times(1)).getMailMessage(messageDTOArgumentCaptor.capture());
        MessageDTO capturedMessageDTO = messageDTOArgumentCaptor.getValue();
        assertThat(capturedMessageDTO).isSameAs(messageDTO);
    }

    private void verifyMailFactoryCreateMailMethodInvocation() {
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
        List<String> capturedArguments = stringArgumentCaptor.getAllValues();
        assertIterableEquals(List.of("Nowa wiadomość", messageDTO.getFirstName() + " " + messageDTO.getLastName(), MAIL_MESSAGE_CONTENT), capturedArguments);
    }

    private void verifyAppMailSenderSendMailMessageMethodInvocation() throws MessagingException, UnsupportedEncodingException {
        ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(appMailSender, times(1)).sendMailMessage(mailArgumentCaptor.capture());
        Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail).isSameAs(TEST_MAIL);
    }

    private void assertEmptyMessageDTOFields(MessageDTO messageDTO) {
        assertAll(
                () -> assertThat(messageDTO.getFirstName()).isNull(),
                () -> assertThat(messageDTO.getLastName()).isNull(),
                () -> assertThat(messageDTO.getMessage()).isNull()
        );
    }

    @SafeVarargs
    private <T> void verifyNoInteractionsWithMocks(T... mockInstances) {
        for (T mockInstance : mockInstances) {
            verifyNoInteractions(mockInstance);
        }
    }

    void verifyMailSendingMechanism() {
        assertAll(
                () -> verifyMailMessageHelperGetMailMessageInvocation(),
                () -> verifyMessageSourceMethodsInvocation(),
                () -> verifyMailFactoryCreateMailMethodInvocation(),
                () -> verifyAppMailSenderSendMailMessageMethodInvocation()
        );
    }

    @Test
    @WithAnonymousUser
    void givenUnauthenticatedUser_whenIndex_thenStatusIsOkAndModelAttributesAdded() throws Exception {
        //        Arrange
        String urlTemplate = INDEX_URL;
        String expectedViewName = HOME_VIEW;
        messageDTO.setEmail(null);

        stubDonationAndInstitutionServiceMethods();

        //        Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        //        Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyNoInteractions(loggedUserModelHandler),
                () -> verifyDonationAndInstitutionServiceMethodsInvocation(),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    MessageDTO modelMessage = (MessageDTO) mvcResult.getModelAndView().getModel().get("message");
                    assertEmptyMessageDTOFields(modelMessage);
                }
        );
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenIndex_thenStatusIsOkAndModelAttributesAdded() throws Exception {
        //       Arrange
        String urlTemplate = INDEX_URL;
        String expectedViewName = HOME_VIEW;

        stubDonationAndInstitutionServiceMethods();
        stubLoggedUserModelHandlerMethodsInvocation(loggedUserModelHandler, USER);

        //        Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        //        Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verifyDonationAndInstitutionServiceMethodsInvocation(),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    MessageDTO modelMessage = (MessageDTO) mvcResult.getModelAndView().getModel().get("message");
                    assertEmptyMessageDTOFields(modelMessage);
                    assertThat(modelMessage.getEmail()).isEqualTo(USER.getEmail());
                }
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_USER", "ROLE_ADMIN"})
    void givenUserWithBothUserRoleAndAdminRole_whenIndex_thenStatusIsOkAndModelAttributesAdded() throws Exception {
        //       Arrange
        String urlTemplate = INDEX_URL;
        String expectedViewName = HOME_VIEW;

        stubDonationAndInstitutionServiceMethods();
        stubLoggedUserModelHandlerMethodsInvocation(loggedUserModelHandler, USER);

        //        Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        //        Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verifyDonationAndInstitutionServiceMethodsInvocation(),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    MessageDTO modelMessage = (MessageDTO) mvcResult.getModelAndView().getModel().get("message");
                    assertEmptyMessageDTOFields(modelMessage);
                    assertThat(modelMessage.getEmail()).isEqualTo(USER.getEmail());
                }
        );
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessMessageFormAndMessageValid_thenMessageIsSentStatusOkAndViewRendered() throws Exception {
        //        Arrange
        String urlTemplate = MESSAGE_URL;
        String expectedViewName = HOME_VIEW;

        stubDonationAndInstitutionServiceMethods();
        stubMailMessageHelperAndMailMessageFactoryMethods();
        stubMailMessageMethodsInvocation();

        expectedAttributes.put("messageSuccess", SUCCESS_INFO_TEST_MESSAGE);

        //    Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                .flashAttr("message", messageDTO))
                .andReturn();

        //        Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyNoInteractions(loggedUserModelHandler),
                () -> verifyMailSendingMechanism(),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    MessageDTO modelMessage = (MessageDTO) mvcResult.getModelAndView().getModel().get("message");
                    assertEmptyMessageDTOFields(modelMessage);
                    assertThat(modelMessage.getEmail()).isNull();
                }
        );
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessMessageFormAndMessageValidAndMessagingExceptionThrown_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
        //        Arrange
        String urlTemplate = MESSAGE_URL;
        String expectedViewName = ERROR_VIEW;

        stubMailMessageHelperAndMailMessageFactoryMethods();
        stubMailMessageMethodsInvocation();

        expectedAttributes = new HashMap<>(Map.of(
                "errorTitle", MAIL_EXCEPTION_TITLE,
                "errorMessage", MAIL_EXCEPTION_MESSAGE
        ));

        doAnswer(invocationOnMock -> {
            throw new MessagingException("message");
        }).when(appMailSender).sendMailMessage(any(Mail.class));

        //    Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andReturn();

        //        Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyNoInteractions(loggedUserModelHandler),
                () -> verifyMailSendingMechanism(),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("messageSuccess")).isNull(),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("messageError")).isNull()
        );
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessMessageFormAndMessageValidAndUnsupportedEncodingExceptionThrown_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
        //        Arrange
        String urlTemplate = MESSAGE_URL;
        String expectedViewName = ERROR_VIEW;

        stubMailMessageHelperAndMailMessageFactoryMethods();
        stubMailMessageMethodsInvocation();

        expectedAttributes = new HashMap<>(Map.of(
                "errorTitle", MAIL_EXCEPTION_TITLE,
                "errorMessage", MAIL_EXCEPTION_MESSAGE
        ));

        doAnswer(invocationOnMock -> {
            throw new MessagingException("message");
        }).when(appMailSender).sendMailMessage(any(Mail.class));

        //    Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andReturn();

        //        Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyNoInteractions(loggedUserModelHandler),
                () -> verifyMailSendingMechanism(),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("messageSuccess")).isNull(),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("messageError")).isNull()
        );
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessMessageFormAndMessageIsInvalid_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
        //        Arrange
        String urlTemplate = MESSAGE_URL;
        String expectedViewName = HOME_VIEW;
        messageDTO.setEmail(null);

        stubDonationAndInstitutionServiceMethods();
        stubMailMessageHelperAndMailMessageFactoryMethods();
        stubMailMessageMethodsInvocation();

        expectedAttributes.put("messageError", ERROR_INFO_TEST_MESSAGE);
        expectedAttributes.put("message", messageDTO);

        //    Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andExpect(model().attributeHasFieldErrors("message", "email"))
                .andReturn();

        //        Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyMessageSourceMethodsInvocation(),
                () -> verifyNoInteractionsWithMocks(loggedUserModelHandler, mailFactory, appMailSender, mailMessageHelper),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("messageSuccess")).isNull()
        );
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessMessageFormAndMessageValidAndMessagingExceptionThrown_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
        //        Arrange
        String urlTemplate = MESSAGE_URL;
        String expectedViewName = ERROR_VIEW;

        stubLoggedUserModelHandlerMethodsInvocation(loggedUserModelHandler, USER);
        stubMailMessageHelperAndMailMessageFactoryMethods();
        stubMailMessageMethodsInvocation();

        expectedAttributes = new HashMap<>(Map.of(
                "errorTitle", MAIL_EXCEPTION_TITLE,
                "errorMessage", MAIL_EXCEPTION_MESSAGE
        ));

        doAnswer(invocationOnMock -> {
            throw new MessagingException("message");
        }).when(appMailSender).sendMailMessage(any(Mail.class));

        //    Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andReturn();

        //        Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verifyMailSendingMechanism(),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("messageSuccess")).isNull(),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("messageError")).isNull()
        );
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessMessageFormAndMessageValidAndUnsupportedEncodingExceptionThrown_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
        //        Arrange
        String urlTemplate = MESSAGE_URL;
        String expectedViewName = ERROR_VIEW;

        stubLoggedUserModelHandlerMethodsInvocation(loggedUserModelHandler, USER);
        stubMailMessageHelperAndMailMessageFactoryMethods();
        stubMailMessageMethodsInvocation();

        expectedAttributes = new HashMap<>(Map.of(
                "errorTitle", MAIL_EXCEPTION_TITLE,
                "errorMessage", MAIL_EXCEPTION_MESSAGE
        ));

        doAnswer(invocationOnMock -> {
            throw new UnsupportedEncodingException("message");
        }).when(appMailSender).sendMailMessage(any(Mail.class));

        //    Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andReturn();

        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verifyMailSendingMechanism(),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("messageSuccess")).isNull(),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("messageError")).isNull()
        );
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessMessageFormAndMessageValid_thenMessageIsSentStatusOkAndViewRendered() throws Exception {
        //        Arrange
        String urlTemplate = MESSAGE_URL;
        String expectedViewName = HOME_VIEW;

        stubDonationAndInstitutionServiceMethods();
        stubLoggedUserModelHandlerMethodsInvocation(loggedUserModelHandler, USER);
        stubMailMessageHelperAndMailMessageFactoryMethods();
        stubMailMessageMethodsInvocation();

        expectedAttributes.put("messageSuccess", SUCCESS_INFO_TEST_MESSAGE);

        //    Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andReturn();

        //        Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verifyMailSendingMechanism(),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    MessageDTO modelMessage = (MessageDTO) mvcResult.getModelAndView().getModel().get("message");
                    assertEmptyMessageDTOFields(modelMessage);
                    assertThat(modelMessage.getEmail()).isEqualTo(USER.getEmail());
                }
        );
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessMessageFormAndMessageIsInvalid_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/message";
        String expectedViewName = "index";
        messageDTO.setMessage(null);

        stubLoggedUserModelHandlerMethodsInvocation(loggedUserModelHandler, USER);
        stubDonationAndInstitutionServiceMethods();
        stubMailMessageHelperAndMailMessageFactoryMethods();
        stubMailMessageMethodsInvocation();

        expectedAttributes.put("messageError", ERROR_INFO_TEST_MESSAGE);
        expectedAttributes.put("message", messageDTO);

        //    Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andExpect(model().attributeHasFieldErrors("message", "message"))
                .andReturn();

        //        Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verifyMessageSourceMethodsInvocation(),
                () -> verifyNoInteractionsWithMocks(mailFactory, appMailSender, mailMessageHelper),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("messageSuccess")).isNull()
        );
    }


    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenIndex_thenStatusIsRedirected() throws Exception {
        String urlTemplate = INDEX_URL;
        String expectedRedirectUrl = UrlTemplates.ADMIN_DASHBOARD_URL;

        mockMvc.perform(get(urlTemplate))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        verifyNoInteractions(loggedUserModelHandler);
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenProcessMessageForm_thenStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = MESSAGE_URL;
        String expectedRedirectUrl = UrlTemplates.ADMIN_DASHBOARD_URL;

//        Act & assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        assertAll(
                () -> verifyNoInteractionsWithMocks(loggedUserModelHandler, mailFactory, appMailSender, mailMessageHelper),
                () -> verifyMessageSourceMethodsInvocation()
        );

    }
}