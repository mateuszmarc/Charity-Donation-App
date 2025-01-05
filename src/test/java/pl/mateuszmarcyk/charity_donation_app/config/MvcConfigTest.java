package pl.mateuszmarcyk.charity_donation_app.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class MvcConfigTest {

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() throws IOException {
        Path photosPath = Paths.get("photos");
        Path imeagePath = Paths.get("photos/test.jpg");
        if (!Files.exists(photosPath)) {
            Files.createDirectories(photosPath);
        }

        if (!Files.exists(imeagePath)) {
            Files.createFile(imeagePath);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        Path path = Paths.get("photos");
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        System.out.println(p);
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + p, e);
                        }
                    });
        }
    }

    @Test
    void givenResourceHandler_whenAccessingResource_thenResourceIsExposed() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(get("/photos/test.jpg"))
                .andExpect(status().isOk());
    }
}