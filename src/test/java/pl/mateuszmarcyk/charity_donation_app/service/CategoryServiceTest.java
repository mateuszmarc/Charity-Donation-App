package pl.mateuszmarcyk.charity_donation_app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;
import pl.mateuszmarcyk.charity_donation_app.entity.Category;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.exception.EntityDeletionException;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void givenCategoryService_whenFindAll_thenReturnCategoryList() {

        Category firstCategory = new Category(1L, "Jedzenie", new ArrayList<>());
        Category secondCategory = new Category(2L, "Ubrania", new ArrayList<>());
        List<Category> categories = new ArrayList<>(List.of(firstCategory, secondCategory));

        when(categoryRepository.findAll()).thenReturn(categories);

        List<Category> categoriesReturnedFromService = categoryService.findAll();

        verify(categoryRepository).findAll();

        assertAll(
                () -> assertIterableEquals(categories, categoriesReturnedFromService),
                () -> assertThat(categoriesReturnedFromService).hasSize(2)
        );
    }

    @Test
    void givenCategoryService_whenFindAll_thenReturnEmptyList() {

        List<Category> categories = new ArrayList<>();

        when(categoryRepository.findAll()).thenReturn(categories);

        List<Category> categoriesReturnedFromService = categoryService.findAll();

        verify(categoryRepository).findAll();

        assertAll(
                () -> assertIterableEquals(categories, categoriesReturnedFromService),
                () -> assertThat(categoriesReturnedFromService).isEmpty()
        );
    }

    @Test
    void givenCategoryService_whenFindCategoryByIdFetchDonations_thenGetCategory() {
        Category category = new Category(1L, "Jedzenie", new ArrayList<>(List.of(new Donation(), new Donation())));

        when(categoryRepository.findByIdFetchDonations(category.getId())).thenReturn(Optional.of(category));

        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Category categoryReturnedFromService = categoryService.findByIdFetchDonations(category.getId());

        verify(categoryRepository).findByIdFetchDonations(idArgumentCaptor.capture());

        Long usedId = idArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(usedId).isEqualTo(category.getId()),
                () -> assertThat(categoryReturnedFromService).isEqualTo(category),
                () -> assertThat(categoryReturnedFromService.getDonations()).hasSize(2)
        );
    }

    @Test
    void givenCategoryService_whenFindCategoryByIdFetchDonationsForNullId_thenThrowResourceNotFoundException() {
        Long id = null;

        when(categoryRepository.findByIdFetchDonations(id)).thenReturn(Optional.empty());

        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertThatThrownBy(() -> categoryService.findByIdFetchDonations(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Kategoria nie istnieje");

        verify(categoryRepository).findByIdFetchDonations(idArgumentCaptor.capture());
        Long usedId = idArgumentCaptor.getValue();

        assertThat(usedId).isEqualTo(id);
    }

    @Test
    void givenCategoryService_whenFindCategoryByIdFetchDonationsForId_thenThrowResourceNotFoundException() {
        Long id = 1L;

        when(categoryRepository.findByIdFetchDonations(id)).thenReturn(Optional.empty());

        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertThatThrownBy(() -> categoryService.findByIdFetchDonations(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Kategoria nie istnieje");

        verify(categoryRepository).findByIdFetchDonations(idArgumentCaptor.capture());
        Long usedId = idArgumentCaptor.getValue();

        assertThat(usedId).isEqualTo(id);
    }

    @Test
    void givenCategoryService_whenFindCategoryById_thenCategoryReturned() {
        Category category = new Category(1L, "Jedzenie", new ArrayList<>());

        when(categoryRepository.findByIdFetchDonations(category.getId())).thenReturn(Optional.of(category));

        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Category categoryReturnedFromService = categoryService.findByIdFetchDonations(category.getId());

        verify(categoryRepository).findByIdFetchDonations(idArgumentCaptor.capture());

        Long usedId = idArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(usedId).isEqualTo(category.getId()),
                () -> assertThat(categoryReturnedFromService).isEqualTo(category)
        );
    }

    @Test
    void givenCategoryService_whenFindByNullId_thenThrowResourceNotFoundException() {
        Long id = null;

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertThatThrownBy(() -> categoryService.findCategoryById(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Kategoria nie istnieje");

        verify(categoryRepository).findById(idArgumentCaptor.capture());
        Long usedId = idArgumentCaptor.getValue();

        assertThat(usedId).isEqualTo(id);
    }

    @Test
    void givenCategoryService_whenFindCategoryById_thenThrowResourceNotFoundException() {
        Long id = 1L;

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertThatThrownBy(() -> categoryService.findCategoryById(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Kategoria nie istnieje");

        verify(categoryRepository).findById(idArgumentCaptor.capture());
        Long usedId = idArgumentCaptor.getValue();

        assertThat(usedId).isEqualTo(id);
    }

    @Test
    void givenCategoryService_whenSaveCategory_thenCategorySaved() {
        Category category = new Category(null, "Jedzenie", new ArrayList<>());
        Category savedCategory = new Category(1L, "Jedzenie", new ArrayList<>());

        when(categoryRepository.save(category)).thenReturn(savedCategory);

        ArgumentCaptor<Category> argumentCaptor = ArgumentCaptor.forClass(Category.class);

        Category categorySavedByService = categoryService.save(category);

        verify(categoryRepository).save(argumentCaptor.capture());
        Category categoryParameterForRepositorySaveMethod = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(categorySavedByService).isEqualTo(savedCategory),
                () -> assertThat(categoryParameterForRepositorySaveMethod).isEqualTo(category)
        );
    }

    @Test
    void givenCategoryService_whenDeleteByNullId_thenThrowResourceNotFoundException() {
        Category category = new Category();
        Long categoryId = category.getId();

        when(categoryRepository.findByIdFetchDonations(category.getId())).thenReturn(Optional.empty());

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertThatThrownBy(() -> categoryService.deleteById(categoryId)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Kategoria nie istnieje");
        verify(categoryRepository).findByIdFetchDonations(argumentCaptor.capture());
        verify(categoryRepository, never()).delete(category);
        Long idForSearch = argumentCaptor.getValue();

        assertThat(idForSearch).isEqualTo(category.getId());
    }

    @Test
    void givenCategoryService_whenDeleteById_thenThrowResourceNotFoundException() {
        Category category = new Category();
        category.setId(1L);
        Long categoryId = category.getId();

        when(categoryRepository.findByIdFetchDonations(category.getId())).thenReturn(Optional.empty());

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertThatThrownBy(() -> categoryService.deleteById(categoryId)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Kategoria nie istnieje");
        verify(categoryRepository).findByIdFetchDonations(argumentCaptor.capture());
        verify(categoryRepository, never()).delete(category);
        Long idForSearch = argumentCaptor.getValue();

        assertThat(idForSearch).isEqualTo(category.getId());
    }

    @Test
    void givenCategoryService_whenDeleteByIdCategoryWithDonationsThatHaveOnlyOneCategory_thenThrowEntityDeletionException() {
        Category category = getCategory();
        Long categoryId = category.getId();

        when(categoryRepository.findByIdFetchDonations(category.getId())).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.deleteById(categoryId)).isInstanceOf(EntityDeletionException.class).hasMessage("Do kategorii przypisane są dary");
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);

        verify(categoryRepository, times(1)).findByIdFetchDonations(argumentCaptor.capture());
        verify(categoryRepository, never()).delete(category);
        Long idForSearch = argumentCaptor.getValue();

        assertThat(idForSearch).isEqualTo(category.getId());
    }

    @Test
    void givenCategoryService_whenDeleteByIdCategoryWithDonationsThatHaveMoreThanOneCategory_thenCategoryDeleted() {
        Category category = new Category(1L, "Jedzenie", new ArrayList<>());
        Category secondCategory = new Category(2L, "Ubrania", new ArrayList<>());

        Donation donation = TestDataFactory.getDonationForCategory(category);
        donation.getCategories().add(secondCategory);

        category.setDonations(new ArrayList<>(List.of(donation)));
        secondCategory.setDonations(new ArrayList<>(List.of(donation)));

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Category> categoryArgumentCaptor = ArgumentCaptor.forClass(Category.class);

        when(categoryRepository.findByIdFetchDonations(category.getId())).thenReturn(Optional.of(category));
        categoryService.deleteById(category.getId());

        verify(categoryRepository).findByIdFetchDonations(argumentCaptor.capture());
        verify(categoryRepository, times(1)).delete(categoryArgumentCaptor.capture());

        Long idUsedForSearch = argumentCaptor.getValue();
        Category categoryToDelete = categoryArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(idUsedForSearch).isEqualTo(category.getId()),
                () -> assertThat(categoryToDelete).isEqualTo(category),
                () -> assertThat(category.getDonations().get(0).getCategories()).hasSize(1)
        );
    }

    public static Category getCategory() {
        Category category = new Category(1L, "Jedzenie", new ArrayList<>());

        Donation donation = TestDataFactory.getDonationForCategory(category);
        donation.setId(1L);

        category.setDonations(new ArrayList<>(List.of(donation)));

        return category;

    }
}