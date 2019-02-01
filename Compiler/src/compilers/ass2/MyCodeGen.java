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
public class MyCodeGen implements Codegen{
    
    int labelNum = 0;
    String continueLoop = "";
    String breakLoop = "";
   
    public String codegen(Program p) throws CodegenException {
        // here we want to generate a declarations and gets its body and generate it in genExp
        // we need to do this for all decl of p
        // need a for loop 
        String progCode = "main_entry:\n"
                + "move $fp $sp\n"
                + "sw $fp 0($sp)\n" // maybe remove
                + "addiu $sp $sp -4\n"
                + "jal " + ((Declaration)p.decls.get(0)).id + "_entry \n"
                + "li $v0 10\n"
                + "syscall\n"; // system call to return control!!!!
        
        for(Declaration i : p.decls){
            progCode += genDecl(i);
        }
        return progCode;
        
    }
    
    
    public String genDecl(Declaration d) throws CodegenException{
        String codeD = "";
        int sizeAR = (2 + d.numOfArgs) * 4;
        codeD = d.id + "_entry:\n"
                + "move $fp $sp\n"
                + "sw $ra 0($sp)\n"
                + "addiu $sp $sp-4\n"
                + genExp(d.body) // unsure what to code gen 
                + "lw $ra 4($sp)\n" // can use fp or ra
                + "addiu $sp $sp " + sizeAR + "\n"
                + "lw $fp 0($sp)\n"
                + "jr $ra\n";
        return codeD;
    }
    
    public String genExp(Exp e){
        String code = "";
        if (e instanceof IntLiteral){
            int n = ((IntLiteral) e).n;
            code += "li $a0 " + n + "\n";
        } 
        else if (e instanceof Variable){ // works so long as $fp has something in
            Variable a = (Variable)e;
            int offset = a.x * 4;
            code += "lw $a0 " + offset + "($fp)\n"; 
        }
        else if (e instanceof If){
            String elseBranch = newLabel(); // might not be correct
            String thenBranch = newLabel ();
            String exitLabel = newLabel ();
            code += genExp(((If) e).l) 
                    + "sw $a0 0($sp)\n"
                    + "addiu $sp $sp -4\n"
                    + genExp(((If) e).r)
                    + "lw $t1 4($sp)\n"
                    + "addiu $sp $sp 4\n"
                    + "sub $a0 $a0 $t1\n"
                    + comp(((If) e).comp, elseBranch)
                    + "b " + thenBranch + "\n"
                    + elseBranch + ":\n"
                    + genExp (((If) e).elseBody)
                    + "b " + exitLabel + "\n"
                    + thenBranch + ":\n"
                    + genExp (((If) e).thenBody)
                    + exitLabel + ":\n";  
        }
        else if (e instanceof Binexp){
            code += genExp(((Binexp) e).l)
                    + "sw $a0 0($sp)\n"
                    + "addiu $sp $sp -4\n"
                    + genExp(((Binexp) e).r)
                    + "lw $t1 4($sp)\n";
            if (((Binexp) e).binop instanceof Plus){ 
                code += "add $a0 $t1 $a0\n" 
                        + "addiu $sp $sp 4\n";
            } else if(((Binexp) e).binop instanceof Minus){
                code += "sub $a0 $t1 $a0\n" 
                        + "addiu $sp $sp 4\n";
            } else if(((Binexp) e).binop instanceof Times){
                code += "mult $t1 $a0\n"
                        + "mflo $a0\n";
            } else if (((Binexp) e).binop instanceof Div){
                code += "div $t1 $a0\n"
                        + "mflo $a0\n";
            }
        }
        else if (e instanceof Invoke){
            //store last arg first 
            code += "sw $fp 0($sp)\n"
                    + "addiu $sp $sp -4\n";
 
            for (int i = ((Invoke)e).args.size(); i==0 ; i--){ // likely to have errors                   
                code += genExp ( ((Invoke)e).args.get(i) )+"\n"
                     + "sw $a0 0($sp)\n"
                     + "addiu $sp $sp -4\n";
            }
            
            code += "jal " + ((Invoke)e).name + "_entry \n";  
            
            // may need to call something else
        }
        else if (e instanceof While){
            String body = newLabel();
            String exit = newLabel();
            String check = newLabel();
            breakLoop = exit;
            continueLoop = check;
            code += check + ":\n"
                    + genExp(((While) e).l)
                    + "lw $t1 4($sp)\n"
                    + "addiu $sp $sp 4\n"
                    + genExp(((While) e).r)
                    + "sub $a0 $a0 $t1\n"
                    + comp(((While) e).comp, exit); //left is in $t1 and right in $ao we want (r-l)            
            code += body + "\n"
                    + genExp(((While) e).body)
                    + exit + ":\n"; // need to make exit a string for break and continue to work
        }
        else if (e instanceof RepeatUntil){
            String repeat = newLabel();
            String exit = newLabel();
            String check = newLabel();
            breakLoop = exit;
            continueLoop = check;
            code += repeat + ":\n"
                    + genExp(((RepeatUntil) e).body)
                    + check + ":\n"
                    + genExp(((RepeatUntil) e).l)
                    + "lw $t1 4($sp)\n"
                    + "addiu $sp $sp 4\n"
                    + genExp(((RepeatUntil) e).r)
                    + "sub $a0 $a0 $t1\n"
                    + comp(((RepeatUntil) e).comp, exit)
                    + "b repeat\n"
                    + exit + ":\n";
            
        }
        else if (e instanceof Assign){
            int offset = 4 * ((Assign) e).x;
            code += genExp (e)
                    + "sw $a0" + offset + "($fp)\n";
        }
        else if (e instanceof Seq){
            code += genExp(((Seq) e).l)
                    + "sw $a0 0($sp)\n"
                    + "addiu $sp $sp -4\n"
                    + genExp(((Seq) e).r);
        }
        else if(e instanceof Skip){
            code += "nop\n";
        }
        else if(e instanceof Break){
            code += "b "+ breakLoop + "\n";
        } 
        else if( e instanceof Continue){
            code += "b "+ continueLoop + "\n";
        }
        return code;
    }
            

            
    public String newLabel(){
        labelNum++;
        return "label" + labelNum;
    }
    
    public String comp(Comp c, String jumpTo){ // checks it is wrong
        String code = "";
        if (c instanceof Less){
                code += "blez $a0 "+ jumpTo + "\n";
            } 
            else if(c instanceof LessEq){
                code += "bltz $a0 "+ jumpTo + "\n";
            }
            else if(c instanceof Equals){
                code += "bne $a0 $zero "+ jumpTo + "\n"; // assuming $r0 always holds 0 and we are allowed to use it
            } 
            else if(c instanceof GreaterEq){
                code += "bgtz $a0 "+ jumpTo + "\n";
            } 
            else if (c instanceof Greater){
                code += "bgez $a0 "+ jumpTo + "\n";
            }
        return code;
    }
}
