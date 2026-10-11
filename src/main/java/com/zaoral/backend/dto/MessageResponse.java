package com.zaoral.backend.dto;

/**
 * メッセージのみを返すレスポンス。現行の削除APIの {"message": "..."} に対応する。
 */
public record MessageResponse(String message) {
}
