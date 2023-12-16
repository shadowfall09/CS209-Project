package org.java2.backend.common;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接口统一返回包装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result {

    public static final String CODE_SUCCESS = "200";
    public static final String CODE_SYS_ERROR = "500";

    private String code;

    private String msg;

    private Object data;

    /**
     * 请求成功，无数据返回，返回值200
     */
    public static Result success(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        return new Result(CODE_SUCCESS, "Request Success", null);
    }

    /**
     * 请求成功，有数据返回，返回值200
     */
    public static Result success(HttpServletResponse response, Object data) {
        response.setStatus(HttpServletResponse.SC_OK);
        return new Result(CODE_SUCCESS, "Request Success", data);
    }

    /**
     * 请求失败，无数据返回，返回值500
     */
    public static Result error(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new Result(CODE_SYS_ERROR, "System Error", null);
    }

    /**
     * 请求失败，无数据返回，自定义消息，返回值500
     */
    public static Result error(HttpServletResponse response, String msg) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new Result(CODE_SYS_ERROR, msg, null);
    }

    /**
     * 请求失败，有数据返回，自定义消息、返回值
     */
    public static Result error(HttpServletResponse response, String code, String msg) {
        response.setStatus(Integer.parseInt(code));
        return new Result(code, msg, null);
    }

    public static Result error(HttpServletResponse response, String code, String msg, Object data) {
        response.setStatus(Integer.parseInt(code));
        return new Result(code, msg, data);
    }

}
