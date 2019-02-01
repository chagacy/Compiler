/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilers.ass2;

/**
 *
 * @author chayagacy
 */
class CodegenException extends Exception {
    public String msg;
    public CodegenException ( String _msg ) { msg = _msg; } 
}

