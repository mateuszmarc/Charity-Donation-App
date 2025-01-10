package pl.mateuszmarcyk.charity_donation_app.controller;

import jakarta.mail.MessagingException;
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
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.util.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

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

    @Test
    @WithAnonymousUser
    void givenUnauthenticatedUser_whenIndex_thenStatusIsOkAndModelAttributesAdded() throws Exception {
//        Arrange
        List<Institution> institutions = new ArrayList<>(List.of(getInstitution(), getInstitution()));
        Integer countedBags = 100;
        Integer countedDonations = 10;

        when(institutionService.findAll()).thenReturn(institutions);
        when(donationService.countAllBags()).thenReturn(countedBags);
        when(donationService.countAllDonations()).thenReturn(countedDonations);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, never()).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, never()).addUserToModel(any(User.class), any(Model.class));

        verify(institutionService, times(1)).findAll();
        verify(donationService, times(1)).countAllBags();
        verify(donationService, times(1)).countAllDonations();

        assertAll(
                () -> assertIterableEquals(institutions, (List) modelAndView.getModel().get("institutions")),
                () -> assertThat(modelAndView.getModel().get("allDonations")).isEqualTo(countedDonations),
                () -> assertThat(modelAndView.getModel().get("allDonationBags")).isEqualTo(countedBags),
                () -> assertThat(modelAndView.getModel().get("message")).isInstanceOf(MessageDTO.class)
        );
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenIndex_thenStatusIsOkAndModelAttributesAdded() throws Exception {
//       Arrange
        User loggedInUser = getUser();
        List<Institution> institutions = new ArrayList<>(List.of(getInstitution(), getInstitution()));
        Integer countedBags = 100;
        Integer countedDonations = 10;

        when(institutionService.findAll()).thenReturn(institutions);
        when(donationService.countAllBags()).thenReturn(countedBags);
        when(donationService.countAllDonations()).thenReturn(countedDonations);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);

        doAnswer(invocation -> {

            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        verify(institutionService, times(1)).findAll();
        verify(donationService, times(1)).countAllBags();
        verify(donationService, times(1)).countAllDonations();

        MessageDTO messageDTO = (MessageDTO) modelAndView.getModel().get("message");
        assertAll(
                () -> assertIterableEquals(institutions, (List) modelAndView.getModel().get("institutions")),
                () -> assertThat(modelAndView.getModel().get("allDonations")).isEqualTo(countedDonations),
                () -> assertThat(modelAndView.getModel().get("allDonationBags")).isEqualTo(countedBags),
                () -> assertThat(messageDTO).isNotNull(),
                () -> assertThat(messageDTO.getEmail()).isEqualTo(loggedInUser.getEmail())
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_USER", "ROLE_ADMIN"})
    void givenUserWithBothUserRoleAndAdminRole_whenIndex_thenStatusIsOkAndModelAttributesAdded() throws Exception {
//       Arrange
        User loggedInUser = getUser();
        List<Institution> institutions = new ArrayList<>(List.of(getInstitution(), getInstitution()));
        Integer countedBags = 100;
        Integer countedDonations = 10;

        when(institutionService.findAll()).thenReturn(institutions);
        when(donationService.countAllBags()).thenReturn(countedBags);
        when(donationService.countAllDonations()).thenReturn(countedDonations);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);

        doAnswer(invocation -> {

            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        verify(institutionService, times(1)).findAll();
        verify(donationService, times(1)).countAllBags();
        verify(donationService, times(1)).countAllDonations();

        MessageDTO messageDTO = (MessageDTO) modelAndView.getModel().get("message");

        assertAll(
                () -> assertIterableEquals(institutions, (List) modelAndView.getModel().get("institutions")),
                () -> assertThat(modelAndView.getModel().get("allDonations")).isEqualTo(countedDonations),
                () -> assertThat(modelAndView.getModel().get("allDonationBags")).isEqualTo(countedBags),
                () -> assertThat(messageDTO).isNotNull(),
                () -> assertThat(messageDTO.getEmail()).isEqualTo(loggedInUser.getEmail())
        );
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessMessageFormAndMessageValid_thenMessageIsSentStatusOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/message";
        String expectedViewName = "index";
        MessageDTO messageDTO = new MessageDTO("first name test", "last name test", "test message", "email@email.com");
        String messageSuccessInfo = "Message test info";
        String messageErrorInfo = "Message error info";
        String testMailMessage = "test mail message";
        Mail testMail = new Mail("Subject", "Sender", testMailMessage);

        when(messageSource.getMessage("mail.message.success.info", null, Locale.getDefault())).thenReturn(messageSuccessInfo);
        when(messageSource.getMessage("mail.message.error.info", null, Locale.getDefault())).thenReturn(messageErrorInfo);
        when(mailMessageHelper.getMailMessage(any(MessageDTO.class))).thenReturn(testMailMessage);
        when(mailFactory.createMail(any(), any(), any())).thenReturn(testMail);

//    Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                .flashAttr("message", messageDTO))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, never()).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, never()).addUserToModel(any(User.class), any(Model.class));

        ArgumentCaptor<MessageDTO> messageDTOArgumentCaptor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(mailMessageHelper, times(1)).getMailMessage(messageDTOArgumentCaptor.capture());
        MessageDTO capturedMessageDTO = messageDTOArgumentCaptor.getValue();
        assertThat(capturedMessageDTO).isSameAs(messageDTO);

        verify(messageSource, times(1)).getMessage("mail.message.success.info", null, Locale.getDefault());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
        List<String> capturedArguments = stringArgumentCaptor.getAllValues();
        assertIterableEquals(List.of("Nowa wiadomość", messageDTO.getFirstName() + " " + messageDTO.getLastName(), testMailMessage), capturedArguments);

        ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(appMailSender, times(1)).sendMailMessage(mailArgumentCaptor.capture());
        Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail).isSameAs(testMail);

        MessageDTO messageDTOFromModel = (MessageDTO) modelAndView.getModel().get("message");

        assertAll(
                () -> assertThat(modelAndView.getModel().get("messageSuccess")).isEqualTo(messageSuccessInfo),
                () -> assertThat(modelAndView.getModel().get("messageError")).isNull(),
                () -> assertThat(messageDTOFromModel.getMessage()).isNull(),
                () -> assertThat(messageDTOFromModel.getFirstName()).isNull(),
                () -> assertThat(messageDTOFromModel.getLastName()).isNull(),
                () -> assertThat(messageDTOFromModel.getEmail()).isNull()
        );
        assertThat(modelAndView.getModel().get("messageSuccess")).isEqualTo(messageSuccessInfo);
        assertThat(modelAndView.getModel().get("messageError")).isNull();
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessMessageFormAndMessageValidAndMessagingExceptionThrown_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/message";
        String expectedViewName = "error-page";
        MessageDTO messageDTO = new MessageDTO("first name test", "last name test", "test message", "email@email.com");
        String messageSuccessInfo = "Message test info";
        String messageErrorInfo = "Message error info";
        String testMailMessage = "test mail message";
        Mail testMail = new Mail("Subject", "Sender", testMailMessage);
        String exceptionTitle = "Nie można wysłać";
        String exceptionMessage = "Wystąpił błąd podczas wysyłania. Spróbuj ponownie";


        when(messageSource.getMessage("mail.message.success.info", null, Locale.getDefault())).thenReturn(messageSuccessInfo);
        when(messageSource.getMessage("mail.message.error.info", null, Locale.getDefault())).thenReturn(messageErrorInfo);
        when(mailMessageHelper.getMailMessage(any(MessageDTO.class))).thenReturn(testMailMessage);
        when(mailFactory.createMail(any(), any(), any())).thenReturn(testMail);
        doAnswer(invocationOnMock -> {
            throw new MessagingException("message");
        }).when(appMailSender).sendMailMessage(any(Mail.class));

//    Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, never()).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, never()).addUserToModel(any(User.class), any(Model.class));

        ArgumentCaptor<MessageDTO> messageDTOArgumentCaptor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(mailMessageHelper, times(1)).getMailMessage(messageDTOArgumentCaptor.capture());
        MessageDTO capturedMessageDTO = messageDTOArgumentCaptor.getValue();
        assertThat(capturedMessageDTO).isSameAs(messageDTO);

        verify(messageSource, times(1)).getMessage("mail.message.success.info", null, Locale.getDefault());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
        List<String> capturedArguments = stringArgumentCaptor.getAllValues();
        assertIterableEquals(List.of("Nowa wiadomość", messageDTO.getFirstName() + " " + messageDTO.getLastName(), testMailMessage), capturedArguments);

        ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(appMailSender, times(1)).sendMailMessage(mailArgumentCaptor.capture());
        Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail).isSameAs(testMail);

        assertAll(
                () -> assertThat(modelAndView.getModel().get("messageSuccess")).isNull(),
                () -> assertThat(modelAndView.getModel().get("messageError")).isNull(),
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(exceptionTitle),
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(exceptionMessage)
        );
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessMessageFormAndMessageValidAndUnsupportedEncodingExceptionThrown_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/message";
        String expectedViewName = "error-page";
        MessageDTO messageDTO = new MessageDTO("first name test", "last name test", "test message", "email@email.com");
        String messageSuccessInfo = "Message test info";
        String messageErrorInfo = "Message error info";
        String testMailMessage = "test mail message";
        Mail testMail = new Mail("Subject", "Sender", testMailMessage);
        String exceptionTitle = "Nie można wysłać";
        String exceptionMessage = "Wystąpił błąd podczas wysyłania. Spróbuj ponownie";


        when(messageSource.getMessage("mail.message.success.info", null, Locale.getDefault())).thenReturn(messageSuccessInfo);
        when(messageSource.getMessage("mail.message.error.info", null, Locale.getDefault())).thenReturn(messageErrorInfo);
        when(mailMessageHelper.getMailMessage(any(MessageDTO.class))).thenReturn(testMailMessage);
        when(mailFactory.createMail(any(), any(), any())).thenReturn(testMail);
        doAnswer(invocationOnMock -> {
            throw new UnsupportedEncodingException("message");
        }).when(appMailSender).sendMailMessage(any(Mail.class));

//    Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, never()).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, never()).addUserToModel(any(User.class), any(Model.class));

        ArgumentCaptor<MessageDTO> messageDTOArgumentCaptor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(mailMessageHelper, times(1)).getMailMessage(messageDTOArgumentCaptor.capture());
        MessageDTO capturedMessageDTO = messageDTOArgumentCaptor.getValue();
        assertThat(capturedMessageDTO).isSameAs(messageDTO);

        verify(messageSource, times(1)).getMessage("mail.message.success.info", null, Locale.getDefault());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
        List<String> capturedArguments = stringArgumentCaptor.getAllValues();
        assertIterableEquals(List.of("Nowa wiadomość", messageDTO.getFirstName() + " " + messageDTO.getLastName(), testMailMessage), capturedArguments);

        ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(appMailSender, times(1)).sendMailMessage(mailArgumentCaptor.capture());
        Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail).isSameAs(testMail);

        assertAll(
                () -> assertThat(modelAndView.getModel().get("messageSuccess")).isNull(),
                () -> assertThat(modelAndView.getModel().get("messageError")).isNull(),
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(exceptionTitle),
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(exceptionMessage)
        );
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessMessageFormAndMessageIsInvalid_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/message";
        String expectedViewName = "index";
        MessageDTO messageDTO = new MessageDTO("first name test", "last name test", "test message", null);
        String messageSuccessInfo = "Message test info";
        String messageErrorInfo = "Message error info";
        String testMailMessage = "test mail message";
        Mail testMail = new Mail("Subject", "Sender", testMailMessage);

        when(messageSource.getMessage("mail.message.success.info", null, Locale.getDefault())).thenReturn(messageSuccessInfo);
        when(messageSource.getMessage("mail.message.error.info", null, Locale.getDefault())).thenReturn(messageErrorInfo);
        when(mailMessageHelper.getMailMessage(any(MessageDTO.class))).thenReturn(testMailMessage);
        when(mailFactory.createMail(any(), any(), any())).thenReturn(testMail);

//    Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeHasFieldErrors("message", "email"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, never()).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, never()).addUserToModel(any(User.class), any(Model.class));

        verify(mailMessageHelper, never()).getMailMessage(any(MessageDTO.class));

        verify(messageSource, times(1)).getMessage("mail.message.success.info", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("mail.message.error.info", null, Locale.getDefault());

        verify(mailFactory, never()).createMail(any(String.class), any(String.class), any(String.class));
        verify(appMailSender, never()).sendMailMessage(any(Mail.class));

        assertThat(modelAndView.getModel().get("messageSuccess")).isNull();
        assertThat(modelAndView.getModel().get("messageError")).isEqualTo(messageErrorInfo);
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessMessageFormAndMessageValidAndMessagingExceptionThrown_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/message";
        String expectedViewName = "error-page";
        User loggedInUser = getUser();
        MessageDTO messageDTO = new MessageDTO("first name test", "last name test", "test message", "email@email.com");
        String messageSuccessInfo = "Message test info";
        String testMailMessage = "test mail message";
        Mail testMail = new Mail("Subject", "Sender", testMailMessage);
        String exceptionTitle = "Nie można wysłać";
        String exceptionMessage = "Wystąpił błąd podczas wysyłania. Spróbuj ponownie";

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {

            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        when(messageSource.getMessage("mail.message.success.info", null, Locale.getDefault())).thenReturn(messageSuccessInfo);
        when(mailMessageHelper.getMailMessage(any(MessageDTO.class))).thenReturn(testMailMessage);
        when(mailFactory.createMail(any(), any(), any())).thenReturn(testMail);

        doAnswer(invocationOnMock -> {
            throw new MessagingException("message");
        }).when(appMailSender).sendMailMessage(any(Mail.class));

//    Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        ArgumentCaptor<MessageDTO> messageDTOArgumentCaptor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(mailMessageHelper, times(1)).getMailMessage(messageDTOArgumentCaptor.capture());
        MessageDTO capturedMessageDTO = messageDTOArgumentCaptor.getValue();
        assertThat(capturedMessageDTO).isSameAs(messageDTO);

        verify(messageSource, times(1)).getMessage("mail.message.success.info", null, Locale.getDefault());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
        List<String> capturedArguments = stringArgumentCaptor.getAllValues();
        assertIterableEquals(List.of("Nowa wiadomość", messageDTO.getFirstName() + " " + messageDTO.getLastName(), testMailMessage), capturedArguments);

        ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(appMailSender, times(1)).sendMailMessage(mailArgumentCaptor.capture());
        Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail).isSameAs(testMail);

        MessageDTO messageDTOFromModel = (MessageDTO) modelAndView.getModel().get("message");


        assertAll(
                () -> assertThat(modelAndView.getModel().get("messageSuccess")).isNull(),
                () -> assertThat(modelAndView.getModel().get("messageError")).isNull(),
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(exceptionTitle),
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(exceptionMessage)
        );
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessMessageFormAndMessageValidAndUnsupportedEncodingExceptionThrown_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/message";
        String expectedViewName = "error-page";
        User loggedInUser = getUser();
        MessageDTO messageDTO = new MessageDTO("first name test", "last name test", "test message", "email@email.com");
        String messageSuccessInfo = "Message test info";
        String testMailMessage = "test mail message";
        Mail testMail = new Mail("Subject", "Sender", testMailMessage);
        String exceptionTitle = "Nie można wysłać";
        String exceptionMessage = "Wystąpił błąd podczas wysyłania. Spróbuj ponownie";

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {

            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        when(messageSource.getMessage("mail.message.success.info", null, Locale.getDefault())).thenReturn(messageSuccessInfo);
        when(mailMessageHelper.getMailMessage(any(MessageDTO.class))).thenReturn(testMailMessage);
        when(mailFactory.createMail(any(), any(), any())).thenReturn(testMail);

        doAnswer(invocationOnMock -> {
            throw new UnsupportedEncodingException("message");
        }).when(appMailSender).sendMailMessage(any(Mail.class));

//    Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        ArgumentCaptor<MessageDTO> messageDTOArgumentCaptor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(mailMessageHelper, times(1)).getMailMessage(messageDTOArgumentCaptor.capture());
        MessageDTO capturedMessageDTO = messageDTOArgumentCaptor.getValue();
        assertThat(capturedMessageDTO).isSameAs(messageDTO);

        verify(messageSource, times(1)).getMessage("mail.message.success.info", null, Locale.getDefault());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
        List<String> capturedArguments = stringArgumentCaptor.getAllValues();
        assertIterableEquals(List.of("Nowa wiadomość", messageDTO.getFirstName() + " " + messageDTO.getLastName(), testMailMessage), capturedArguments);

        ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(appMailSender, times(1)).sendMailMessage(mailArgumentCaptor.capture());
        Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail).isSameAs(testMail);

        assertAll(
                () -> assertThat(modelAndView.getModel().get("messageSuccess")).isNull(),
                () -> assertThat(modelAndView.getModel().get("messageError")).isNull(),
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(exceptionTitle),
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(exceptionMessage)
        );
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessMessageFormAndMessageValid_thenMessageIsSentStatusOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/message";
        String expectedViewName = "index";
        User loggedInUser = getUser();
        MessageDTO messageDTO = new MessageDTO("first name test", "last name test", "test message", "email@email.com");
        String messageSuccessInfo = "Message test info";
        String testMailMessage = "test mail message";
        Mail testMail = new Mail("Subject", "Sender", testMailMessage);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {

            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        when(messageSource.getMessage("mail.message.success.info", null, Locale.getDefault())).thenReturn(messageSuccessInfo);
        when(mailMessageHelper.getMailMessage(any(MessageDTO.class))).thenReturn(testMailMessage);
        when(mailFactory.createMail(any(), any(), any())).thenReturn(testMail);

//    Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        ArgumentCaptor<MessageDTO> messageDTOArgumentCaptor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(mailMessageHelper, times(1)).getMailMessage(messageDTOArgumentCaptor.capture());
        MessageDTO capturedMessageDTO = messageDTOArgumentCaptor.getValue();
        assertThat(capturedMessageDTO).isSameAs(messageDTO);

        verify(messageSource, times(1)).getMessage("mail.message.success.info", null, Locale.getDefault());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
        List<String> capturedArguments = stringArgumentCaptor.getAllValues();
        assertIterableEquals(List.of("Nowa wiadomość", messageDTO.getFirstName() + " " + messageDTO.getLastName(), testMailMessage), capturedArguments);

        ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(appMailSender, times(1)).sendMailMessage(mailArgumentCaptor.capture());
        Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail).isSameAs(testMail);

        MessageDTO messageDTOFromModel = (MessageDTO) modelAndView.getModel().get("message");

        assertAll(
                () ->  assertThat(modelAndView.getModel().get("messageSuccess")).isNotNull(),
                () ->  assertThat(modelAndView.getModel().get("messageError")).isNull(),
                () -> assertThat(messageDTOFromModel.getMessage()).isNull(),
                () -> assertThat(messageDTOFromModel.getFirstName()).isNull(),
                () -> assertThat(messageDTOFromModel.getLastName()).isNull(),
                () -> assertThat(messageDTOFromModel.getEmail()).isSameAs(loggedInUser.getEmail())
        );
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessMessageFormAndMessageIsInvalid_thenMessageIsNotSentStatusOkAndViewRendered() throws Exception {
//        Arrange
        User loggedInUser = getUser();
        String urlTemplate = "/message";
        String expectedViewName = "index";
        MessageDTO messageDTO = new MessageDTO("first name test", "last name test", null, "test@gmail.com");
        String messageSuccessInfo = "Message test info";
        String messageErrorInfo = "Message error info";
        String testMailMessage = "test mail message";
        Mail testMail = new Mail("Subject", "Sender", testMailMessage);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        when(messageSource.getMessage("mail.message.success.info", null, Locale.getDefault())).thenReturn(messageSuccessInfo);
        when(messageSource.getMessage("mail.message.error.info", null, Locale.getDefault())).thenReturn(messageErrorInfo);
        when(mailMessageHelper.getMailMessage(any(MessageDTO.class))).thenReturn(testMailMessage);
        when(mailFactory.createMail(any(), any(), any())).thenReturn(testMail);

//    Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeHasFieldErrors("message", "message"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        verify(mailMessageHelper, never()).getMailMessage(any(MessageDTO.class));

        verify(messageSource, times(1)).getMessage("mail.message.success.info", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("mail.message.error.info", null, Locale.getDefault());

        verify(mailFactory, never()).createMail(any(String.class), any(String.class), any(String.class));
        verify(appMailSender, never()).sendMailMessage(any(Mail.class));

        assertThat(modelAndView.getModel().get("messageSuccess")).isNull();
        assertThat(modelAndView.getModel().get("messageError")).isEqualTo(messageErrorInfo);
    }


    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenIndex_thenStatusIsRedirected() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admins/dashboard"));

        verify(loggedUserModelHandler, never()).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler,  never()).addUserToModel(any(User.class), any(Model.class));
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenProcessMessageForm_thenStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/message";
        String expectedRedirectUrl = "/admins/dashboard";
        MessageDTO messageDTO = new MessageDTO("first name test", "last name test", null, "test@gmail.com");

//        Act & assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("message", messageDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        verify(loggedUserModelHandler, never()).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler,  never()).addUserToModel(any(User.class), any(Model.class));

        verify(messageSource, times(1)).getMessage("mail.message.success.info", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("mail.message.error.info", null, Locale.getDefault());

        verify(mailFactory, never()).createMail(any(String.class), any(String.class), any(String.class));
        verify(appMailSender, never()).sendMailMessage(any(Mail.class));
    }


    private static Institution getInstitution() {
        return new Institution(1L, "test name", "test description", new ArrayList<>());
    }

    private static User getUser() {
        UserProfile userProfile = new UserProfile(2L, null, "Mateusz", "Marcykiewicz", "Kielce",
                "Poland", null, "555666777");
        UserType userType = new UserType(2L, "ROLE_USER", new ArrayList<>());
        User user = new User(
                1L,
                "test@email.com",
                true,
                false,
                "testPW",
                LocalDateTime.of(2023, 11, 11, 12, 25, 11),
                "testPW",
                new HashSet<>(Set.of(userType)),
                userProfile,
                null,
                null,
                new ArrayList<>()
        );

        userProfile.setUser(user);
        return user;
    }

}