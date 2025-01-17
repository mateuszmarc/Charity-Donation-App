package pl.mateuszmarcyk.charity_donation_app.validation;

public class MailMessageTestData {
    public static final String BUILD_MAIL_MESSAGE_EXPECTED_MESSAGE = """
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


    public static final String DONATION_WITH_USER_WITH_NULL_FIRST_NAME_EXPECTED_MESSAGE = """
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


    public static final String DONATION_WITH_USER_FIRST_NAME_EMPTY_EXPECTED_MESSAGE = """
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


    public static final String DONATION_WITH_USER_FIRST_NAME_NOT_EMPTY_EXPECTED_MESSAGE = """
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

    public static final String DONATION_WITH_NULL_DONATION_COMMENT_EXPECTED_MESSAGE = """
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

    public static final String DONATION_WITH_EMPTY_DONATION_COMMENT_EXPECTED_MESSAGE = """
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

    public static final String DONATION_WITH_DONATION_COMMENT_EXPECTED_MESSAGE = """
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
                                    <td>Proszę zadzwonić</td>
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


    public static final String GET_MAIL_MESSAGE_EXPECTED_DATA = """
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
                                    <td>test@gmail.com</td>
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


    public static final String BUILD_PASSWORD_RESET_MESSAGE_EXPECTED_MESSAGE = """
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

}
