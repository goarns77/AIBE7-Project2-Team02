package org.example.matcheat.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {
    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void registersJwtBearerSecurityScheme() {
        OpenAPI openApi = config.matchEatOpenApi();

        assertThat(openApi.getComponents().getSecuritySchemes())
                .containsKey(OpenApiConfig.BEARER_AUTH);
        assertThat(openApi.getComponents().getSecuritySchemes().get(OpenApiConfig.BEARER_AUTH).getScheme())
                .isEqualTo("bearer");
    }

    @Test
    void appliesBearerAuthOnlyToProtectedApiOperations() {
        Operation login = new Operation();
        Operation account = new Operation();
        Operation admin = new Operation();
        Operation publicProductList = new Operation();
        Operation createProduct = new Operation();
        Operation sellerProducts = new Operation();
        OpenAPI openApi = config.matchEatOpenApi().paths(new Paths()
                .addPathItem("/api/v1/auth/login", new PathItem().post(login))
                .addPathItem("/api/v1/account/me", new PathItem().get(account))
                .addPathItem("/api/v1/admin/dashboard", new PathItem().get(admin))
                .addPathItem("/api/v1/products", new PathItem()
                        .get(publicProductList)
                        .post(createProduct))
                .addPathItem("/api/v1/products/mine", new PathItem().get(sellerProducts)));

        config.bearerAuthOpenApiCustomizer().customise(openApi);

        assertThat(login.getSecurity()).isNull();
        assertThat(publicProductList.getSecurity()).isNull();
        assertThat(account.getSecurity()).singleElement()
                .satisfies(requirement -> assertThat(requirement).containsKey(OpenApiConfig.BEARER_AUTH));
        assertThat(admin.getSecurity()).singleElement()
                .satisfies(requirement -> assertThat(requirement).containsKey(OpenApiConfig.BEARER_AUTH));
        assertThat(createProduct.getSecurity()).singleElement()
                .satisfies(requirement -> assertThat(requirement).containsKey(OpenApiConfig.BEARER_AUTH));
        assertThat(sellerProducts.getSecurity()).singleElement()
                .satisfies(requirement -> assertThat(requirement).containsKey(OpenApiConfig.BEARER_AUTH));
    }
}
