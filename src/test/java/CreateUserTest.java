import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateUserTest extends BaseTest {

    private String email;
    private String accessToken;

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Проверяет успешную регистрацию нового пользователя с уникальными данными")
    public void createUniqueUserTest() {
        email = "test_" + System.currentTimeMillis() + "@yandex.ru";
        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"password123\", \"name\": \"TestUser\"}", email);

        Response response = given()
                .spec(getRequestSpec())
                .body(requestBody)
                .when()
                .post("/api/auth/register");

        response.then().statusCode(200)
                .body("success", is(true))
                .body("user.email", equalTo(email))
                .body("accessToken", notNullValue());

        accessToken = response.path("accessToken");
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя")
    @Description("Проверяет, что при попытке регистрации с существующим email возвращается ошибка 403")
    public void createExistingUserTest() {
        String existingEmail = "existing_" + System.currentTimeMillis() + "@yandex.ru";
        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"password123\", \"name\": \"Existing\"}", existingEmail);


        given().spec(getRequestSpec()).body(requestBody).when().post("/api/auth/register");


        given()
                .spec(getRequestSpec())
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", is(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без логина")
    @Description("Проверяет, что при отсутствии email (логина) возвращается ошибка 403")
    public void createUserWithoutLoginTest() {
        String requestBody = "{\"password\": \"password123\", \"name\": \"NoLogin\"}";

        given()
                .spec(getRequestSpec())
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", is(false))
                .body("message", containsString("required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    @Description("Проверяет, что при отсутствии password возвращается ошибка 403")
    public void createUserWithoutPasswordTest() {
        String requestBody = String.format(
                "{\"email\": \"nopass_%s@yandex.ru\", \"name\": \"NoPassword\"}",
                System.currentTimeMillis());

        given()
                .spec(getRequestSpec())
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", is(false))
                .body("message", containsString("required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    @Description("Проверяет, что при отсутствии name (Username) возвращается ошибка 403")
    public void createUserWithoutNameTest() {
        String requestBody = String.format(
                "{\"email\": \"noname_%s@yandex.ru\", \"password\": \"password123\"}",
                System.currentTimeMillis());

        given()
                .spec(getRequestSpec())
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", is(false))
                .body("message", containsString("required fields"));
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