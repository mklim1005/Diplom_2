package praktikum;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static helpers.AuthHelper.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TestLogInUser {
    public String email = RandomStringUtils.randomAlphabetic(10) + "@mailinator.com";
    public String password = RandomStringUtils.randomAlphabetic(10);
    public String name = RandomStringUtils.randomAlphabetic(10);
    User user;

    @Before
    public void setUp() {
        user = new User(email, password, name);
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
     register(user).then().assertThat().statusCode(200);
    }

    @After
    public void deleteUser() {
        String accessToken = loginAndGetToken(user);

        if (accessToken == null) {
            System.out.println("Skip delete user");
        } else {
            delete(accessToken).then().assertThat().statusCode(202);
        }
    }

    @Test
    public void testLogInExistingUser() {
        Response responseLogin = login(user);

        responseLogin.then()
                .assertThat().statusCode(200)
                .assertThat().body("user.email", equalTo(user.getEmail().toLowerCase()))
                .assertThat().body("user.name", equalTo(user.getName()));

    }

    @Test
    public void testLogInWrongEmail() {
        user.setEmail("");
        Response responseLogin = login(user);

        responseLogin.then()
                .assertThat().statusCode(401)
                .assertThat().body("success", equalTo(false))
                .assertThat().body("message", equalTo("email or password are incorrect"));
    }

    @Test
    public void testLogInWrongPassword() {
        user.setPassword("");
        Response responseLogin = login(user);

        responseLogin.then()
                .assertThat().statusCode(401)
                .assertThat().body("success", equalTo(false))
                .assertThat().body("message", equalTo("email or password are incorrect"));
    }
}
