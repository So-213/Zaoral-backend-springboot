package com.zaoral.backend.support;

import com.zaoral.backend.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * cronから呼ばれるエンドポイントの認証。共有シークレットを Authorization: Bearer で受け取って照合する。
 *
 * 業務APIの {@code X-User-Id} はNext.jsが認証を済ませた証として信用しているが(04_認証認可切り分け.md)、
 * cronはNext.jsを経由しないためその前提が無く、ここだけは自前で認証する必要がある。
 * 方式は置き換え元(Vercel Cron)と同じ共有シークレットを引き継いでいる。
 */
@Component
public class CronAuthorization {

    private static final Logger log = LoggerFactory.getLogger(CronAuthorization.class);

    private static final String BEARER_PREFIX = "Bearer ";

    private final String secret;

    public CronAuthorization(@Value("${zaoral.cron.secret:}") String secret) {
        this.secret = secret;

        // 未設定に気づかないまま運用が始まるとcronが毎回401で失敗し続けるため、起動時に残す
        if (secret.isBlank()) {
            log.warn("CRON_SECRET が未設定です。cron用エンドポイントは常に401を返します。");
        }
    }

    /**
     * Authorizationヘッダを検証する。一致しない場合は401になる {@link ApiException} を投げる。
     *
     * シークレットが未設定なら常に失敗させる(fail-closed)。「未設定なら認証しない」にすると、
     * 環境変数の設定漏れがそのまま誰でも叩けるエンドポイントになってしまうため。
     *
     * @param authorizationHeader Authorizationヘッダの値。ヘッダが無い場合は null
     */
    public void verify(String authorizationHeader) {
        if (secret.isBlank() || authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw unauthorized();
        }

        byte[] expected = secret.getBytes(StandardCharsets.UTF_8);
        byte[] actual = authorizationHeader.substring(BEARER_PREFIX.length()).getBytes(StandardCharsets.UTF_8);

        // equals は不一致の位置で比較を打ち切るため、応答時間からシークレットを1文字ずつ特定できてしまう。
        // MessageDigest.isEqual は長さが同じであれば最後まで比較する
        if (!MessageDigest.isEqual(expected, actual)) {
            throw unauthorized();
        }
    }

    /** 失敗の理由(ヘッダ欠落・不一致・未設定)は区別せず、外からは同じ応答にする */
    private ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "認証が必要です");
    }

}
