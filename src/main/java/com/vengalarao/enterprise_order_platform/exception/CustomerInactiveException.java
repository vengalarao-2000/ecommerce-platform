package com.vengalarao.enterprise_order_platform.exception;

public class CustomerInactiveException extends RuntimeException{
    public CustomerInactiveException(String message) {
        super(message);
    }
}
