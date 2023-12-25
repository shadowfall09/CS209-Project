package org.java2.backend.controller;


import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.java2.backend.common.Result;
import org.java2.backend.exception.ServiceException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/log")
@Slf4j
public class LoggingController {

    @GetMapping("/warn")
    public Result warn() {
        log.warn("This is a warning message");
        throw new ServiceException("400", "This is a warning message");
    }

    @GetMapping("/error")
    public Result error() {
        log.error("This is an error message");
        throw new ServiceException("500", "This is an error message");
    }

    @GetMapping("/info")
    public Result info(HttpServletResponse response) {
        log.info("This is an info message");
        return Result.success(response,"This is an info message");
    }

}
