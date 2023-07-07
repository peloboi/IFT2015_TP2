// Auteurs:
// TIRAO, Raihiti - 20235290
// PELOQUIN, Vincent - 20105029

import java.io.*;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TP2 {
    public static void main(String[] args){

        // initial time for empirical analysis.
        long initialTime = System.currentTimeMillis();

        // setup of the main variables
        File readFile = new File(args[0]);
        File writeFile = new File(args[1]);

        try {
            Scanner read = new Scanner(readFile);
            String rawInput = "";

            while (read.hasNextLine()){
                rawInput += read.nextLine();
            }

            String[] commands = rawInput.split(";", 0);

            for (int i=0; i < commands.length ; i++){
                System.out.println(commands[i]);
            }


        }
        catch (FileNotFoundException e){
            System.out.println("The file " + args[0] + " doesn't exist");
        }

    }
}