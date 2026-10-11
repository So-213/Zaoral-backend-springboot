package com.zaoral.backend.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 予期しない例外(500)のときに返すメッセージを、コントローラのメソッドごとに指定する。
 * 未指定の場合は既定のメッセージになる。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FailureMessage {

    String value();

}
