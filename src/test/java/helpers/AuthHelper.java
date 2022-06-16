package helpers;

import io.restassured.response.Response;
import praktikum.User;

import static io.restassured.RestAssured.given;

public class AuthHelper {

    public static Response register(User user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/register");
    }

    public static Response login(User user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/login");
    }

    public static String loginAndGetToken(User user) {
        Response response = login(user);
        return response.body().jsonPath().getString("accessToken");
    }
}
