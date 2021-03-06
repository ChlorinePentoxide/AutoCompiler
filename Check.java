// package omitted currently
// package test;
import java.util.*;
import java.util.stream.Collectors;

import javax.tools.*;
import javax.tools.JavaCompiler.*;
import java.io.*;

//class name will be renamed afterwards
public class Check {
    public static void main(String[] args) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MyDiagnosticListener listener = new MyDiagnosticListener(); 
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(listener, null, null); 
       
       
        //file name using command line argument
        String fileToCompile = args[0];
       
        Iterable fileObjects = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(fileToCompile)); 
        CompilationTask task = compiler.getTask(null, fileManager, listener, null, null, fileObjects); 

        Boolean result = task.call(); // compile the file 
        //print the list each tuple has 3 values error type, position and what symbol is expected

       // System.out.println(listener.getList());
        List<Tuple<String>> list = listener.getList();
        // sort using positions of errors
        List<Tuple<String>> list_missing_symbols = list.stream()
        .filter(e->e.getThird().endsWith("expected")) //filter out only expected errors
        .sorted(Comparator.comparing(Tuple::getSecond))
        .collect(Collectors.toList());
    
        System.out.println(list_missing_symbols);
        //if (result != true)
        //    System.out.println("Missing Elements : ");
        RandomWriter writer = new RandomWriter();
        // int index = 0;
        for (Tuple<String> tuple : list_missing_symbols) {
            String error = tuple.getThird();
            System.out.print(error +" " +error.charAt(1)+"\t");
            String pos = tuple.getSecond();
            writer.writeToFileWithNewLine(fileToCompile, String.valueOf(error.charAt(1)), Long.parseLong(pos));
        }
    
        System.out.println("missing symbols");
        List<Tuple<String>> otherErrors = list.stream()
        .filter(e -> !e.getThird().endsWith("expected")) 
        .sorted(Comparator.comparing(Tuple::getSecond))
        .collect(Collectors.toList());
    
        System.out.println(otherErrors);
        RandomReader reader = new RandomReader();
        for (Tuple<String> tuple : otherErrors) {
            
            String error  = tuple.getThird();
            error  = error.trim();
            String type = tuple.getFirst();
            // System.out.println(error);
            
            if(error.equals("incompatible types: int[] cannot be converted to int")|| 
               error.equals("incompatible types: double[] cannot be converted to double") ||
               error.equals("incompatible types: float[] cannot be converted to float") 
                    )
            {
                //manually handle the case when square bracket is missing
                // long pos = Long.parseLong(tuple.getSecond());
                // writer.writeToFile(fileToCompile, "[", pos-2);
                // writer.writeToFile(fileToCompile, "]", pos - 1);
                long lineNumber = Long.parseLong(tuple.getForth());

                String line = reader.getLine(fileToCompile, lineNumber);
                // System.out.println(line);
                StringBuffer sb = new StringBuffer(line.trim());
                int index = sb.indexOf("=");
                sb.insert(index - 1, "[]");
                // System.out.println(sb);
                // writer.writeTofileWithLineNumber(fileToCompile, "// this line has been commented ", lineNumber - 1);
                writer.commentLine(fileToCompile, lineNumber-1);
                writer.writeTofileWithLineNumber(fileToCompile, sb.toString(), lineNumber);
            }
            if(error.equals("reached end of file while parsing"))
            {
                
                // System.out.println("matched ");
                // lineNumber = Long.parseLong(tuple.getForth());
                long lineNumber = Long.parseLong(tuple.getForth());
                writer.writeTofileWithLineNumber(fileToCompile, "}",lineNumber );
       
            }
             if(type.equals("compiler.err.class.public.should.be.in.file"))
            {
                long lineNumber = Long.parseLong(tuple.getForth());
                // String line = reader.getLine(fileToCompile, lineNumber);
                // writer.commentLine(fileToCompile, lineNumber);

                writer.commentLine(fileToCompile, lineNumber - 1);
                // String[] arr = fileToCompile.split(".");
                // System.out.println(Arrays.toString(arr));
                String classname = fileToCompile.split("[.]")[0];
                // System.out.println(fileToCompile.split("."));
                writer.writeTofileWithLineNumber(fileToCompile, "public class "+classname+"{", lineNumber);
            }

            else
            {
                System.out.println("Currently not support for this error ");
            }
        }

        System.out.println("done 2");
        if (result) {
            System.out.println("\nCompilation has succeeded");
        }
    }
}
