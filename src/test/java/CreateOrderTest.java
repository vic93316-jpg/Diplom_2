import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateOrderTest extends BaseTest {

    private String accessToken;
    private List<String> validIngredients;
    private Map<String, String> ingredientIdToName;
    private final String invalidHash = "invalid_hash_123";

    @Before
    public void setUp() {

        Response ingredientsResponse = given().spec(getRequestSpec()).when().get("/api/ingredients");
        validIngredients = ingredientsResponse.path("data._id");


        List<Map<String, String>> ingredientsData = ingredientsResponse.jsonPath().getList("data");
        ingredientIdToName = new HashMap<>();
        for (Map<String, String> ingredient : ingredientsData) {
            ingredientIdToName.put(ingredient.get("_id"), ingredient.get("name"));
        }


        String email = "order_" + System.currentTimeMillis() + "@yandex.ru";
        String regBody = String.format(
                "{\"email\": \"%s\", \"password\": \"password123\", \"name\": \"OrderUser\"}", email);
        Response regResponse = given().spec(getRequestSpec()).body(regBody).when().post("/api/auth/register");
        accessToken = regResponse.path("accessToken");
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Проверяет успешное создание заказа авторизованным пользователем с валидными ингредиентами")
    public void createOrderWithAuthTest() {
        String orderBody = String.format("{\"ingredients\": [\"%s\", \"%s\"]}",
                validIngredients.get(0), validIngredients.get(1));

        given()
                .spec(getRequestSpec())
                .header("Authorization", accessToken)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Проверяет успешное создание заказа без передачи токена авторизации")
    public void createOrderWithoutAuthTest() {
        String orderBody = String.format("{\"ingredients\": [\"%s\"]}", validIngredients.get(0));

        given()
                .spec(getRequestSpec())
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами")
    @Description("Проверяет, что при передаче ингредиента он попадает в заказ (имя ингредиента присутствует в названии заказа)")
    public void createOrderWithIngredientsTest() {
        String ingredientId = validIngredients.get(0);
        String ingredientName = ingredientIdToName.get(ingredientId);
        String orderBody = String.format("{\"ingredients\": [\"%s\"]}", ingredientId);

        given()
                .spec(getRequestSpec())
                .header("Authorization", accessToken)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("order.ingredients[0].name", containsString(ingredientName));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Проверяет, что при пустом списке ингредиентов возвращается ошибка 400")
    public void createOrderWithoutIngredientsTest() {
        String orderBody = "{\"ingredients\": []}";

        given()
                .spec(getRequestSpec())
                .header("Authorization", accessToken)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400)
                .body("success", is(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Проверяет, что при передаче невалидного хеша ингредиента возвращается ошибка 500")
    public void createOrderWithInvalidIngredientHashTest() {
        String orderBody = String.format("{\"ingredients\": [\"%s\"]}", invalidHash);

        given()
                .spec(getRequestSpec())
                .header("Authorization", accessToken)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(500);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            given().spec(getRequestSpec())
                    .header("Authorization", accessToken)
                    .when().delete("/api/auth/user");
        }
    }
}