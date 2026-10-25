package clients;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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



    public static Response registerUser(String email, String password, String name) {
        JsonObject body = new JsonObject();
        if (email != null) body.addProperty("email", email);
        if (password != null) body.addProperty("password", password);
        if (name != null) body.addProperty("name", name);

        return RestAssured.given()
                .spec(spec())
                .body(body)
                .when()
                .post("/api/auth/register");
    }

    public static Response loginUser(String email, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        return RestAssured.given()
                .spec(spec())
                .body(body)
                .when()
                .post("/api/auth/login");
    }

    public static Response deleteUser(String accessToken) {
        return RestAssured.given()
                .spec(spec())
                .header("Authorization", accessToken)
                .when()
                .delete("/api/auth/user");
    }



    public static Response getIngredients() {
        return RestAssured.given()
                .spec(spec())
                .when()
                .get("/api/ingredients");
    }



    public static Response createOrder(List<String> ingredients, String accessToken) {
        JsonArray array = new JsonArray();
        for (String id : ingredients) {
            array.add(id);
        }
        JsonObject body = new JsonObject();
        body.add("ingredients", array);

        RequestSpecification request = RestAssured.given()
                .spec(spec())
                .body(body);
        if (accessToken != null) {
            request.header("Authorization", accessToken);
        }
        return request.when().post("/api/orders");
    }
}