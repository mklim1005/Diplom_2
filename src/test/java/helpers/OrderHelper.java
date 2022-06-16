package helpers;

import io.restassured.response.Response;
import praktikum.GetIngredientsResponse;
import praktikum.Order;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;

public class OrderHelper {
    public static String getRandomIngredientUuid() {
        Response response = given()
                .header("Content-type", "application/json")
                .get("/api/ingredients");
        GetIngredientsResponse getIngredientsResponse = response.body().as(GetIngredientsResponse.class);
        int size = getIngredientsResponse.getData().size();
        int random = (int) (Math.random() * size);
        return getIngredientsResponse.getData().get(random).get_id();
    }

    public static void createOrderForUser(String accessToken, ArrayList<String> ingredients) {
        Order order = new Order(ingredients);
        given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken)
                .body(order)
                .post("/api/orders");
    }

}
