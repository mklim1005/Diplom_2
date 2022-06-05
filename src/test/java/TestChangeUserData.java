import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TestChangeUserData {
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
    public void testChangeUserDataNameAuthorized() {
        Response responseLogin = given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post("/api/auth/login");
        String accessToken;
        accessToken = responseLogin.body().jsonPath().getString("accessToken");

        String newName = RandomStringUtils.randomAlphabetic(10);
        Response response = given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .and()
                .body("{\"name\":\"" + newName + "\"}")
                .when()
                .patch("/api/auth/user");
        response.then().assertThat().statusCode(200)
                .and()
                .assertThat().body("success", equalTo(true))
                .and()
                .assertThat().body("user.name", equalTo(newName));
    }

    @Test
    public void testChangeUserDataEmailAuthorized() {
        Response responseLogin = given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post("/api/auth/login");
        String accessToken;
        accessToken = responseLogin.body().jsonPath().getString("accessToken");

        String newEmail = RandomStringUtils.randomAlphabetic(10) + "@mailinator.com";
        Response response = given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .and()
                .body("{\"email\":\"" + newEmail + "\"}")
                .when()
                .patch("/api/auth/user");
        response.then().assertThat().statusCode(200)
                .and()
                .assertThat().body("success", equalTo(true))
                .and()
                .assertThat().body("user.email", equalTo(newEmail.toLowerCase()));
    }

    @Test
    public void testChangeDataNotAuthorized() {
        String newName = RandomStringUtils.randomAlphabetic(10);
        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body("{\"name\":\"" + newName + "\"}")
                .when()
                .patch("/api/auth/user");
        response.then().assertThat().statusCode(401)
                .and()
                .assertThat().body("success", equalTo(false))
                .and()
                .assertThat().body("message", equalTo("You should be authorised"));
    }
}
