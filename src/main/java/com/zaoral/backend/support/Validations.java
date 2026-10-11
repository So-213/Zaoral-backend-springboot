package com.zaoral.backend.support;

import com.zaoral.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * 現行Next.js実装の api-helpers.ts#validateRequired と同じ判定・文言を再現する。
 */
public final class Validations {

    private Validations() {
    }

    /**
     * null・空文字・空白のみを「未入力」として 400 を投げる。
     */
    public static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + "が必要です");
        }
    }

}
