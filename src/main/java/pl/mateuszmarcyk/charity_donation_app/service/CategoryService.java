package pl.mateuszmarcyk.charity_donation_app.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.entity.Category;
import pl.mateuszmarcyk.charity_donation_app.exception.EntityDeletionException;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.CategoryRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category findByIdFetchDonations(Long id) {
        return categoryRepository.findByIdFetchDonations(id).orElseThrow(() -> new ResourceNotFoundException("kategoria nie znaleziona", "Kategoria nie istnieje"));
    }

    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Kategoria nie znaleziona", "Kategoria nie istnieje"));
    }

    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteById(Long categoryId) {

        Category category = findByIdFetchDonations(categoryId);


        log.info("{}", category);
        category.getDonations().forEach(donation -> {
            if (donation.getCategories().size() == 1) {
                log.info("This donation has only one category");
                throw new EntityDeletionException("Nie można usunąć kategorii", "Do kategorii przypisane są dary");
            } else {
                donation.removeCategory(category);
                log.info("Removing category from donation");
            }
        });


        categoryRepository.delete(category);

    }
}
