package org.java2.backend.exception;

import org.java2.backend.common.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result serviceException(HttpServletResponse response, ServiceException e) {
        return Result.error(response, e.getCode(), e.getMessage(), e.getCauseObject());
    }

}
