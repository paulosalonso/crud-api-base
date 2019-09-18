/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.senior.treinamento.service;

/**
 *
 * @author paulo
 */
public class UpdateException extends Exception {

    public UpdateException() {
    }
    
    public UpdateException(String msg) {
        super(msg);
    }

    public UpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpdateException(Throwable cause) {
        super(cause);
    }
}
