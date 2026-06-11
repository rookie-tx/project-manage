package cn.tx.aiagent.common.api;

import cn.tx.aiagent.common.config.MessageSourceHolder;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Result<T> {

    private int code;

    private String msg;

    private T data;

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    private static final String SUCCESS_MSG_KEY = ErrorCode.SUCCESS.getMessageKey();

    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), resolveMsg(SUCCESS_MSG_KEY), data);
    }

    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), resolveMsg(SUCCESS_MSG_KEY), null);
    }

    public static <T> Result<T> error(ErrorCode errorCode, Object... args) {
        return new Result<>(errorCode.getCode(), resolveMsg(errorCode.getMessageKey(), args), null);
    }

    private static String resolveMsg(String key, Object... args) {
        return MessageSourceHolder.getMessage(key, args);
    }
}
