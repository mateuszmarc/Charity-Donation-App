package pl.mateuszmarcyk.charity_donation_app.category;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.donation.DonationRepository;
import pl.mateuszmarcyk.charity_donation_app.exception.EntityDeletionException;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DonationRepository donationRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findByIdFetchDonations(Long id) {
        return categoryRepository.findByIdFetchDonations(id).orElseThrow(() -> new ResourceNotFoundException("kategoria nie znaleziona", "Kategoria nie istnieje"));
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("kategoria nie znaleziona", "Kategoria nie istnieje"));
    }

    @Transactional
    public void save(Category category) {
        categoryRepository.save(category);
    }

    @Transactional
    public void deleteById(Long categoryId) {

        Category category = findByIdFetchDonations(categoryId);

        System.out.println(category);
        category.getDonations().forEach(donation -> {
            if (donation.getCategories().size() == 1) {
                System.out.println("This donation has only one category");
                throw new EntityDeletionException("Nie można usunąć kategorii", "Do kategorii przypisane są dary");
            } else {
                donation.removeCategory(category);
                System.out.println("Removing category from donation");
            }
        });


        categoryRepository.delete(category);

    }
}
