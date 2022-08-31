package com.example.uboatvault.api.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@RestController
public class AppErrorController implements ErrorController {
    @RequestMapping(value = "/error")
    public String error(HttpServletRequest request) {
        return String.format("""
                        <!DOCTYPE html>
                        <html>
                        <body>
                        <h1>Error</h1>
                        <p>Status code is:    <b>%s</b> </p>
                        <p>Status message is: <b>%s</b> </p>
                        </body>
                        </html>
                        """,
                Objects.requireNonNullElse(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE), "Unknown status code"),
                Objects.requireNonNullElse(request.getAttribute(RequestDispatcher.ERROR_MESSAGE), "Unknown status code")
        );
    }
}
