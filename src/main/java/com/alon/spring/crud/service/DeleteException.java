/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alon.spring.crud.service;

/**
 *
 * @author paulo
 */
public class DeleteException extends Exception {

    public DeleteException() {
    }
    
    public DeleteException(String msg) {
        super(msg);
    }

    public DeleteException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeleteException(Throwable cause) {
        super(cause);
    }
}
