package com.mshzidan.guard.security.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ThymeleafExceptionHandler {

    @ExceptionHandler(OtpSendingException.class)
    public ModelAndView handleOtpSendingException(OtpSendingException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", ex.getMessage());
        mav.setViewName("error/otp-error"); // Thymeleaf template
        return mav;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ModelAndView handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", ex.getMessage());
        mav.setViewName("register"); // Redirect back to registration form
        return mav;
    }

//    // Add other Thymeleaf-specific exceptions here
//    @ExceptionHandler(Exception.class)
//    public ModelAndView handleGenericException(Exception ex) {
//        ModelAndView mav = new ModelAndView();
//        mav.addObject("error", "An unexpected error occurred");
//        mav.setViewName("error/generic-error");
//        return mav;
//    }
}
