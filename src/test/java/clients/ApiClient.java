package clients;

import com.google.gson.annotations.SerializedName;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;

public class ApiClient {

    private static final String BASE_URL = "https://qa-stellarburgers.education-services.ru/";

    static {
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                        .defaultObjectMapperType(ObjectMapperType.GSON));
    }

    private static RequestSpecification spec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }



    public static class UserRegistration {
        @SerializedName("email")    private final String email;
        @SerializedName("password") private final String password;
        @SerializedName("name")     private final String name;

        public UserRegistration(String email, String password, String name) {
            this.email = email;
            this.password = password;
            this.name = name;
        }
    }

    public static class UserLogin {
        @SerializedName("email")    private final String email;
        @SerializedName("password") private final String password;

        public UserLogin(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class Order {
        @SerializedName("ingredients") private final List<String> ingredients;

        public Order(List<String> ingredients) {
            this.ingredients = ingredients;
        }
    }



    @Step("Регистрация пользователя: email = {email}, name = {name}")
    public static Response registerUser(String email, String password, String name) {
        UserRegistration body = new UserRegistration(email, password, name);
        return RestAssured.given()
                .spec(spec())
                .body(body)
                .when()
                .post("/api/auth/register");
    }

    @Step("Авторизация пользователя: email = {email}")
    public static Response loginUser(String email, String password) {
        UserLogin body = new UserLogin(email, password);
        return RestAssured.given()
                .spec(spec())
                .body(body)
                .when()
                .post("/api/auth/login");
    }

    @Step("Удаление пользователя")
    public static Response deleteUser(String accessToken) {
        return RestAssured.given()
                .spec(spec())
                .header("Authorization", accessToken)
                .when()
                .delete("/api/auth/user");
    }

    @Step("Получение списка ингредиентов")
    public static Response getIngredients() {
        return RestAssured.given()
                .spec(spec())
                .when()
                .get("/api/ingredients");
    }

    @Step("Создание заказа с ингредиентами: {ingredients}")
    public static Response createOrder(List<String> ingredients, String accessToken) {
        Order body = new Order(ingredients);
        RequestSpecification request = RestAssured.given()
                .spec(spec())
                .body(body);
        if (accessToken != null) {
            request.header("Authorization", accessToken);
        }
        return request.when().post("/api/orders");
    }
}