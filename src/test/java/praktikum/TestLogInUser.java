package praktikum;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


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
        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post("/api/auth/register");
        response.then().assertThat().statusCode(200)
                .and()
                .assertThat().body("success", equalTo(true));
    }

    @After
    public void deleteUser() {
        Response responseLogin = given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post("/api/auth/login");
        String accessToken;
        accessToken = responseLogin.body().jsonPath().getString("accessToken");

        if (accessToken == null) {
            System.out.println("Skip delete user");
        } else {
            Response responseDelete = given()
                    .header("Authorization", accessToken)
                    .delete("/api/auth/user");
            responseDelete.then().assertThat().statusCode(202)
                    .and()
                    .assertThat().body("success", equalTo(true))
                    .and()
                    .assertThat().body("message", equalTo("User successfully removed"));
        }
    }

    @Test
    public void testLogInExistingUser() {
        Response responseLogin = given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post("/api/auth/login");
        responseLogin.then().assertThat().statusCode(200)
                .and()
                .assertThat().body("user.email", equalTo(user.getEmail().toLowerCase()))
                .and()
                .assertThat().body("user.name", equalTo(user.getName()));

    }

    @Test
    public void testLogInWrongEmail() {
        user.setEmail("");
        Response responseLogin = given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post("/api/auth/login");
        responseLogin.then().assertThat().statusCode(401)
                .and()
                .assertThat().body("success", equalTo(false))
                .and()
                .assertThat().body("message", equalTo("email or password are incorrect"));
    }

    @Test
    public void testLogInWrongPassword() {
        user.setPassword("");
        Response responseLogin = given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post("/api/auth/login");
        responseLogin.then().assertThat().statusCode(401)
                .and()
                .assertThat().body("success", equalTo(false))
                .and()
                .assertThat().body("message", equalTo("email or password are incorrect"));
    }
}
