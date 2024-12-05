package pl.mateuszmarcyk.charity_donation_app.util;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

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
            throw new IOException("Could not save image file: " + filename, e);
        }
    }

    public void saveImage(UserProfile profile, MultipartFile image, User profileOwner) {
        String imageName = "";
        if (!Objects.equals(image.getOriginalFilename(), "")) {
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            profile.setProfilePhoto(imageName);
        }

        userService.updateUser(profileOwner);

        String imageUploadDir = "photos/users/" + profileOwner.getId();

        try {
            if (!Objects.equals(image.getOriginalFilename(), "")) {
                saveFile(imageUploadDir, imageName, image);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
