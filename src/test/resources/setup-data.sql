-- Insert Institutions
INSERT INTO institutions (id, name, description)
VALUES
    (1, 'Pomocna Dłoń', 'Zapewniamy żywność i schronienie potrzebującym'),
    (2, 'Akcja Zabawka', 'Przekazujemy zabawki dzieciom z ubogich rodzin'),
    (3, 'Bank Odzieży', 'Oferujemy darmową odzież dla potrzebujących'),
    (4, 'Książkowa Przystań', 'Przekazujemy książki do szkół i bibliotek w ubogich regionach'),
    (5, 'Technologia dla Wszystkich', 'Zapewniamy odnowiony sprzęt elektroniczny uczniom i rodzinom'),
    (6, 'Bohaterowie Higieny', 'Rozdajemy produkty higieniczne do schronisk dla bezdomnych'),
    (7, 'Sport dla Dzieci', 'Wspieramy młodzieżowy sport, przekazując sprzęt i zasoby');

-- Insert Categories
INSERT INTO categories (id, name)
VALUES
    (1, 'Jedzenie'),
    (2, 'Zabawki'),
    (3, 'Ubrania'),
    (4, 'Książki'),
    (5, 'Elektronika'),
    (6, 'Meble'),
    (7, 'Produkty Higieniczne'),
    (8, 'Przybory Szkolne'),
    (9, 'Produkty dla Dzieci'),
    (10, 'Sprzęt Sportowy');

-- Insert Users
INSERT INTO users (id, email, is_active, blocked, password, registration_date_time)
VALUES
    (2, 'testuser@example.com', true, false, 'P@ssword123', '2024-12-24 12:00:00');

-- Insert Donations
INSERT INTO donations (
    id,
    quantity,
    street,
    city,
    zip_code,
    pick_up_date,
    pick_up_time,
    pick_up_comment,
    phone_number,
    institution_id,
    user_id,
    created,
    received,
    donation_passed_time
) VALUES (
             1,
             5,
             '123 Charity Lane',
             'Kindness City',
             '12-345',
             '2024-12-31',
             '10:30:00',
             'Please call on arrival.',
             '123456789',
             1,
             1,
             '2024-12-24 12:00:00',
             false,
             NULL
         );

-- Insert Donation Categories
INSERT INTO donations_categories (donation_id, category_id)
VALUES (1, 1);
