package com.zaoral.backend.support;

import com.zaoral.backend.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CronAuthorizationTest {

    private static final String SECRET = "s3cret-value";

    @Nested
    @DisplayName("シークレットが設定されている場合")
    class WhenSecretIsConfigured {

        private final CronAuthorization authorization = new CronAuthorization(SECRET);

        @Test
        void 正しいBearerトークンなら通る() {
            assertThatCode(() -> authorization.verify("Bearer " + SECRET)).doesNotThrowAnyException();
        }

        @Test
        void ヘッダが無ければ401() {
            assertUnauthorized(authorization, null);
        }

        @Test
        void Bearerスキームでなければ401() {
            assertUnauthorized(authorization, SECRET);
            assertUnauthorized(authorization, "Basic " + SECRET);
        }

        @Test
        void トークンが一致しなければ401() {
            assertUnauthorized(authorization, "Bearer wrong-value");
            // 前方一致や長さ違いを通さない
            assertUnauthorized(authorization, "Bearer s3cret");
            assertUnauthorized(authorization, "Bearer " + SECRET + "x");
        }
    }

    @Nested
    @DisplayName("シークレットが未設定の場合")
    class WhenSecretIsMissing {

        // 設定漏れがそのまま「誰でも叩けるエンドポイント」にならないこと(fail-closed)
        @Test
        void 空文字なら何を渡しても401() {
            CronAuthorization authorization = new CronAuthorization("");
            assertUnauthorized(authorization, null);
            assertUnauthorized(authorization, "Bearer ");
            assertUnauthorized(authorization, "Bearer anything");
        }

        @Test
        void 空白のみでも401() {
            CronAuthorization authorization = new CronAuthorization("   ");
            assertUnauthorized(authorization, "Bearer    ");
        }
    }

    private static void assertUnauthorized(CronAuthorization authorization, String header) {
        assertThatThrownBy(() -> authorization.verify(header))
                .isInstanceOf(ApiException.class)
                .hasMessage("認証が必要です")
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
