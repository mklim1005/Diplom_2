package praktikum;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static helpers.AuthHelper.loginAndGetToken;

public class TestChangeUserData {
    public String email = RandomStringUtils.randomAlphabetic(10) + "@mailinator.com";
    public String password = RandomStringUtils.randomAlphabetic(10);
    public String name = RandomStringUtils.randomAlphabetic(10);
    User user;

    @Before
    public void setUp() {
        user = new User(email, password, name);
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/register")
                .then().assertThat().statusCode(200);
    }

    @After
    public void deleteUser() {
        String accessToken = loginAndGetToken(user);
        if (accessToken == null) {
            System.out.println("Skip delete user");
        } else {
            given()
                    .header("Authorization", accessToken)
                    .delete("/api/auth/user")
                    .then().assertThat().statusCode(202);
        }
    }

    @Test
    public void testChangeUserDataNameAuthorized() {
        String accessToken = loginAndGetToken(user);
        String newName = RandomStringUtils.randomAlphabetic(10);

        Response response = given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .body("{\"name\":\"" + newName + "\"}")
                .patch("/api/auth/user");

        response.then()
                .assertThat().statusCode(200)
                .assertThat().body("success", equalTo(true))
                .assertThat().body("user.name", equalTo(newName));
    }

    @Test
    public void testChangeUserDataEmailAuthorized() {
        String accessToken = loginAndGetToken(user);
        String newEmail = RandomStringUtils.randomAlphabetic(10) + "@mailinator.com";

        Response response = given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .body("{\"email\":\"" + newEmail + "\"}")
                .patch("/api/auth/user");

        response.then()
                .assertThat().statusCode(200)
                .assertThat().body("success", equalTo(true))
                .assertThat().body("user.email", equalTo(newEmail.toLowerCase()));
    }

    @Test
    public void testChangeDataNotAuthorized() {
        String newName = RandomStringUtils.randomAlphabetic(10);

        Response response = given()
                .header("Content-type", "application/json")
                .body("{\"name\":\"" + newName + "\"}")
                .patch("/api/auth/user");

        response.then()
                .assertThat().statusCode(401)
                .assertThat().body("success", equalTo(false))
                .assertThat().body("message", equalTo("You should be authorised"));
    }
}
