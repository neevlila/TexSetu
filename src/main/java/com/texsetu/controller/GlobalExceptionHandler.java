package com.texsetu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        log.warn("File upload size exceeded limit: {}", exc.getMessage());
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: File exceeds the maximum allowed limit of 10MB.");
            return "redirect:" + referer;
        }
        model.addAttribute("errorTitle", "File Too Large");
        model.addAttribute("errorMessage", "The file you attempted to upload exceeds the maximum allowed size of 10MB. Please select a smaller file.");
        return "error/custom-error";
    }

    @ExceptionHandler({SecurityException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(Exception exc, Model model) {
        log.warn("Access denied or security exception: {}", exc.getMessage());
        model.addAttribute("errorTitle", "Access Denied");
        model.addAttribute("errorMessage", exc.getMessage() != null ? exc.getMessage() : "You do not have permission to view or modify this resource.");
        return "error/403";
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleClientError(Exception exc, Model model) {
        log.warn("Client error: {}", exc.getMessage());
        model.addAttribute("errorTitle", "Invalid Request");
        model.addAttribute("errorMessage", exc.getMessage());
        return "error/custom-error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception exc, Model model) {
        log.error("Unhandled application exception", exc);
        model.addAttribute("errorTitle", "System Notice");
        model.addAttribute("errorMessage", "An unexpected situation occurred while processing your request. Please try again or contact support if the issue persists.");
        return "error/custom-error";
    }
}
