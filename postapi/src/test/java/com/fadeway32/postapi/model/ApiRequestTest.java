package com.fadeway32.postapi.model;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiRequestTest {
    @Test
    void buildsGetRequestWithQueryAndHeaders() {
        ApiRequest request = ApiRequest.get("https://example.com/users")
                .queryParam("page", 1)
                .queryParam("tag", "java")
                .header("X-Trace-Id", "trace-001")
                .build();

        assertThat(request.method()).isEqualTo(HttpMethod.GET);
        assertThat(request.bodyType()).isEqualTo(BodyType.NONE);
        assertThat(request.queryParams()).containsKeys("page", "tag");
        assertThat(request.headers()).containsKey("X-Trace-Id");
    }

    @Test
    void buildsFormRequest() {
        ApiRequest request = ApiRequest.post("https://example.com/login")
                .form("username", "admin")
                .form("password", "123456")
                .build();

        assertThat(request.bodyType()).isEqualTo(BodyType.FORM_URLENCODED);
        assertThat(request.formFields()).containsEntry("username", "admin");
    }

    @Test
    void buildsMultipartRequestWithFiles() {
        ApiRequest request = ApiRequest.post("https://example.com/upload")
                .part("bizType", "avatar")
                .file("file", Paths.get("avatar.png"), "image/png")
                .build();

        assertThat(request.bodyType()).isEqualTo(BodyType.MULTIPART);
        assertThat(request.multipartFields()).containsEntry("bizType", "avatar");
        assertThat(request.files()).hasSize(1);
        assertThat(request.files().get(0).fileName()).isEqualTo("avatar.png");
    }

    @Test
    void buildsBinaryProtocolRequests() {
        ApiRequest protobuf = ApiRequest.post("https://example.com/pb")
                .protobuf(new byte[]{1, 2, 3})
                .build();
        ApiRequest kryo = ApiRequest.post("https://example.com/kryo")
                .kryo(new byte[]{4, 5, 6})
                .build();

        assertThat(protobuf.bodyType()).isEqualTo(BodyType.PROTOBUF);
        assertThat(protobuf.contentType()).isEqualTo("application/x-protobuf");
        assertThat(kryo.bodyType()).isEqualTo(BodyType.KRYO);
        assertThat(kryo.contentType()).isEqualTo("application/x-kryo");
    }
}
