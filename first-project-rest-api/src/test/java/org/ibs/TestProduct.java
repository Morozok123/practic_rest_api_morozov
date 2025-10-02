package org.ibs;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TestProduct {

    // Конфигурационные параметры
    private static final String BASE_URL = "http://localhost:8080";
    private static final String JSESSIONID = "679975347DF874F2F9F3D324C03C4A3D";

    // Тестовые данные
    private static class TestData {
        static final String FRUIT_NAME = "Банан";
        static final String FRUIT_TYPE = "FRUIT";
        static final boolean FRUIT_EXOTIC = true;

        static final String VEGETABLE_NAME = "Артишок";
        static final String VEGETABLE_TYPE = "VEGETABLE";
        static final boolean VEGETABLE_EXOTIC = true;

        static final String[] EXPECTED_PRODUCT_NAMES = {FRUIT_NAME, VEGETABLE_NAME};
    }

    // Endpoints
    private static class Endpoints {
        static final String ROOT = "/";
        static final String FOOD = "/api/food";
        static final String RESET = "/api/data/reset";
    }

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    // Метод для запросов с куками
    private io.restassured.specification.RequestSpecification givenWithAuth() {
        return given()
                .cookie("JSESSIONID", JSESSIONID)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    // Универсальный метод для создания товара
    private void createProduct(String name, String type, boolean exotic) {
        String requestBody = String.format("""
        {
            "name": "%s",
            "type": "%s",
            "exotic": %s
        }
        """, name, type, exotic);

        givenWithAuth()
                .body(requestBody)
                .when()
                .post(Endpoints.FOOD)
                .then()
                .statusCode(200);
    }

    // Метод для проверки наличия товаров в списке
    private void verifyProductsExist(String... productNames) {
        givenWithAuth()
                .when()
                .get(Endpoints.FOOD)
                .then()
                .statusCode(200)
                .body("name", hasItems(productNames));
    }

    // Тест-кейс 1: Проверка доступности сервера
    @Test
    void Test1() {
        givenWithAuth()
                .when()
                .get(Endpoints.ROOT)
                .then()
                .statusCode(200);
    }

    // Тест-кейс 2: Получение списка товаров
    @Test
    void Test2() {
        givenWithAuth()
                .when()
                .get(Endpoints.FOOD)
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(0)))
                .log().body();
    }

    // Тест-кейс 3: Создание фрукта
    @Test
    void Test3() {
        createProduct(TestData.FRUIT_NAME, TestData.FRUIT_TYPE, TestData.FRUIT_EXOTIC);
    }

    // Тест-кейс 4: Создание экзотического овоща
    @Test
    void Test4() {
        createProduct(TestData.VEGETABLE_NAME, TestData.VEGETABLE_TYPE, TestData.VEGETABLE_EXOTIC);
    }

    // Тест-кейс 5: Проверка всех созданных товаров
    @Test
    void Test5() {
        verifyProductsExist(TestData.EXPECTED_PRODUCT_NAMES);

        // Дополнительно логируем весь список для отладки
        givenWithAuth()
                .when()
                .get(Endpoints.FOOD)
                .then()
                .log().body();
    }

    // Тест-кейс 6: Сброс тестовых данных
    @Test
    void Test6() {
        givenWithAuth()
                .when()
                .post(Endpoints.RESET)
                .then()
                .statusCode(200);

        // Проверяем что данные сбросились
        givenWithAuth()
                .when()
                .get(Endpoints.FOOD)
                .then()
                .statusCode(200)
                .log().body();
    }
}