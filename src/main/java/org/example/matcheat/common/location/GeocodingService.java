package org.example.matcheat.common.location;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

/**
 * 주소 문자열을 Kakao Local API를 이용해 위도와 경도로 변환한다.
 */
@Service
public class GeocodingService {

    private final RestClient restClient;
    private final String restApiKey;

    public GeocodingService(
            RestClient.Builder restClientBuilder,
            @Value("${app.kakao.rest-api-key:}")
            String restApiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl("https://dapi.kakao.com")
                .build();

        this.restApiKey = restApiKey;
    }

    /**
     * 입력한 주소를 WGS84 위도와 경도로 변환한다.
     */
    public Coordinates geocode(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException(
                    "좌표로 변환할 주소를 입력해주세요."
            );
        }

        if (restApiKey == null || restApiKey.isBlank()) {
            throw new IllegalStateException(
                    "KAKAO_REST_API_KEY가 설정되지 않았습니다."
            );
        }

        try {
            JsonNode response =
                    restClient.get()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .path(
                                                    "/v2/local/search/address.json"
                                            )
                                            .queryParam(
                                                    "query",
                                                    address.trim()
                                            )
                                            .build()
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    "KakaoAK " + restApiKey
                            )
                            .retrieve()
                            .body(JsonNode.class);

            if (response == null
                    || !response.has("documents")
                    || response.path("documents").isEmpty()) {

                throw new IllegalArgumentException(
                        "주소를 찾을 수 없습니다. 도로명 주소를 확인해주세요."
                );
            }

            JsonNode document =
                    response.path("documents").get(0);

            // Kakao Local API의 x는 경도, y는 위도이다.
            double longitude =
                    document.path("x").asDouble();

            double latitude =
                    document.path("y").asDouble();

            return new Coordinates(
                    latitude,
                    longitude
            );

        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(
                    "주소 좌표 변환 중 Kakao API 호출에 실패했습니다.",
                    exception
            );
        }
    }

    /**
     * 주소 변환 결과의 위도와 경도를 전달한다.
     */
    public record Coordinates(
            double latitude,
            double longitude
    ) {
    }
}