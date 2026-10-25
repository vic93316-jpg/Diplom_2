import clients.ApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class LoginUserTest {

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Проверяет успешную авторизацию с корректными email и паролем")
    public void loginExistingUserTest() {
        String email = "login_" + System.currentTimeMillis() + "@yandex.ru";
        String password = "password123";

        String accessToken = ApiClient.registerUser(email, password, "LoginUser").path("accessToken");
        try {
            ApiClient.loginUser(email, password)
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("success", is(true))
                    .body("accessToken", notNullValue())
                    .body("user.email", equalTo(email));
        } finally {
            if (accessToken != null) {
                ApiClient.deleteUser(accessToken);
            }
        }
    }

    @Test
    @DisplayName("Вход с неверным логином")
    @Description("Проверяет, что при несуществующем email и правильном пароле возвращается ошибка 401")
    public void loginWithInvalidLoginTest() {
        String email = "login_" + System.currentTimeMillis() + "@yandex.ru";
        String password = "password123";

        String accessToken = ApiClient.registerUser(email, password, "LoginUser").path("accessToken");
        try {
            String wrongEmail = "wrong_" + System.currentTimeMillis() + "@yandex.ru";
            ApiClient.loginUser(wrongEmail, password)
                    .then()
                    .statusCode(HttpStatus.SC_UNAUTHORIZED)
                    .body("success", is(false))
                    .body("message", equalTo("email or password are incorrect"));
        } finally {
            if (accessToken != null) {
                ApiClient.deleteUser(accessToken);
            }
        }
    }

    @Test
    @DisplayName("Вход с неверным логином и паролем")
    @Description("Проверяет, что при неверных учетных данных возвращается ошибка 401")
    public void loginWithInvalidCredentialsTest() {
        String email = "login_" + System.currentTimeMillis() + "@yandex.ru";
        String password = "password123";

        String accessToken = ApiClient.registerUser(email, password, "LoginUser").path("accessToken");
        try {
            ApiClient.loginUser("wrong@yandex.ru", "wrongpass")
                    .then()
                    .statusCode(HttpStatus.SC_UNAUTHORIZED)
                    .body("success", is(false))
                    .body("message", equalTo("email or password are incorrect"));
        } finally {
            if (accessToken != null) {
                ApiClient.deleteUser(accessToken);
            }
        }
    }
}