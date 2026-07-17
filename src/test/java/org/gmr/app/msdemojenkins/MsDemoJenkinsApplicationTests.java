package org.gmr.app.msdemojenkins;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class MsDemoJenkinsApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainShouldStartApplication() {
        assertDoesNotThrow(() ->
                MsDemoJenkinsApplication.main(new String[]{}));
    }

}
