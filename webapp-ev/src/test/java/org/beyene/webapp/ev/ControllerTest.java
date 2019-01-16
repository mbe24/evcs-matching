package org.beyene.webapp.ev;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerTest {

    private static final String PATH = "/api/v1";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testGetHello() {
        String name = "John Doe";
        String body = this.restTemplate.getForObject(PATH + "/hello?name=" + name, String.class);
        Assert.assertThat(body, Matchers.is(String.format("Hello %s!", name)));
    }

    @Test
    public void testGetTest() {
        String body = this.restTemplate.getForObject(PATH + "/test?name=throw", String.class);
        Assert.assertThat(body, Matchers.is("THROW is not a valid argument!"));
    }

}
