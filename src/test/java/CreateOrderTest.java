import clients.ApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class CreateOrderTest {

    private static final String INVALID_HASH = "invalid_hash_123";

    private String accessToken;

    @After
    public void tearDown() {
        if (accessToken != null) {
            ApiClient.deleteUser(accessToken);
            accessToken = null;
        }
    }

    private static List<String> getValidIngredients() {
        return ApiClient.getIngredients().path("data._id");
    }

    private static Map<String, String> getIngredientIdToName() {
        List<Map<String, String>> data = ApiClient.getIngredients().jsonPath().getList("data");
        Map<String, String> map = new HashMap<>();
        for (Map<String, String> ingredient : data) {
            map.put(ingredient.get("_id"), ingredient.get("name"));
        }
        return map;
    }

    private String registerOrderUser() {
        String email = "order_" + System.currentTimeMillis() + "@yandex.ru";
        return ApiClient.registerUser(email, "password123", "OrderUser").path("accessToken");
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Проверяет успешное создание заказа авторизованным пользователем с валидными ингредиентами")
    public void createOrderWithAuthTest() {
        List<String> ingredients = getValidIngredients();
        accessToken = registerOrderUser();

        ApiClient.createOrder(Arrays.asList(ingredients.get(0), ingredients.get(1)), accessToken)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("success", is(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Проверяет успешное создание заказа без передачи токена авторизации")
    public void createOrderWithoutAuthTest() {
        List<String> ingredients = getValidIngredients();

        ApiClient.createOrder(Collections.singletonList(ingredients.get(0)), null)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("success", is(true));
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами")
    @Description("Проверяет, что при передаче ингредиента он попадает в заказ")
    public void createOrderWithIngredientsTest() {
        List<String> ingredients = getValidIngredients();
        Map<String, String> idToName = getIngredientIdToName();
        String ingredientId = ingredients.get(0);
        String ingredientName = idToName.get(ingredientId);

        accessToken = registerOrderUser();

        ApiClient.createOrder(Collections.singletonList(ingredientId), accessToken)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("success", is(true))
                .body("order.ingredients[0].name", containsString(ingredientName));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Проверяет, что при пустом списке ингредиентов возвращается ошибка 400")
    public void createOrderWithoutIngredientsTest() {
        accessToken = registerOrderUser();

        ApiClient.createOrder(Collections.emptyList(), accessToken)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("success", is(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Проверяет, что при передаче невалидного хеша ингредиента возвращается ошибка 500")
    public void createOrderWithInvalidIngredientHashTest() {
        accessToken = registerOrderUser();

        ApiClient.createOrder(Collections.singletonList(INVALID_HASH), accessToken)
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
}