
package ca.sfu.QuestLog;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePhotoTest {

    private final ProfilePhotoService service =
            new ProfilePhotoService();

    // Helper: create a real PNG image
    private byte[] createPng() throws Exception {

        BufferedImage image = new BufferedImage(
                10, 10, BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        ImageIO.write(image, "png", output);

        return output.toByteArray();
    }

    // TEST 1: Valid image uploads successfully
    @Test
    void validImageCanBeUploaded() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "photo",
                "avatar.png",
                "image/png",
                createPng()
        );

        service.savePhoto(-10001L, file);

        byte[] savedPhoto = service.getPhoto(-10001L);

        assertNotNull(savedPhoto);
        assertTrue(savedPhoto.length > 0);
    }

    // TEST 2: Empty images are rejected
    @Test
    void emptyImageIsRejected() {

        MockMultipartFile file = new MockMultipartFile(
                "photo",
                "empty.png",
                "image/png",
                new byte[0]
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.savePhoto(-10002L, file)
        );
    }

    // TEST 3: Non-image files are rejected
    @Test
    void nonImageFileIsRejected() {

        MockMultipartFile file = new MockMultipartFile(
                "photo",
                "notes.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.savePhoto(-10003L, file)
        );
    }

    // TEST 4: Images larger than 2 MB are rejected
    @Test
    void oversizedImageIsRejected() {

        byte[] largeFile = new byte[2 * 1024 * 1024 + 1];

        MockMultipartFile file = new MockMultipartFile(
                "photo",
                "large.png",
                "image/png",
                largeFile
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.savePhoto(-10004L, file)
        );
    }

    // TEST 5: Fake image contents are rejected
    @Test
    void fakeImageContentsAreRejected() {

        MockMultipartFile file = new MockMultipartFile(
                "photo",
                "fake.png",
                "image/png",
                "not a real image".getBytes()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.savePhoto(-10005L, file)
        );
    }
}
