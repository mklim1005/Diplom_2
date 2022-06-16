package praktikum;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static helpers.AuthHelper.*;
import static helpers.OrderHelper.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TestGetOrders {
    public String email = RandomStringUtils.randomAlphabetic(10) + "@mailinator.com";
    public String password = RandomStringUtils.randomAlphabetic(10);
    public String name = RandomStringUtils.randomAlphabetic(10);
    User user;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        user = new User(email, password, name);
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
    public void getOrdersNotAuthorizedUser() {
        Response response = given()
                .header("Content-type", "application/json")
                .get("/api/orders");

        response.then().assertThat().statusCode(401)
                .assertThat().body("success", equalTo(false))
                .assertThat().body("message", equalTo("You should be authorised"));
    }

    @Test
    public void getOrderAuthorizedUser() {
        String accessToken = loginAndGetToken(user);
        String firstIngredientUuid = getRandomIngredientUuid();
        String secondIngredientUuid = getRandomIngredientUuid();
        ArrayList<String> ingredients = new ArrayList<>();
        ingredients.add(firstIngredientUuid);
        ingredients.add(secondIngredientUuid);
        createOrderForUser(accessToken, ingredients);

        Response response = given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken)
                .get("/api/orders");

        response.then()
                .assertThat().statusCode(200)
                .assertThat().body("success", equalTo(true))
                .assertThat().body("orders[0].status", equalTo("done"))
                .assertThat().body("orders[0].ingredients[0]", equalTo(firstIngredientUuid))
                .assertThat().body("orders[0].ingredients[1]", equalTo(secondIngredientUuid));
    }
}
