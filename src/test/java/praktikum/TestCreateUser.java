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

public class TestCreateUser {
    public String email = RandomStringUtils.randomAlphabetic(10) + "@mailinator.com";
    public String password = RandomStringUtils.randomAlphabetic(10);
    public String name = RandomStringUtils.randomAlphabetic(10);
    User user;

    @Before
    public void setUp() {
        user = new User(email, password, name);
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
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
    public void testCreateUniqueUser() {
        Response response = given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/register");

        response.then()
                .assertThat().statusCode(200)
                .assertThat().body("success", equalTo(true));
    }

    @Test
    public void testCreateUserThatAlreadyExists() {
        given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/register");

        Response response = given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/register");

        response.then()
                .assertThat().statusCode(403)
                .assertThat().body("success", equalTo(false))
                .assertThat().body("message", equalTo("User already exists"));
    }

    @Test
    public void testCreateUserNotFillEmail() {
        user.setEmail("");
        Response response = given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/register");

        response.then()
                .assertThat().statusCode(403)
                .assertThat().body("success", equalTo(false))
                .assertThat().body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    public void testCreateUserNotFillPassword() {
        user.setPassword("");
        Response response = given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/register");

        response.then()
                .assertThat().statusCode(403)
                .assertThat().body("success", equalTo(false))
                .assertThat().body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    public void testCreateUserNotFillName() {
        user.setName("");
        Response response = given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/register");

        response.then()
                .assertThat().statusCode(403)
                .assertThat().body("success", equalTo(false))
                .assertThat().body("message", equalTo("Email, password and name are required fields"));
    }
}
