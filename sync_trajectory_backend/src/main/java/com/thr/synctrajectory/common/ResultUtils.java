package com.thr.synctrajectory.common;

/**
 * 返回工具类
 *
 * @author thr
 */
public class ResultUtils {
    /**
     * 返回成功的结果
     * @param data 数据
     * @param <T> 类型
     * @return 创建的成功返回
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }


    /**
     * 返回失败的结果
     * @param errorCode 错误码
     * @param <T> 类型
     * @return 创建的失败返回
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    public static <T> BaseResponse<T> error(int code, String message, String description) {
        return new BaseResponse<>(code, null, message, description);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse<>(errorCode.getCode(), null, message, description);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, String description) {
        return new BaseResponse<>(errorCode.getCode(), null,errorCode.getMessage(), description);
    }

}
