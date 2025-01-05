package pl.mateuszmarcyk.charity_donation_app.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.exception.SaveException;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileUploadUtil {

    private final UserService userService;

    public void saveFile(String uploadDir, String filename, MultipartFile multipartFile) throws IOException {

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path path = uploadPath.resolve(filename);
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.info(e.getMessage());
            throw new SaveException("Nie da się zapisać: " + filename, "Błąd zapisu");
        }
    }

    public void saveImage(UserProfile profile, MultipartFile image, User profileOwner) throws IOException {
        String imageName = "";
        if (!Objects.equals(image.getOriginalFilename(), "")) {
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            profile.setProfilePhoto(imageName);
        }

        profileOwner.setProfile(profile);
        userService.updateUser(profileOwner);

        String imageUploadDir = "photos/users/" + profileOwner.getId();
        if (!Objects.equals(image.getOriginalFilename(), "")) {
                saveFile(imageUploadDir, imageName, image);
            }
    }
}
