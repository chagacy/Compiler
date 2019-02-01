/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilers.ass2;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chayagacy
 */
public class makeProg {
    public static void main(String[] args) throws CodegenException{
        List <Declaration> decl = new ArrayList();
        
        Declaration d = new Declaration ( "f", 
              3, 
              new If ( new Variable ( 1 ), 
                       new Equals (),
                       new Variable( 2 ),
                       new Variable( 3 ),
                       new IntLiteral ( 0 ) ) );
        
        decl.add(d);
        Program p = new Program(decl);
        MyCodeGen c1 = new MyCodeGen ();    
        System.out.println(c1.codegen(p)); 
    } 
}
