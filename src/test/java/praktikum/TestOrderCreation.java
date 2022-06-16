package praktikum;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static helpers.OrderHelper.*;
import static helpers.AuthHelper.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TestOrderCreation {
    public String email = RandomStringUtils.randomAlphabetic(10) + "@mailinator.com";
    public String password = RandomStringUtils.randomAlphabetic(10);
    public String name = RandomStringUtils.randomAlphabetic(10);
    User user;
    Order order;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        user = new User(email, password, name);
        register(user).then().assertThat().statusCode(200);

        String firstRandomIngredient = getRandomIngredientUuid();
        String secondRandomIngredient = getRandomIngredientUuid();
        ArrayList<String> ingredients = new ArrayList<>();
        ingredients.add(firstRandomIngredient);
        ingredients.add(secondRandomIngredient);
        order = new Order(ingredients);
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
    public void createOrderWithoutAuthorization() {
        Response response = given()
                .header("Content-type", "application/json")
                .body(order)
                .post("/api/orders");

        response.then()
                .assertThat().statusCode(200)
                .assertThat().body("success", equalTo(true))
                .assertThat().body("$", hasKey("name"))
                .assertThat().body("order", hasKey("number"))
                .assertThat().body("order", not(hasKey("ingredients")))
                .assertThat().body("order", not(hasKey("price")));
    }

    @Test
    public void createOrderWithAuthorization() {
        String accessToken = loginAndGetToken(user);

        Response response = given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken)
                .body(order)
                .post("/api/orders");

        response.then()
                .assertThat().statusCode(200)
                .assertThat().body("success", equalTo(true))
                .assertThat().body("$", hasKey("name"))
                .assertThat().body("order", hasKey("number"))
                .assertThat().body("order", hasKey("ingredients"))
                .assertThat().body("order", hasKey("price"))
                .assertThat().body("order", hasKey("status"));
    }

    @Test
    public void testCreateOrderWithoutIngredients() {
        order.setIngredients(new ArrayList<>());

        Response response = given()
                .header("Content-type", "application/json")
                .body(order)
                .post("/api/orders");

        response.then()
                .assertThat().statusCode(400)
                .assertThat().body("success", equalTo(false))
                .assertThat().body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    public void testCreateOrderWithWrongHashIngredients() {
        ArrayList<String> ingredients = new ArrayList<>();
        ingredients.add("vinni");
        ingredients.add("puh");
        order.setIngredients(ingredients);

        Response response = given()
                .header("Content-type", "application/json")
                .body(order)
                .post("/api/orders");

        response.then().assertThat().statusCode(500);
    }
}
