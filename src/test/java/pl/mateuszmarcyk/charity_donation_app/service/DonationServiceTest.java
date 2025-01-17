package pl.mateuszmarcyk.charity_donation_app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.DonationRepository;
import pl.mateuszmarcyk.charity_donation_app.util.event.DonationProcessCompleteEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @InjectMocks
    private DonationService donationService;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Test
    void givenDonationService_whenCountAllDonationsReturnsNull_thenCountIsZero() {

        when(donationRepository.countAll()).thenReturn(null);

        Integer donationCount = donationService.countAllDonations();

        verify(donationRepository, times(1)).countAll();

        assertThat(donationCount).isZero();
    }

    @Test
    void givenDonationService_whenCountAllDonationsReturnsResult_thenResultIsCorrect() {

        when(donationRepository.countAll()).thenReturn(10);

        Integer donationCount = donationService.countAllDonations();

        verify(donationRepository, times(1)).countAll();

        assertThat(donationCount).isEqualTo(10);
    }


    @Test
    void givenDonationService_whenCountAllBagsReturnsNull_thenResultIsZero() {

        when(donationRepository.countAllBags()).thenReturn(null);

        Integer allBagsCountedByService = donationService.countAllBags();

        verify(donationRepository, times(1)).countAllBags();
        assertThat(allBagsCountedByService).isZero();
    }

    @Test
    void givenDonationService_whenCountAllBags_thenResultIsCorrect() {

        when(donationRepository.countAllBags()).thenReturn(10);

        Integer allBagsCountedByService = donationService.countAllBags();

        verify(donationRepository, times(1)).countAllBags();
        assertThat(allBagsCountedByService).isEqualTo(10);
    }

    @Test
    void givenDonationService_WhenSaveDonation_thenSaveMethodInvokedAndDonationProcessCompleteEventPublished() {
        Donation donation = TestDataFactory.getDonation();
        User user = donation.getUser();

        when(donationRepository.save(donation)).thenReturn(donation);
        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);
        ArgumentCaptor<DonationProcessCompleteEvent> argumentCaptor = ArgumentCaptor.forClass(DonationProcessCompleteEvent.class);

        donationService.save(donation);

        verify(donationRepository).save(donationArgumentCaptor.capture());
        verify(publisher).publishEvent(argumentCaptor.capture());

        Donation persistedDonation = donationArgumentCaptor.getValue();
        DonationProcessCompleteEvent event = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(persistedDonation).isEqualTo(donation),
                () -> assertThat(event.getDonation()).isEqualTo(donation),
                () -> assertThat(event.getUser()).isEqualTo(user)
        );

    }

    @Test
    void givenDonationService_whenGetDonationByNullId_thenThrowResourceNotFoundException() {
        Long id = null;

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(donationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donationService.findDonationById(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Ten dar nie istnieje");
        verify(donationRepository, times(1)).findById(argumentCaptor.capture());

        Long idUsedToFindDonation = argumentCaptor.getValue();

        assertThat(idUsedToFindDonation).isEqualTo(id);
    }

    @Test
    void givenDonationService_whenFindDonationById_thenThrowResourceNotFoundException() {
        Long id = 1L;

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(donationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donationService.findDonationById(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Ten dar nie istnieje");
        verify(donationRepository, times(1)).findById(argumentCaptor.capture());

        Long idUsedToFindDonation = argumentCaptor.getValue();

        assertThat(idUsedToFindDonation).isEqualTo(id);
    }

    @Test
    void givenDonationService_whenFindDonationById_thenDonationReturned() {
        Donation donation = new Donation();
        donation.setId(1L);
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(donationRepository.findById(donation.getId())).thenReturn(Optional.of(donation));

        Donation foundDonation = donationService.findDonationById(donation.getId());
        verify(donationRepository, times(1)).findById(argumentCaptor.capture());

        Long idUsedToFindDonation = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(idUsedToFindDonation).isEqualTo(donation.getId()),
                () -> assertThat(foundDonation).isEqualTo(donation)
        );
        assertThat(idUsedToFindDonation).isEqualTo(donation.getId());
    }

    @Test
    void givenDonationService_whenArchiveDonation_thenDonationIsArchived() {
        Donation donation = new Donation();
        donation.setId(1L);
        donation.setReceived(false);

        ArgumentCaptor<Donation> argumentCaptor = ArgumentCaptor.forClass(Donation.class);
        donationService.archiveDonation(donation);

        verify(donationRepository, times(1)).save(argumentCaptor.capture());

        Donation archievedDonation = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(archievedDonation.isReceived()).isTrue(),
                () -> assertThat(archievedDonation.getDonationPassedTime()).isNotNull(),
                () -> assertThat(archievedDonation).isEqualTo(donation)
        );

    }

    @Test
    void givenDonationService_whenGetDonationsForUserSortedByNullSortType_thenFindAllDonationsByUserInvoked() {
        User user = new User();
        user.setId(1L);
        String sortType = null;

        when(donationRepository.findAllDonationsByUser(user)).thenReturn(new ArrayList<>());

        List<Donation> donationsRetrievedByService = donationService.getDonationsForUserSortedBy(sortType, user);

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(donationRepository, times(1)).findAllDonationsByUser(argumentCaptor.capture());

        User userUsedInService = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(donationsRetrievedByService).isEmpty(),
                () -> assertThat(userUsedInService).isEqualTo(user)
        );
    }

    @Test
    void givenDonationService_whenGetDonationsForUserSortedByCreatedSortType_thenFindAllDonationsByUserSortedByCreatedInvoked() {
        User user = new User();
        user.setId(1L);
        String sortType = "created";

        when(donationRepository.findAllDonationsByUserSortedByCreated(user)).thenReturn(new ArrayList<>());

        List<Donation> donationsRetrievedByService = donationService.getDonationsForUserSortedBy(sortType, user);

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(donationRepository, times(1)).findAllDonationsByUserSortedByCreated(argumentCaptor.capture());

        User userUsedInService = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(donationsRetrievedByService).isEmpty(),
                () -> assertThat(userUsedInService).isEqualTo(user)
        );
    }

    @Test
    void givenDonationService_whenGetDonationsForUserSortedByQuantityDescSortType_thenFindAllDonationsByUserSortedByQuantityDescInvoked() {
        User user = new User();
        user.setId(1L);
        String sortType = "quantity desc";

        when(donationRepository.findAllDonationsByUserSortedByQuantityDesc(user)).thenReturn(new ArrayList<>());

        List<Donation> donationsRetrievedByService = donationService.getDonationsForUserSortedBy(sortType, user);

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(donationRepository, times(1)).findAllDonationsByUserSortedByQuantityDesc(argumentCaptor.capture());

        User userUsedInService = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(donationsRetrievedByService).isEmpty(),
                () -> assertThat(userUsedInService).isEqualTo(user)
        );
    }

    @Test
    void givenDonationService_whenGetDonationsForUserSortedByQuantityAscSortType_thenFindAllDonationsByUserSortedByQuantityAscInvoked() {
        User user = new User();
        user.setId(1L);
        String sortType = "quantity asc";

        when(donationRepository.findAllDonationsByUserSortedByQuantityAsc(user)).thenReturn(new ArrayList<>());

        List<Donation> donationsRetrievedByService = donationService.getDonationsForUserSortedBy(sortType, user);

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(donationRepository, times(1)).findAllDonationsByUserSortedByQuantityAsc(argumentCaptor.capture());

        User userUsedInService = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(donationsRetrievedByService).isEmpty(),
                () -> assertThat(userUsedInService).isEqualTo(user)
        );
    }

    @Test
    void givenDonationService_whenGetDonationsForUserSortedByReceivedAscSortType_thenFindAllDonationsByUserSortedByReceivedInvoked() {
        User user = new User();
        user.setId(1L);
        String sortType = "received asc";

        when(donationRepository.findAllDonationsByUserSortedByReceived(user)).thenReturn(new ArrayList<>());

        List<Donation> donationsRetrievedByService = donationService.getDonationsForUserSortedBy(sortType, user);

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(donationRepository, times(1)).findAllDonationsByUserSortedByReceived(argumentCaptor.capture());

        User userUsedInService = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(donationsRetrievedByService).isEmpty(),
                () -> assertThat(userUsedInService).isEqualTo(user)
        );
    }

    @Test
    void givenDonationService_whenGetDonationsForUserSortedByIncorrectSortType_thenFindAllDonationsByUserSortedByUnreceivedInvoked() {
        User user = new User();
        user.setId(1L);
        String sortType = "wrong sort";

        when(donationRepository.findAllDonationsByUserSortedByUnreceived(user)).thenReturn(new ArrayList<>());

        List<Donation> donationsRetrievedByService = donationService.getDonationsForUserSortedBy(sortType, user);

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(donationRepository, times(1)).findAllDonationsByUserSortedByUnreceived(argumentCaptor.capture());

        User userUsedInService = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(donationsRetrievedByService).isEmpty(),
                () -> assertThat(userUsedInService).isEqualTo(user)
        );
    }

    @ParameterizedTest
    @CsvSource({"created", "invalid"})
    void givenDonationService_whenFindAllWithCreatedOrInvalidSortType_thenFindAllDonationsSortedByCreated(String sortType) {
        List<Donation> donationsToReturnByMock = new ArrayList<>();
        when(donationRepository.findAllDonationsSortedByCreated()).thenReturn(donationsToReturnByMock);

        List<Donation> donations = donationService.findAll(sortType);

        verify(donationRepository, times(1)).findAllDonationsSortedByCreated();

        assertIterableEquals(donationsToReturnByMock, donations);
    }

    @Test
    void givenDonationService_whenFindAllWithNullSortType_thenFindDonationsSortedByCreated() {
        String sortType = null;
        List<Donation> donationsToReturnByMock = new ArrayList<>();
        when(donationRepository.findAllDonationsSortedByCreated()).thenReturn(donationsToReturnByMock);

        List<Donation> donations = donationService.findAll(sortType);

        verify(donationRepository, times(1)).findAllDonationsSortedByCreated();

        assertIterableEquals(donationsToReturnByMock, donations);
    }

    @Test
    void givenDonationService_whenFindAllWithQuantityDescSortType_thenFindAllDonationsByQuantityDescInvoked() {
        String sortType = "quantity desc";
        List<Donation> donationsToReturnByMock = new ArrayList<>();
        when(donationRepository.findAllDonationsByQuantityDesc()).thenReturn(donationsToReturnByMock);

        List<Donation> donations = donationService.findAll(sortType);

        verify(donationRepository, times(1)).findAllDonationsByQuantityDesc();

        assertIterableEquals(donationsToReturnByMock, donations);
    }

    @Test
    void givenDonationService_whenFindAllWithQuantityDescSortType_thenFindAllDonationsByQuantityAscInvoked() {
        String sortType = "quantity asc";
        List<Donation> donationsToReturnByMock = new ArrayList<>();
        when(donationRepository.findAllDonationsSortedByQuantityAsc()).thenReturn(donationsToReturnByMock);

        List<Donation> donations = donationService.findAll(sortType);

        verify(donationRepository, times(1)).findAllDonationsSortedByQuantityAsc();

        assertIterableEquals(donationsToReturnByMock, donations);
    }

    @Test
    void givenDonationService_whenFindAllWithReceivedAscSortType_thenFindAllDonationsByReceivedAscInvoked() {
        String sortType = "received asc";
        List<Donation> donationsToReturnByMock = new ArrayList<>();
        when(donationRepository.findAllDonationsSortedByReceivedAsc()).thenReturn(donationsToReturnByMock);

        List<Donation> donations = donationService.findAll(sortType);

        verify(donationRepository, times(1)).findAllDonationsSortedByReceivedAsc();

        assertIterableEquals(donationsToReturnByMock, donations);
    }

    @Test
    void givenDonationService_whenUnArchiveDonation_thenDonationIsUnArchived() {
        Donation donation = new Donation();
        donation.setId(1L);
        donation.setReceived(true);
        donation.setDonationPassedTime(LocalDateTime.now());

        ArgumentCaptor<Donation> argumentCaptor = ArgumentCaptor.forClass(Donation.class);
        donationService.unArchiveDonation(donation);

        verify(donationRepository, times(1)).save(argumentCaptor.capture());

        Donation archievedDonation = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(archievedDonation.isReceived()).isFalse(),
                () -> assertThat(archievedDonation.getDonationPassedTime()).isNull(),
                () -> assertThat(archievedDonation).isEqualTo(donation)
        );

    }

    @Test
    void givenDonationService_whenDeleteDonation_ThenAllAssociationsBrokenAndDeleteMethodInvoked() {

        Donation donation = TestDataFactory.getDonation();
        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);

        donationService.deleteDonation(donation);

        verify(donationRepository, times(1)).delete(donationArgumentCaptor.capture());

        Donation deletedDonation = donationArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(donation.getInstitution().getDonations()).isEmpty(),
                () -> assertThat(donation.getCategories().get(0).getDonations()).isEmpty(),
                () -> assertThat(donation.getUser().getDonations()).isEmpty(),
                () -> assertThat(deletedDonation.getCity()).isEqualTo(donation.getCity()),
                () -> assertThat(deletedDonation.getStreet()).isEqualTo(donation.getStreet()),
                () -> assertThat(deletedDonation.getZipCode()).isEqualTo(donation.getZipCode()),
                () -> assertThat(deletedDonation.getPickUpComment()).isEqualTo(donation.getPickUpComment())
        );
    }

    @Test
    void givenDonationService_whenDeleteDonationWithNullInstitution_ThenAllAssociationsBrokenAndDeleteMethodInvoked() {

        Donation donation = TestDataFactory.getDonation();
        donation.setInstitution(null);
        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);

        donationService.deleteDonation(donation);

        verify(donationRepository, times(1)).delete(donationArgumentCaptor.capture());

        Donation deletedDonation = donationArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(donation.getCategories().get(0).getDonations()).isEmpty(),
                () -> assertThat(donation.getUser().getDonations()).isEmpty(),
                () -> assertThat(deletedDonation.getCity()).isEqualTo(donation.getCity()),
                () -> assertThat(deletedDonation.getStreet()).isEqualTo(donation.getStreet()),
                () -> assertThat(deletedDonation.getZipCode()).isEqualTo(donation.getZipCode()),
                () -> assertThat(deletedDonation.getPickUpComment()).isEqualTo(donation.getPickUpComment())
        );
    }

    @Test
    void givenDonationService_whenDeleteDonationWithNullUser_ThenAllAssociationsBrokenAndDeleteMethodInvoked() {

        Donation donation = TestDataFactory.getDonation();
        donation.setUser(null);
        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);

        donationService.deleteDonation(donation);

        verify(donationRepository, times(1)).delete(donationArgumentCaptor.capture());

        Donation deletedDonation = donationArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(donation.getInstitution().getDonations()).isEmpty(),
                () -> assertThat(donation.getCategories().get(0).getDonations()).isEmpty(),
                () -> assertThat(deletedDonation.getCity()).isEqualTo(donation.getCity()),
                () -> assertThat(deletedDonation.getStreet()).isEqualTo(donation.getStreet()),
                () -> assertThat(deletedDonation.getZipCode()).isEqualTo(donation.getZipCode()),
                () -> assertThat(deletedDonation.getPickUpComment()).isEqualTo(donation.getPickUpComment())
        );
    }

    @Test
    void givenDonationService_whenDeleteDonationWithNullUserAndNullInstitution_ThenAllAssociationsBrokenAndDeleteMethodInvoked() {

        Donation donation = TestDataFactory.getDonation();
        donation.setUser(null);
        donation.setInstitution(null);
        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);

        donationService.deleteDonation(donation);

        verify(donationRepository, times(1)).delete(donationArgumentCaptor.capture());

        Donation deletedDonation = donationArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(donation.getCategories().get(0).getDonations()).isEmpty(),
                () -> assertThat(deletedDonation.getCity()).isEqualTo(donation.getCity()),
                () -> assertThat(deletedDonation.getStreet()).isEqualTo(donation.getStreet()),
                () -> assertThat(deletedDonation.getZipCode()).isEqualTo(donation.getZipCode()),
                () -> assertThat(deletedDonation.getPickUpComment()).isEqualTo(donation.getPickUpComment())
        );
    }

    @Test
    void givenDonationService_whenGetUserDonationById_thenThrowResourceNotFoundException() {
        Long id = 1L;
        User user = new User();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(donationRepository.findUserDonationById(user, id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donationService.getUserDonationById(user, id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Ten dar nie istnieje");
        verify(donationRepository, times(1)).findUserDonationById(userArgumentCaptor.capture(), longArgumentCaptor.capture());

        Long idUsedToFindDonation = longArgumentCaptor.getValue();
        User captureduser = userArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(idUsedToFindDonation).isEqualTo(id),
                () -> assertThat(captureduser).isSameAs(user)
        );
    }

    @Test
    void givenDonationService_whenGetUserDonationById_thenDonationReturned() {
        Long id = 1L;
        User user = new User();
        Donation donation = TestDataFactory.getDonation();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(donationRepository.findUserDonationById(user, id)).thenReturn(Optional.of(donation));

        Donation founddonation  = donationService.getUserDonationById(user, id);
        verify(donationRepository, times(1)).findUserDonationById(userArgumentCaptor.capture(), longArgumentCaptor.capture());

        Long idUsedToFindDonation = longArgumentCaptor.getValue();
        User captureduser = userArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(idUsedToFindDonation).isEqualTo(id),
                () -> assertThat(captureduser).isSameAs(user),
                () -> assertThat(founddonation).isSameAs(donation)
        );
    }

    @Test
    void whenArchiveUserDonation_ThenDonationArchived() {
//        Arrange
        Donation donationToArchive = spy(TestDataFactory.getDonation());
        User user = new User();

        when(donationRepository.findUserDonationById(user, donationToArchive.getId())).thenReturn(Optional.of(donationToArchive));

//        Act
        donationService.archiveUserDonation(donationToArchive.getId(), user);

//        Verify
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(donationRepository, times(1)).findUserDonationById(userArgumentCaptor.capture(), longArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(user);

        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(donationToArchive.getId());

        verify(donationToArchive, times(1)).setReceived(true);
        verify(donationToArchive, times(1)).setDonationPassedTime(any(LocalDateTime.class));

        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);
        verify(donationRepository, times(1)).save(donationArgumentCaptor.capture());
        Donation capturedDonation = donationArgumentCaptor.getValue();
        assertThat(capturedDonation).isSameAs(donationToArchive);
    }
    
    }
