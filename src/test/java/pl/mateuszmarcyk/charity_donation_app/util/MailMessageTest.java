package pl.mateuszmarcyk.charity_donation_app.util;

import org.junit.jupiter.api.Test;
import pl.mateuszmarcyk.charity_donation_app.entity.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MailMessageTest {

    private MailMessage mailMessage = new MailMessage();

    @Test
    void givenMailMessage_whenBuildMessage_thenMessageMatches() {
        String url = "http://localhost/app/example";
        String expected = """
             <!DOCTYPE html>
                     <html>
                     <head>
                         <style>
                             body {
                                 font-family: Arial, sans-serif;
                                 background-color: #f4f4f4;
                                 margin: 0;
                                 padding: 0;
                             }
                             .email-container {
                                 max-width: 600px;
                                 margin: 20px auto;
                                 background-color: #ffffff;
                                 border-radius: 10px;
                                 box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
                                 overflow: hidden;
                             }
                             .header {
                                 background-color: #28a745;
                                 color: #ffffff;
                                 text-align: center;
                                 padding: 20px 10px;
                             }
                             .header h1 {
                                 margin: 0;
                                 font-size: 24px;
                             }
                             .content {
                                 padding: 20px 30px;
                                 color: #333333;
                                 line-height: 1.6;
                             }
                             .content p {
                                 margin: 0 0 20px;
                             }
                             .details {
                                 margin: 20px 0;
                             }
                             .details h2 {
                                 margin-bottom: 10px;
                                 font-size: 18px;
                                 color: #007BFF;
                             }
                             .footer {
                                 text-align: center;
                                 background-color: #f4f4f4;
                                 color: #777777;
                                 font-size: 12px;
                                 padding: 10px;
                             }
                         </style>
                     </head>
                     <body>
                         <div class="email-container">
                             <div class="header">
                                 <h1>Dziękujemy za Rejestrację!</h1>
                             </div>
                             <div class="content">
                                 <p>Drogi/a użytkowniku,</p>
                                 <p>Dziękujemy za rejestrację w <strong>Oddaj w Dobre Ręce</strong>. Dzięki Twoim przyszłym darowiznom możemy dalej realizować naszą misję i pomagać innym.</p>
                                 <div class="details">
                                     <h2>Został jeszcze jeden krok</h2>
                                 </div>
                                 <div>
                                 <p>W celu aktywacji konta kliknij w poniższy link:</p>
                                 <a href="http://localhost/app/example">Zweryfikuj swoje konto klikając w link</a>
                                 </div>
                                 <p>Z wyrazami wdzięczności,<br>Oddaj w Dobre Ręce</p>
                             </div>
                             <div class="footer">
                                 <p>© 2024 Oddaj w Dobre Ręce. Wszelkie prawa zastrzeżone.</p>
                             </div>
                         </div>
                     </body>
                     </html>
            """;

        String builtMessage = mailMessage.buildMessage(url);
        assertThat(builtMessage).isEqualTo(expected);
    }

    @Test
    void givenMailMessageAndDonationWithUserFirstNameNull_whenBuildDonationMessage_thenMessageMatches() {
        Donation donation = spy(getDonation());
        donation.getUser().getProfile().setFirstName(null);

        String expectedMessage = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background-color: #007BFF;
                        color: #ffffff;
                        text-align: center;
                        padding: 20px 10px;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content {
                        padding: 20px 30px;
                        color: #333333;
                        line-height: 1.6;
                    }
                    .content p {
                        margin: 0 0 20px;
                    }
                    .details {
                        margin: 20px 0;
                    }
                    .details h2 {
                        margin-bottom: 10px;
                        font-size: 18px;
                        color: #007BFF;
                    }
                    .details table {
                        width: 100%;
                        border-collapse: collapse;
                    }
                    .details table th,
                    .details table td {
                        text-align: left;
                        padding: 10px;
                        border-bottom: 1px solid #f4f4f4;
                    }
                    .details table th {
                        background-color: #f9f9f9;
                        color: #555555;
                    }
                    .footer {
                        text-align: center;
                        background-color: #f4f4f4;
                        color: #777777;
                        font-size: 12px;
                        padding: 10px;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="header">
                        <h1>Dziękujemy za Twoją darowiznę!</h1>
                    </div>
                    <div class="content">
                        <p>Drogi/a donatorze,</p>
                        <p>Dziękujemy za Twoją hojność i wsparcie dla <strong>Pomagam</strong>. Dzięki Twojej darowiźnie możemy dalej realizować naszą misję i pomagać innym.</p>
                        <div class="details">
                            <h2>Szczegóły darowizny</h2>
                            <table>
                                <tr>
                                    <th>Ilość</th>
                                    <td>5</td>
                                </tr>
                                <tr>
                                    <th>Kategorie</th>
                                    <td>Ubranie, Jedzenie</td>
                                </tr>
                                <tr>
                                    <th>Adres odbioru</th>
                                    <td>
                                        Sample Street, Sample City, 12-345
                                    </td>
                                </tr>
                                <tr>
                                    <th>Data i godzina odbioru</th>
                                    <td>2024-12-31 o 10:00</td>
                                </tr>
                                <tr>
                                    <th>Numer telefonu</th>
                                    <td>123456789</td>
                                </tr>
                                <tr>
                                    <th>Uwagi</th>
                                    <td>Pickup Comment</td>
                                </tr>
                            </table>
                        </div>
                        <p>Jesteśmy bardzo wdzięczni za Twoje wsparcie. Jeśli masz jakiekolwiek pytania, skontaktuj się z nami w dowolnym momencie.</p>
                        <p>Z wyrazami wdzięczności,<br>Oddaj w Dobre Ręce</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Oddaj w Dobre Ręce. Wszelkie prawa zastrzeżone.</p>
                    </div>
                </div>
            </body>
            </html>
            
            """;

        String builtMessage = mailMessage.buildDonationMessage(donation);

        verify(donation, times(2)).getUser();
        verify(donation, times(1)).getInstitution();
        verify(donation, times(1)).getQuantity();
        verify(donation, times(1)).getCategoriesString();
        verify(donation, times(1)).getStreet();
        verify(donation, times(1)).getCity();
        verify(donation, times(1)).getZipCode();
        verify(donation, times(1)).getPickUpDate();
        verify(donation, times(1)).getPickUpTime();
        verify(donation, times(1)).getPhoneNumber();
        verify(donation, times(3)).getPickUpComment();

        assertThat(builtMessage).isEqualTo(expectedMessage);
    }

    @Test
    void givenMailMessageAndDonationWithNullDonationComment_whenBuildDonationMessage_thenMessageMatches() {
        Donation donation = spy(getDonation());
        donation.setPickUpComment(null);

        String expectedMessage = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background-color: #007BFF;
                        color: #ffffff;
                        text-align: center;
                        padding: 20px 10px;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content {
                        padding: 20px 30px;
                        color: #333333;
                        line-height: 1.6;
                    }
                    .content p {
                        margin: 0 0 20px;
                    }
                    .details {
                        margin: 20px 0;
                    }
                    .details h2 {
                        margin-bottom: 10px;
                        font-size: 18px;
                        color: #007BFF;
                    }
                    .details table {
                        width: 100%;
                        border-collapse: collapse;
                    }
                    .details table th,
                    .details table td {
                        text-align: left;
                        padding: 10px;
                        border-bottom: 1px solid #f4f4f4;
                    }
                    .details table th {
                        background-color: #f9f9f9;
                        color: #555555;
                    }
                    .footer {
                        text-align: center;
                        background-color: #f4f4f4;
                        color: #777777;
                        font-size: 12px;
                        padding: 10px;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="header">
                        <h1>Dziękujemy za Twoją darowiznę!</h1>
                    </div>
                    <div class="content">
                        <p>Drogi/a Mateusz,</p>
                        <p>Dziękujemy za Twoją hojność i wsparcie dla <strong>Pomagam</strong>. Dzięki Twojej darowiźnie możemy dalej realizować naszą misję i pomagać innym.</p>
                        <div class="details">
                            <h2>Szczegóły darowizny</h2>
                            <table>
                                <tr>
                                    <th>Ilość</th>
                                    <td>5</td>
                                </tr>
                                <tr>
                                    <th>Kategorie</th>
                                    <td>Ubranie, Jedzenie</td>
                                </tr>
                                <tr>
                                    <th>Adres odbioru</th>
                                    <td>
                                        Sample Street, Sample City, 12-345
                                    </td>
                                </tr>
                                <tr>
                                    <th>Data i godzina odbioru</th>
                                    <td>2024-12-31 o 10:00</td>
                                </tr>
                                <tr>
                                    <th>Numer telefonu</th>
                                    <td>123456789</td>
                                </tr>
                                <tr>
                                    <th>Uwagi</th>
                                    <td>Brak uwag</td>
                                </tr>
                            </table>
                        </div>
                        <p>Jesteśmy bardzo wdzięczni za Twoje wsparcie. Jeśli masz jakiekolwiek pytania, skontaktuj się z nami w dowolnym momencie.</p>
                        <p>Z wyrazami wdzięczności,<br>Oddaj w Dobre Ręce</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Oddaj w Dobre Ręce. Wszelkie prawa zastrzeżone.</p>
                    </div>
                </div>
            </body>
            </html>
            
            """;

        String builtMessage = mailMessage.buildDonationMessage(donation);

        verify(donation, times(1)).getUser();
        verify(donation, times(1)).getInstitution();
        verify(donation, times(1)).getQuantity();
        verify(donation, times(1)).getCategoriesString();
        verify(donation, times(1)).getStreet();
        verify(donation, times(1)).getCity();
        verify(donation, times(1)).getZipCode();
        verify(donation, times(1)).getPickUpDate();
        verify(donation, times(1)).getPickUpTime();
        verify(donation, times(1)).getPhoneNumber();
        verify(donation, times(1)).getPickUpComment();

        assertThat(builtMessage).isEqualTo(expectedMessage);
    }

    @Test
    void givenMailMessage_whenGetMailMessageFroNullUser_thenMessageMatches() {
        User user = null;
        String firstName = "Mateusz";
        String lastName = "Marcykiewicz";
        String message = "Random message";

        String expectedMessage = """
                <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background-color: #007BFF;
                        color: #ffffff;
                        text-align: center;
                        padding: 20px 10px;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content {
                        padding: 20px 30px;
                        color: #333333;
                        line-height: 1.6;
                    }
                    .content p {
                        margin: 0 0 20px;
                    }
                    .details {
                        margin: 20px 0;
                    }
                    .details h2 {
                        margin-bottom: 10px;
                        font-size: 18px;
                        color: #007BFF;
                    }
                    .details table {
                        width: 100%;
                        border-collapse: collapse;
                    }
                    .details table th,
                    .details table td {
                        text-align: left;
                        padding: 10px;
                        border-bottom: 1px solid #f4f4f4;
                    }
                    .details table th {
                        background-color: #f9f9f9;
                        color: #555555;
                    }
                    .footer {
                        text-align: center;
                        background-color: #f4f4f4;
                        color: #777777;
                        font-size: 12px;
                        padding: 10px;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="header">
                        <h1>Wiadomość od użytkownika</h1>
                    </div>
                    <div class="content">
                        <p>Drogi Zespole,</p>
                        <p>Poniżej znajdują się szczegóły wiadomości przesłanej przez użytkownika:</p>
                        <div class="details">
                            <h2>Szczegóły użytkownika</h2>
                            <table>
                                <tr>
                                    <th>Imię</th>
                                    <td>Mateusz</td>
                                </tr>
                                <tr>
                                    <th>Nazwisko</th>
                                    <td>Marcykiewicz</td>
                                </tr>
                                <tr>
                                    <th>Email</th>
                                    <td>brak emaila</td>
                                </tr>
                                <tr>
                                    <th>Wiadomość</th>
                                    <td>Random message</td>
                                </tr>
                            </table>
                        </div>
                        <p>Prosimy o odpowiedź na wiadomość w dogodnym dla Państwa czasie.</p>
                        <p>Z wyrazami szacunku,<br>Zespół Oddaj w Dobre Ręce</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Oddaj w Dobre Ręce. Wszelkie prawa zastrzeżone.</p>
                    </div>
                </div>
            </body>
            </html>
            
            """;

        String builtMessage = mailMessage.getMailMessage(firstName, lastName, message, user);
        assertThat(builtMessage).isEqualTo(expectedMessage);
    }

    @Test
    void givenMailMessage_whenGetMailMessageUser_thenMessageMatches() {
        User user = new User();
        user.setEmail("example@gmail.com");
        String firstName = "Mateusz";
        String lastName = "Marcykiewicz";
        String message = "Random message";

        String expectedMessage = """
                <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background-color: #007BFF;
                        color: #ffffff;
                        text-align: center;
                        padding: 20px 10px;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content {
                        padding: 20px 30px;
                        color: #333333;
                        line-height: 1.6;
                    }
                    .content p {
                        margin: 0 0 20px;
                    }
                    .details {
                        margin: 20px 0;
                    }
                    .details h2 {
                        margin-bottom: 10px;
                        font-size: 18px;
                        color: #007BFF;
                    }
                    .details table {
                        width: 100%;
                        border-collapse: collapse;
                    }
                    .details table th,
                    .details table td {
                        text-align: left;
                        padding: 10px;
                        border-bottom: 1px solid #f4f4f4;
                    }
                    .details table th {
                        background-color: #f9f9f9;
                        color: #555555;
                    }
                    .footer {
                        text-align: center;
                        background-color: #f4f4f4;
                        color: #777777;
                        font-size: 12px;
                        padding: 10px;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="header">
                        <h1>Wiadomość od użytkownika</h1>
                    </div>
                    <div class="content">
                        <p>Drogi Zespole,</p>
                        <p>Poniżej znajdują się szczegóły wiadomości przesłanej przez użytkownika:</p>
                        <div class="details">
                            <h2>Szczegóły użytkownika</h2>
                            <table>
                                <tr>
                                    <th>Imię</th>
                                    <td>Mateusz</td>
                                </tr>
                                <tr>
                                    <th>Nazwisko</th>
                                    <td>Marcykiewicz</td>
                                </tr>
                                <tr>
                                    <th>Email</th>
                                    <td>example@gmail.com</td>
                                </tr>
                                <tr>
                                    <th>Wiadomość</th>
                                    <td>Random message</td>
                                </tr>
                            </table>
                        </div>
                        <p>Prosimy o odpowiedź na wiadomość w dogodnym dla Państwa czasie.</p>
                        <p>Z wyrazami szacunku,<br>Zespół Oddaj w Dobre Ręce</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Oddaj w Dobre Ręce. Wszelkie prawa zastrzeżone.</p>
                    </div>
                </div>
            </body>
            </html>
            
            """;

        String builtMessage = mailMessage.getMailMessage(firstName, lastName, message, user);
        assertThat(builtMessage).isEqualTo(expectedMessage);
    }

    @Test
    void givenMailMessage_whenBuildPasswordResetMessage_thenMessageMatches() {
        String url = "http://localhost/app/example";
        String expected =  """
             <!DOCTYPE html>
                     <html>
                     <head>
                         <style>
                             body {
                                 font-family: Arial, sans-serif;
                                 background-color: #f4f4f4;
                                 margin: 0;
                                 padding: 0;
                             }
                             .email-container {
                                 max-width: 600px;
                                 margin: 20px auto;
                                 background-color: #ffffff;
                                 border-radius: 10px;
                                 box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
                                 overflow: hidden;
                             }
                             .header {
                                 background-color: #28a745;
                                 color: #ffffff;
                                 text-align: center;
                                 padding: 20px 10px;
                             }
                             .header h1 {
                                 margin: 0;
                                 font-size: 24px;
                             }
                             .content {
                                 padding: 20px 30px;
                                 color: #333333;
                                 line-height: 1.6;
                             }
                             .content p {
                                 margin: 0 0 20px;
                             }
                             .details {
                                 margin: 20px 0;
                             }
                             .details h2 {
                                 margin-bottom: 10px;
                                 font-size: 18px;
                                 color: #007BFF;
                             }
                             .footer {
                                 text-align: center;
                                 background-color: #f4f4f4;
                                 color: #777777;
                                 font-size: 12px;
                                 padding: 10px;
                             }
                         </style>
                     </head>
                     <body>
                         <div class="email-container">
                             <div class="header">
                                 <h1>Resetowanie hasła</h1>
                             </div>
                             <div class="content">
                                 <p>Drogi/a użytkowniku,</p>
                                 <div>
                                 <p>W celu zmiany hasła kliknij w poniższy link:</p>
                                 <a href="http://localhost/app/example">Zmień swoje hasło klikając w link</a>
                                 </div>
                                 <p>Z wyrazami wdzięczności,<br>Oddaj w Dobre Ręce</p>
                             </div>
                             <div class="footer">
                                 <p>© 2024 Oddaj w Dobre Ręce. Wszelkie prawa zastrzeżone.</p>
                             </div>
                         </div>
                     </body>
                     </html>
            """;

        String builtMessage = mailMessage.buildPasswordResetMessage(url);
        assertThat(builtMessage).isEqualTo(expected);
    }

    private static Donation getDonation() {
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName("Mateusz");

        User user = new User();
        user.setUserProfile(userProfile);


        Institution institution = new Institution(2L, "Pomagam", "Description", new ArrayList<>());

        Category firstCategory = new Category(1L, "Ubranie", new ArrayList<>());
        Category secondCategory = new Category(2L, "Jedzenie", new ArrayList<>());

        List<Category> categories = List.of(firstCategory, secondCategory);

        return new Donation(
                null,
                false,
                user,
                institution,
                categories,
                "123456789",
                "Pickup Comment",
                LocalTime.of(10, 0),
                LocalDate.of(2024, 12, 31),
                "12-345",
                "Sample City",
                "Sample Street",
                5
        );
    }
}