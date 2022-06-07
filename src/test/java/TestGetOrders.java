import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


public class TestGetOrders {
    public String email = RandomStringUtils.randomAlphabetic(10) + "@mailinator.com";
    public String password = RandomStringUtils.randomAlphabetic(10);
    public String name = RandomStringUtils.randomAlphabetic(10);
    User user;
    Order order;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        user = new User(email, password, name);
        //register user
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
    public void getOrdersNotAuthorizedUser() {
        Response response1 = given()
                .header("Content-type", "application/json")
                .get("/api/orders");
        response1.then().assertThat().statusCode(401)
                .and()
                .assertThat().body("success", equalTo(false))
                .assertThat().body("message", equalTo("You should be authorised"));
    }

    @Test
    public void getOrderAuthorizedUser() {
        //log In User
        Response responseLogin = given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post("/api/auth/login");
        String accessToken;
        accessToken = responseLogin.body().jsonPath().getString("accessToken");

        // create order
        //get all ingredients and pick 2 random
        Response response = given()
                .header("Content-type", "application/json")
                .get("/api/ingredients");
        response.then().assertThat().statusCode(200)
                .and()
                .assertThat().body("success", equalTo(true));
        //create order with random ingredients
        GetIngredientsResponse getIngredientsResponse = response.body().as(GetIngredientsResponse.class);
        int size = getIngredientsResponse.getData().size();
        int random1 = (int) (Math.random() * (size - 0)) + 0;
        int random2 = (int) (Math.random() * (size - 0)) + 0;
        String firstRandomIngredient = getIngredientsResponse.getData().get(random1).get_id();
        String secondRandomIngredient = getIngredientsResponse.getData().get(random2).get_id();
        ArrayList<String> ingredients = new ArrayList<>();
        ingredients.add(firstRandomIngredient);
        ingredients.add(secondRandomIngredient);
        order = new Order(ingredients);

        //create order for particular user
        given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken)
                .and()
                .body(order)
                .when()
                .post("/api/orders");
        //order
        Response response1 = given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken)
                .get("/api/orders");
        response1.then().assertThat().statusCode(200)
                .assertThat().body("success", equalTo(true))
                .assertThat().body("orders[0].status", equalTo("done"))
                .assertThat().body("orders[0].ingredients[0]", equalTo(firstRandomIngredient))
                .assertThat().body("orders[0].ingredients[1]", equalTo(secondRandomIngredient));
    }
}