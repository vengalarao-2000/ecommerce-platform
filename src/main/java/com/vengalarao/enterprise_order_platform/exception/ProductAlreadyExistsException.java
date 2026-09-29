package com.vengalarao.enterprise_order_platform.exception;

public class ProductAlreadyExistsException extends RuntimeException {

    //Create a constructor that accepts a message and calls super(message) i.e parent class contructor
    public ProductAlreadyExistsException(String message) {
        super(message);
    }

}
