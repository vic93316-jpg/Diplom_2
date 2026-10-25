import clients.ApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class LoginUserTest {

    private String email;
    private String password;
    private String accessToken;

    @Before
    public void setUp() {
        email = "login_" + System.currentTimeMillis() + "@yandex.ru";
        password = "password123";
        accessToken = ApiClient.registerUser(email, password, "LoginUser").path("accessToken");
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            ApiClient.deleteUser(accessToken);
            accessToken = null;
        }
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Проверяет успешную авторизацию с корректными email и паролем")
    public void loginExistingUserTest() {
        ApiClient.loginUser(email, password)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("success", is(true))
                .body("accessToken", notNullValue())
                .body("user.email", equalTo(email));
    }

    @Test
    @DisplayName("Вход с неверным логином")
    @Description("Проверяет, что при несуществующем email и правильном пароле возвращается ошибка 401")
    public void loginWithInvalidLoginTest() {
        String wrongEmail = "wrong_" + System.currentTimeMillis() + "@yandex.ru";

        ApiClient.loginUser(wrongEmail, password)
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным логином и паролем")
    @Description("Проверяет, что при неверных учетных данных возвращается ошибка 401")
    public void loginWithInvalidCredentialsTest() {
        ApiClient.loginUser("wrong@yandex.ru", "wrongpass")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}