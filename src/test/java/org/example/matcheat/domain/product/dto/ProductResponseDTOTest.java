package org.example.matcheat.domain.product.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.matcheat.domain.product.entity.ProductEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductResponseDTOTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void doesNotExposeOwnerAccountId() {
        ProductEntity product = mock(ProductEntity.class);
        when(product.getId()).thenReturn(7L);
        when(product.getOwnerAccountId()).thenReturn(42L);

        JsonNode response = objectMapper.valueToTree(ProductResponseDTO.from(product, 42L));

        assertThat(response.path("id").asLong()).isEqualTo(7L);
        assertThat(response.has("ownerAccountId")).isFalse();
    }

    @Test
    void exposesOwnerFlagInsteadOfRawAccountId() {
        ProductEntity product = mock(ProductEntity.class);
        when(product.getId()).thenReturn(7L);
        when(product.getOwnerAccountId()).thenReturn(42L);

        JsonNode ownerView = objectMapper.valueToTree(ProductResponseDTO.from(product, 42L));
        JsonNode strangerView = objectMapper.valueToTree(ProductResponseDTO.from(product, 99L));
        JsonNode guestView = objectMapper.valueToTree(ProductResponseDTO.from(product, null));

        assertThat(ownerView.path("owner").asBoolean()).isTrue();
        assertThat(strangerView.path("owner").asBoolean()).isFalse();
        assertThat(guestView.path("owner").asBoolean()).isFalse();
    }
}
