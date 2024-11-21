USE charity_donation_app;
INSERT INTO `user_types` VALUES (1,'ROLE_USER'),(2,'ROLE_ADMIN');


# Add categories
INSERT INTO categories (name) VALUES ('Jedzenie');
INSERT INTO categories (name) VALUES ('Zabawki');
INSERT INTO categories (name) VALUES ('Ubrania');
INSERT INTO categories (name) VALUES ('Książki');
INSERT INTO categories (name) VALUES ('Elektronika');
INSERT INTO categories (name) VALUES ('Meble');
INSERT INTO categories (name) VALUES ('Produkty Higieniczne');
INSERT INTO categories (name) VALUES ('Przybory Szkolne');
INSERT INTO categories (name) VALUES ('Produkty dla Dzieci');
INSERT INTO categories (name) VALUES ('Sprzęt Sportowy');



# add institutions
INSERT INTO institutions (name, description) VALUES ('Pomocna Dłoń', 'Zapewniamy żywność i schronienie potrzebującym');
INSERT INTO institutions (name, description) VALUES ('Akcja Zabawka', 'Przekazujemy zabawki dzieciom z ubogich rodzin');
INSERT INTO institutions (name, description) VALUES ('Bank Odzieży', 'Oferujemy darmową odzież dla potrzebujących');
INSERT INTO institutions (name, description) VALUES ('Książkowa Przystań', 'Przekazujemy książki do szkół i bibliotek w ubogich regionach');
INSERT INTO institutions (name, description) VALUES ('Technologia dla Wszystkich', 'Zapewniamy odnowiony sprzęt elektroniczny uczniom i rodzinom');
INSERT INTO institutions (name, description) VALUES ('Bohaterowie Higieny', 'Rozdajemy produkty higieniczne do schronisk dla bezdomnych');
INSERT INTO institutions (name, description) VALUES ('Sport dla Dzieci', 'Wspieramy młodzieżowy sport, przekazując sprzęt i zasoby');
