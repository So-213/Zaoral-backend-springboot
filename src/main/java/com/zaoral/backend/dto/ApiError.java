package com.zaoral.backend.dto;

/**
 * エラーレスポンス。現行Next.js実装と同じく {"error": "..."} の形で返す。
 */
public record ApiError(String error) {
}
