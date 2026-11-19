package com.texsetu.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        log.warn("Illegal argument error: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Invalid Request");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/custom-error";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, Model model) {
        log.warn("Illegal state error: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Action Not Allowed");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/custom-error";
    }

    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException ex, Model model) {
        log.warn("Security violation: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Access Denied");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/403";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(MaxUploadSizeExceededException ex, Model model) {
        log.warn("File upload limit exceeded: {}", ex.getMessage());
        model.addAttribute("errorTitle", "File Too Large");
        model.addAttribute("errorMessage", "The uploaded file exceeds the maximum allowed limit of 10 MB.");
        return "error/custom-error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unhandled exception: ", ex);
        model.addAttribute("errorTitle", "System Error");
        model.addAttribute("errorMessage", "An unexpected error occurred while processing your request. Please try again or contact support.");
        return "error/custom-error";
    }
}
