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
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.TreeMap;

public class Tp2 {
    public static void main(String[] args) {

        // setup of the main variables
        File readFile = new File(args[0]);
        File writeFile = new File(args[1]);
        PharmacyRegistry pharmRegistry = new PharmacyRegistry();

        try {
            Scanner read = new Scanner(readFile);
            StringBuilder rawInput = new StringBuilder();

            while (read.hasNextLine()) {
                rawInput.append(read.nextLine()).append("\n");
            }

            String[] commands = rawInput.toString().split(";", 0);
            ArrayList<String> trimmedCommands = new ArrayList<>();

            for (String command : commands) {
                String trimmedCommand = command.trim();
                if (!trimmedCommand.isEmpty()) {
                    trimmedCommands.add(trimmedCommand);
                }
            }

            // Execute the trimmed commands.
            for (String trimmedCommand : trimmedCommands) {
                if (trimmedCommand.startsWith("DATE")){
                    pharmRegistry.date(trimmedCommand, writeFile);
                }
                if (trimmedCommand.startsWith("PRESCRIPTION")){
                    pharmRegistry.prescription(trimmedCommand, writeFile);
                }
                if (trimmedCommand.startsWith("APPROV")){
                    pharmRegistry.approv(trimmedCommand, writeFile);
                }
                if (trimmedCommand.startsWith("STOCK")){
                    pharmRegistry.stock(writeFile);
                }

            }


        } catch (FileNotFoundException e) {
            System.out.println("The file " + args[0] + " doesn't exist");
        }
    }
}