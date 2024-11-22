package pl.mateuszmarcyk.charity_donation_app.util;

import org.springframework.stereotype.Component;

@Component
public class RegistrationMail {
    public static final String REGISTRATION_MESSAGE = """
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
                                 <a href="%s">Zweryfikuj swoje konto klikając w link</a>
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

    public static String buildMessage(String url) {
        return REGISTRATION_MESSAGE.formatted(url);
    }
}
