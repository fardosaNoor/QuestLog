package ca.sfu.QuestLog;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ProfilePhotoService {

    private final Path uploadDirectory =
            Path.of(System.getProperty("user.home"),
                    ".questlog", "profile-photos");

    public void savePhoto(Long userId, MultipartFile file)
            throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please choose an image.");
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Image must be smaller than 2 MB.");
        }

        String type = file.getContentType();

        if (!"image/jpeg".equals(type)
                && !"image/png".equals(type)) {
            throw new IllegalArgumentException(
                    "Only JPG and PNG images are allowed.");
        }

        BufferedImage image =
                ImageIO.read(file.getInputStream());

        if (image == null
                || image.getWidth() > 3000
                || image.getHeight() > 3000) {
            throw new IllegalArgumentException(
                    "Invalid image or image dimensions too large.");
        }

        Files.createDirectories(uploadDirectory);

        Path destination =
                uploadDirectory.resolve(userId + ".png");

        if (!ImageIO.write(image, "png", destination.toFile())) {
            throw new IOException("Unable to save image.");
        }
    }

    public byte[] getPhoto(Long userId) throws IOException {
        Path path = uploadDirectory.resolve(userId + ".png");

        if (!Files.exists(path)) {
            return null;
        }

        return Files.readAllBytes(path);
    }
}
