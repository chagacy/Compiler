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
class Task1 {
    public static Codegen create () throws CodegenException { 
        Codegen p = new MyCodeGen();
        return p;
    }
}
  
