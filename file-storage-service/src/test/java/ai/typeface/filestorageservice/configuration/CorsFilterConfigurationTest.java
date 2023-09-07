package ai.typeface.filestorageservice.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CorsFilterConfigurationTest {

    /**
     * This test requires the server to be running to hit it and get the response
     */
    @Test
    public void testCorsConfiguration() {
        String apiUrl = "http://localhost:" + 8081 + "/files";

        try {
            new RestTemplate().exchange(apiUrl, HttpMethod.GET, null, String.class);
        } catch (HttpStatusCodeException e) {
            // If the OPTIONS request fails, it means CORS is not configured correctly
            Assertions.assertEquals(HttpStatus.OK, e.getStatusCode());
        }
    }
}
