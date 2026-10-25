import clients.ApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class CreateOrderTest {

    private static final String INVALID_HASH = "invalid_hash_123";

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

    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Проверяет успешное создание заказа авторизованным пользователем с валидными ингредиентами")
    public void createOrderWithAuthTest() {
        List<String> ingredients = getValidIngredients();
        String email = "order_" + System.currentTimeMillis() + "@yandex.ru";
        String accessToken = ApiClient.registerUser(email, "password123", "OrderUser").path("accessToken");

        try {
            ApiClient.createOrder(Arrays.asList(ingredients.get(0), ingredients.get(1)), accessToken)
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("success", is(true))
                    .body("order.number", notNullValue());
        } finally {
            if (accessToken != null) {
                ApiClient.deleteUser(accessToken);
            }
        }
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

        String email = "order_" + System.currentTimeMillis() + "@yandex.ru";
        String accessToken = ApiClient.registerUser(email, "password123", "OrderUser").path("accessToken");

        try {
            ApiClient.createOrder(Collections.singletonList(ingredientId), accessToken)
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("success", is(true))
                    .body("order.ingredients[0].name", containsString(ingredientName));
        } finally {
            if (accessToken != null) {
                ApiClient.deleteUser(accessToken);
            }
        }
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Проверяет, что при пустом списке ингредиентов возвращается ошибка 400")
    public void createOrderWithoutIngredientsTest() {
        String email = "order_" + System.currentTimeMillis() + "@yandex.ru";
        String accessToken = ApiClient.registerUser(email, "password123", "OrderUser").path("accessToken");

        try {
            ApiClient.createOrder(Collections.emptyList(), accessToken)
                    .then()
                    .statusCode(HttpStatus.SC_BAD_REQUEST)
                    .body("success", is(false))
                    .body("message", equalTo("Ingredient ids must be provided"));
        } finally {
            if (accessToken != null) {
                ApiClient.deleteUser(accessToken);
            }
        }
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Проверяет, что при передаче невалидного хеша ингредиента возвращается ошибка 500")
    public void createOrderWithInvalidIngredientHashTest() {
        String email = "order_" + System.currentTimeMillis() + "@yandex.ru";
        String accessToken = ApiClient.registerUser(email, "password123", "OrderUser").path("accessToken");

        try {
            ApiClient.createOrder(Collections.singletonList(INVALID_HASH), accessToken)
                    .then()
                    .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (accessToken != null) {
                ApiClient.deleteUser(accessToken);
            }
        }
    }
}