package pl.mateuszmarcyk.charity_donation_app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.InstitutionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceTest {

    @InjectMocks
    private InstitutionService institutionService;

    @Mock
    private InstitutionRepository institutionRepository;

    @Test
    void givenInstitutionService_whenFindAll_thenFindAllInvoked() {
        List<Institution> institutions = new ArrayList<>(List.of(
                new Institution(1L, "Test 1", "Description 1", new ArrayList<>()),
                new Institution(2L, "Test 2", "Description 2", new ArrayList<>())
        ));

        when(institutionRepository.findAll()).thenReturn(institutions);

        List<Institution> serviceInstitutions = institutionService.findAll();

        verify(institutionRepository, times(1)).findAll();

        assertAll(
                () -> assertThat(serviceInstitutions).hasSize(2),
                () -> assertIterableEquals(institutions, serviceInstitutions)
        );
    }

    @Test
    void givenInstitutionRepository_whenFindById_thenFindInstitutionByIdInvokedAndInstitutionReturned() {
        Institution institution = new Institution(1L, "Test 1", "Description 1", new ArrayList<>());

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(institutionRepository.findById(institution.getId())).thenReturn(Optional.of(institution));

        Institution foundInstitution = institutionService.findInstitutionById(institution.getId());

        verify(institutionRepository, times(1)).findById(argumentCaptor.capture());
        Long userId = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(userId).isEqualTo(institution.getId()),
                () -> assertThat(foundInstitution).isEqualTo(institution)
        );
    }

    @Test
    void givenInstitutionRepository_whenFindById_thenFindInstitutionByIdInvokedAndResourceNotFoundExceptionThrown() {
        Long id = 1L;

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(institutionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> institutionService.findInstitutionById(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Nie znaleziono instytucji z takim id");

        verify(institutionRepository, times(1)).findById(argumentCaptor.capture());
        Long usedId = argumentCaptor.getValue();
        assertThat(usedId).isEqualTo(id);
    }

    @Test
    void givenInstitutionService_thenSaveInstitution_thenSaveMethodInvoked() {
        Institution institution = new Institution(1L, "Test 1", "Description 1", new ArrayList<>());

        ArgumentCaptor<Institution> argumentCaptor = ArgumentCaptor.forClass(Institution.class);
        institutionService.saveInstitution(institution);

        verify(institutionRepository, times(1)).save(argumentCaptor.capture());
        Institution savedInstitution = argumentCaptor.getValue();

        assertThat(savedInstitution).isEqualTo(institution);
    }

    @Test
    void givenInstitutionService_whenDeleteById_thenDeleteIntitutionInvoked() {
        Donation firstDonation = new Donation();
        firstDonation.setId(1L);
        Donation secondDonation = new Donation();
        secondDonation.setId(2L);

        Institution institution = new Institution(1L, "Test 1", "Description 1", new ArrayList<>(List.of(firstDonation, secondDonation)));
        firstDonation.setInstitution(institution);
        secondDonation.setInstitution(institution);

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Institution> institutionArgumentCaptor = ArgumentCaptor.forClass(Institution.class);
        when(institutionRepository.findById(institution.getId())).thenReturn(Optional.of(institution));

        institutionService.deleteIntitutionById(institution.getId());

        verify(institutionRepository).findById(argumentCaptor.capture());
        Long idToFindInstitution = argumentCaptor.getValue();

        verify(institutionRepository).delete(institutionArgumentCaptor.capture());
        Institution deletedInstitution = institutionArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(firstDonation.getInstitution()).isNull(),
                () -> assertThat(secondDonation.getInstitution()).isNull(),
                () -> assertThat(idToFindInstitution).isEqualTo(institution.getId()),
                () -> assertThat(deletedInstitution).isEqualTo(institution)
        );
    }

    @Test
    void givenInstitutionService_whenDeleteById_thenResourceNotFoundExceptionThrownAndDeleteIntitutionNotInvoked() {
        Institution institution = new Institution(1L, "Test 1", "Description 1", new ArrayList<>());

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(institutionRepository.findById(institution.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> institutionService.findInstitutionById(institution.getId())).isInstanceOf(ResourceNotFoundException.class).hasMessage("Nie znaleziono instytucji z takim id");

        verify(institutionRepository, times(1)).findById(argumentCaptor.capture());

        verify(institutionRepository, never()).delete(institution);

        Long usedId = argumentCaptor.getValue();
        assertThat(usedId).isEqualTo(institution.getId());
    }
}