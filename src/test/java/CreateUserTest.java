import clients.ApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class CreateUserTest {

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Проверяет успешную регистрацию нового пользователя с уникальными данными")
    public void createUniqueUserTest() {
        String email = "test_" + System.currentTimeMillis() + "@yandex.ru";

        Response response = ApiClient.registerUser(email, "password123", "TestUser");

        String accessToken = null;
        try {
            response.then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("success", is(true))
                    .body("user.email", equalTo(email))
                    .body("accessToken", notNullValue());
            accessToken = response.path("accessToken");
        } finally {
            if (accessToken != null) {
                ApiClient.deleteUser(accessToken);
            }
        }
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя")
    @Description("Проверяет, что при попытке регистрации с существующим email возвращается ошибка 403")
    public void createExistingUserTest() {
        String email = "existing_" + System.currentTimeMillis() + "@yandex.ru";

        String accessToken = ApiClient
                .registerUser(email, "password123", "Existing")
                .path("accessToken");

        try {
            ApiClient.registerUser(email, "password123", "Existing")
                    .then()
                    .statusCode(HttpStatus.SC_FORBIDDEN)
                    .body("success", is(false))
                    .body("message", equalTo("User already exists"));
        } finally {
            if (accessToken != null) {
                ApiClient.deleteUser(accessToken);
            }
        }
    }

    @Test
    @DisplayName("Создание пользователя без логина")
    @Description("Проверяет, что при отсутствии email (логина) возвращается ошибка 403")
    public void createUserWithoutLoginTest() {
        ApiClient.registerUser(null, "password123", "NoLogin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", containsString("required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    @Description("Проверяет, что при отсутствии password возвращается ошибка 403")
    public void createUserWithoutPasswordTest() {
        String email = "nopass_" + System.currentTimeMillis() + "@yandex.ru";

        ApiClient.registerUser(email, null, "NoPassword")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", containsString("required fields"));
    }


    @Test
    @DisplayName("Создание пользователя без имени")
    @Description("Проверяет, что при отсутствии name (Username) возвращается ошибка 403")
    public void createUserWithoutNameTest() {
        String email = "noname_" + System.currentTimeMillis() + "@yandex.ru";

        ApiClient.registerUser(email, "password123", null)
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", containsString("required fields"));
    }
}