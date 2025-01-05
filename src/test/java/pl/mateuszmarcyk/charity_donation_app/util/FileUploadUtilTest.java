package pl.mateuszmarcyk.charity_donation_app.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadUtilTest {

    @InjectMocks
    private FileUploadUtil fileUploadUtil;

    @Mock
    private UserService userService;

    @Mock
    private MultipartFile multipartFile;


    private Path tempDir;

    @BeforeEach
    void setup() throws IOException {
        tempDir = Files.createTempDirectory("test-upload-dir");
    }

    @AfterEach
    void tearDown() throws IOException {
        deleteDirectoryRecursively(tempDir);
        deleteDirectoryRecursively(Path.of("test-dir"));
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + p, e);
                        }
                    });
        }
    }

    @Test
    void givenFileUploadUtil_whenSaveFile_thenFileIsSaved() throws IOException {
        String filename = "test-file.txt";
        byte[] fileContent = "Test Content".getBytes();
        when(multipartFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(fileContent));

        fileUploadUtil.saveFile(tempDir.toString(), filename, multipartFile);

        Path savedFile = tempDir.resolve(filename);
        assertThat(Files.exists(savedFile)).isTrue();
        assertThat(Files.readAllBytes(savedFile)).isEqualTo(fileContent);
    }

    @Test
    void givenFileUploadUtilAndNoDirectory_whenSaveFile_thenDirectoryCreatedAndFileIsSaved() throws IOException {
        String filename = "test-file.txt";
        Path notExistingDirectory = Path.of("test-dir");
        byte[] fileContent = "Test Content".getBytes();
        when(multipartFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(fileContent));

        fileUploadUtil.saveFile(notExistingDirectory.toString(), filename, multipartFile);

        Path savedFile = notExistingDirectory.resolve(filename);
        assertThat(Files.exists(savedFile)).isTrue();
        assertThat(Files.readAllBytes(savedFile)).isEqualTo(fileContent);
    }

    @Test
    void givenFileUploadUtil_whenSaveFileAndIOExceptionThrown_thenAnotherIOExceptionThrown() throws IOException {
        String filename = "test-file.txt";
        byte[] fileContent = "Test Content".getBytes();

        when(multipartFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(fileContent));

        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {

            filesMockedStatic.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class))).thenThrow(IOException.class);

            assertThatThrownBy(() -> fileUploadUtil.saveFile(tempDir.toString(), filename, multipartFile)).isInstanceOf(IOException.class).hasMessage("Could not save image file: test-file.txt");
        }
    }

    @Test
    void givenFileUploadUtil_whenSaveImage_thenImageIsSavedAndUserServiceCalled() throws IOException {
        UserProfile profile = spy(new UserProfile());
        User user = spy(new User());
        user.setId(1L);
        String originalFilename = "image.jpg";

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserProfile> userProfileArgumentCaptor = ArgumentCaptor.forClass(UserProfile.class);

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("image content".getBytes()));

        assertThatNoException().isThrownBy(() -> fileUploadUtil.saveImage(profile, multipartFile, user));

        verify(profile, times(1)).setProfilePhoto(originalFilename);
        assertThat(profile.getProfilePhoto()).isEqualTo(StringUtils.cleanPath(originalFilename));

        verify(user, times(1)).setProfile(userProfileArgumentCaptor.capture());
        UserProfile capturedProfile = userProfileArgumentCaptor.getValue();
        assertThat(capturedProfile).isEqualTo(profile);

        verify(userService, times(1)).updateUser(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isEqualTo(user);

        Path path = Path.of("photos/users/1/");
        Path savedImage = path.resolve(originalFilename);
        assertThat(Files.exists(savedImage)).isTrue();
    }

    @Test
    void givenFileUploadUtil_whenSaveImageAndSaveFileThrowsException_thenImageIsNotSaved() throws IOException {
        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class))).thenThrow(IOException.class);

            UserProfile profile = spy(new UserProfile());
            User user = spy(new User());
            user.setId(1L);
            String originalFilename = "image.jpg";

            when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
            when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("image content".getBytes()));

            assertThatThrownBy(() -> fileUploadUtil.saveImage(profile, multipartFile, user)).isInstanceOf(IOException.class);
            Path path = Path.of("photos/users/1/");
            Path savedImage = path.resolve(originalFilename);
            assertThat(Files.exists(savedImage)).isFalse();
        }
    }

    @Test
    void givenFileUploadUtil_whenSaveImageWithEmptyFilename_thenNoFileSaved() throws IOException {
        UserProfile profile = mock(UserProfile.class);
        User user = new User();

        when(multipartFile.getOriginalFilename()).thenReturn("");

        fileUploadUtil.saveImage(profile, multipartFile, user);
        verify(profile, never()).setProfilePhoto(any());

        verify(userService, times(1)).updateUser(user);
        assertThat(profile.getProfilePhoto()).isNull();
    }

}

