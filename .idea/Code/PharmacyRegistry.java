import java.util.TreeMap;
import java.util.LinkedList;
import java.time.LocalDate;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class PharmacyRegistry {
    
    private TreeMap<String, TreeMap<LocalDate, Integer>> medsRegistry = new TreeMap<>();

    private TreeMap<String, Integer> orderRegistry = new TreeMap<>();

    private LocalDate currentDate;

    private int prescriptionNumber = 1;

    public TreeMap<LocalDate, Integer> getMed(String med){
        return medsRegistry.get(med);
    }

    public void setMed(String med, LocalDate date, Integer quantity){

        if (medsRegistry.containsKey(med)){
            TreeMap<LocalDate, Integer> medInstances = medsRegistry.get(med);
            if (medInstances.containsKey(date)){
                Integer oldQuantity = medInstances.get(date);
                medInstances.put(date, oldQuantity + quantity);
            }
            else {
                medInstances.put(date, quantity);
            }
        }

        else {
            TreeMap<LocalDate, Integer> instance = new TreeMap<LocalDate, Integer>();
            instance.put(date, quantity);
            medsRegistry.put(med, instance);
        }
    }

    public void approv(String command, File outputFile){

        String[] splits = command.split("\n", 0);
        for (int i=1; i<splits.length; i++){

            String[] newSplits = splits[i].split("[\\s\\t]+", 0);

            String medName = newSplits[0];
            int quant = Integer.valueOf(newSplits[1]);
            String date = newSplits[2];

            String[] dateSplits = date.split("-", 0);
            int year = Integer.valueOf(dateSplits[0]);
            int month = Integer.valueOf(dateSplits[1]);
            int day = Integer.valueOf(dateSplits[2]);

            LocalDate formattedDate = LocalDate.of(year, month, day);

            setMed(medName, formattedDate, quant);

        }
        
        String message = "APPROV OK\n";
        write(message, outputFile);
    }

    public void write(String message, File outputFile){
        try (FileWriter writer = new FileWriter(outputFile, true)) {
            writer.write(message);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture dans le fichier de sortie : " + e.getMessage());
        }
    }

    public void date(String command, File outputFile){

        String[] splits = command.split(" ", 0);
        String unformattedDate = splits[1];

        String[] dateSplits = unformattedDate.split("-", 0);
        int year = Integer.valueOf(dateSplits[0]);
        int month = Integer.valueOf(dateSplits[1]);
        int day = Integer.valueOf(dateSplits[2]);
        
        LocalDate formattedDate = LocalDate.of(year, month, day);
        currentDate = formattedDate;

        String message;
        if (orderRegistry.isEmpty()){
            message = unformattedDate + " OK\n\n";
            write(message, outputFile);
        }
        else{
            message = unformattedDate + " COMMANDES :\n";
            write(message, outputFile);

            LinkedList<String> toRemove = new LinkedList<>();
            for (String med : orderRegistry.keySet()){
                Integer quant = orderRegistry.get(med);
                message = med + " " + quant.toString() + "\n";
                write(message, outputFile);
                toRemove.add(med);
            }

            write("\n", outputFile);

            while (!toRemove.isEmpty()){
                String med = toRemove.getFirst();
                orderRegistry.remove(med);
                toRemove.removeFirst();
            }
        }
    }

    public void prescription(String command, File outputFile){

        String message = "PRESCRIPTION " + prescriptionNumber + "\n";
        write(message, outputFile);
        prescriptionNumber++;

        String[] splits = command.split("\n", 0);
        for (int i=1; i<splits.length; i++){
            
            String[] preciseMedSplits = splits[i].split("[\\s\\t]+", 0);

            String[] nonEmptySplits = new String[3];
            int z = 0;
            for (String split : preciseMedSplits) {
                if (!split.isEmpty()) {
                    nonEmptySplits[z] = split;
                    z++;
                }
            }

            String med = nonEmptySplits[0];

            int quant = Integer.valueOf(nonEmptySplits[1]);
            int freq = Integer.valueOf(nonEmptySplits[2]);
            int totalQuant = quant*freq;

            boolean prescribed = false;

            if (medsRegistry.containsKey(med)){
                int totalStock = getTotalStock(med);
                if (totalStock >= totalQuant){
                    prescribeMed(med, totalQuant);
                    prescribed = true;
                }
                else {
                    orderMed(med, totalQuant);
                }
            }
            else {
                orderMed(med, totalQuant);
            }

            message = med + " " + quant + " " + freq;
            if (prescribed){
                message += " OK\n";
            }
            else {
                message += " COMMANDE\n";
            }

            write(message, outputFile);            

        }
        write("\n", outputFile);
    }

    public void orderMed(String med, int totalQuant){
        if (orderRegistry.containsKey(med)){
            int quant = orderRegistry.get(med);
            quant += totalQuant;
            orderRegistry.put(med, quant);
        }
        else{
            orderRegistry.put(med, totalQuant);
        }
    }

    public void prescribeMed(String med, int quant){
        TreeMap<LocalDate, Integer> medStock = medsRegistry.get(med);
        
        System.out.println(med);
        System.out.println(quant);
        
        LinkedList<LocalDate> toRemove = new LinkedList<>(); 
        for (Map.Entry<LocalDate, Integer> instance : medStock.entrySet()){
            int instanceQuant = instance.getValue();
            if (instanceQuant < quant){
                quant -= instanceQuant;
                toRemove.add(instance.getKey());
            }
            else {
                instanceQuant -= quant;
                medStock.put(instance.getKey(), instanceQuant);
                break;
            }

        }

        while (!toRemove.isEmpty()){
            LocalDate current = toRemove.getFirst();
            medStock.remove(current);
            toRemove.removeFirst();
        }

        medsRegistry.put(med, medStock);

    }
    
    public int getTotalStock(String med){
        int totalStock = 0;
        TreeMap<LocalDate, Integer> medStock = medsRegistry.get(med);
        for (Integer quant : medStock.values()){
            totalStock += quant;
        }
        return totalStock;
    }

    public void stock (File outputFile){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);

        String message = "STOCK " + formattedDate + "\n";

        write(message, outputFile);

        for (String med : medsRegistry.keySet()){
            TreeMap<LocalDate, Integer> medStock = medsRegistry.get(med);
            for (LocalDate date : medStock.keySet()){
                int quant = medStock.get(date);
                message = med + " " + quant + " " + date + "\n";
                write(message, outputFile);
            }
        }

        write ("\n", outputFile);
    }
}
