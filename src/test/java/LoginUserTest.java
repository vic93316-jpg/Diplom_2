import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.Before;
import org.junit.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class LoginUserTest extends BaseTest {

    private String email;
    private String password;

    @Before
    public void setUp() {
        email = "login_" + System.currentTimeMillis() + "@yandex.ru";
        password = "password123";
        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"%s\", \"name\": \"LoginUser\"}", email, password);
        given().spec(getRequestSpec()).body(requestBody).when().post("/api/auth/register");
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Проверяет успешную авторизацию с корректными email и паролем")
    public void loginExistingUserTest() {
        String requestBody = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);

        given()
                .spec(getRequestSpec())
                .body(requestBody)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("accessToken", notNullValue())
                .body("user.email", equalTo(email));
    }

    @Test
    @DisplayName("Вход с неверным логином")
    @Description("Проверяет, что при несуществующем email и правильном пароле возвращается ошибка 401")
    public void loginWithInvalidLoginTest() {
        String wrongEmail = "wrong_" + System.currentTimeMillis() + "@yandex.ru";
        String requestBody = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", wrongEmail, password);

        given()
                .spec(getRequestSpec())
                .body(requestBody)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным логином и паролем")
    @Description("Проверяет, что при неверных учетных данных возвращается ошибка 401")
    public void loginWithInvalidCredentialsTest() {
        String requestBody = "{\"email\": \"wrong@yandex.ru\", \"password\": \"wrongpass\"}";

        given()
                .spec(getRequestSpec())
                .body(requestBody)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}