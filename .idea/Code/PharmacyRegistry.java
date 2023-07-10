import java.util.TreeMap;
import java.util.LinkedList;
import java.time.LocalDate;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class PharmacyRegistry {

    // the complexity analysis will be done directly within the code for
    // optimal clarity.
    //
    // It will only be done in the relevant functions and the functions called
    // by the relevant functions. These relevant functions are:
    //
    // 1 - approv()
    // 2 - date()
    // 3 - prescription()

    // let's add to the analysis the "s" variable that represents the med with
    // the most "sub-instances" of the med (same med with different expiration
    // dates.)
    // The "s" variable will be analized where needed.
    
    private TreeMap<String, TreeMap<LocalDate, Integer>> medsRegistry = new TreeMap<>();

    private TreeMap<String, Integer> orderRegistry = new TreeMap<>();

    private LocalDate currentDate;

    private int prescriptionNumber = 1;

    public TreeMap<LocalDate, Integer> getMed(String med){
        return medsRegistry.get(med);
    }

    // this function's overall complexity is
    // O(log(n) + log(s))
    public void setMed(String med, LocalDate date, Integer quantity){

        // first test is O(log(n))
        if (medsRegistry.containsKey(med)){

            // O(log(n)) to find the med and it's instances
            TreeMap<LocalDate, Integer> medInstances = medsRegistry.get(med);

            // O(log(s)) for a search of an instance.
            if (medInstances.containsKey(date)){

                // O(log(s)) to get the quantity stored by the key date.
                Integer oldQuantity = medInstances.get(date);
                // O(log(s)) to set the quantity stored by the key date.
                medInstances.put(date, oldQuantity + quantity);
            }
            else {
                // O(log(s)) tp set the quantity stored by the key date.
                medInstances.put(date, quantity);
            }
        }

        else {
            TreeMap<LocalDate, Integer> instance = new TreeMap<LocalDate, Integer>();
            instance.put(date, quantity);
            medsRegistry.put(med, instance);
        }
    }

    // whole function is O(k*(log(n) + log(s)))
    public void approv(String command, File outputFile){

        // O(k)
        String[] splits = command.split("\n", 0);
        
        // O(k*(log(n) + log(s))) because the setMed() function is executed m times.
        for (int i=1; i<splits.length; i++){

            // this whole sections is O(1)
            String[] newSplits = splits[i].split("[\\s\\t]+", 0);

            String medName = newSplits[0];
            int quant = Integer.valueOf(newSplits[1]);
            String date = newSplits[2];

            String[] dateSplits = date.split("-", 0);
            int year = Integer.valueOf(dateSplits[0]);
            int month = Integer.valueOf(dateSplits[1]);
            int day = Integer.valueOf(dateSplits[2]);

            LocalDate formattedDate = LocalDate.of(year, month, day);

            // O(log(n) + log(s)) -> see function analysis above
            setMed(medName, formattedDate, quant);

        }
        
        if (currentDate != null){
            // O(n*s*log(n)*log(s)) -> see function analysis below
            removeExpired(currentDate);
        }

        // O(1) for basic operations and write() funtion.
        String message = "APPROV OK\n";
        write(message, outputFile);
    }

    // write function writes the desired message into a single line in the
    // outputFile.
    // write function is O(1)
        public void write(String message, File outputFile){
        try (FileWriter writer = new FileWriter(outputFile, true)) {
            writer.write(message);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture dans le fichier de sortie : " + e.getMessage());
        }
    }

    
    // the whole function time complexity is :
    // O(n*s*log(n)*log(s) + k*log(k))
    public void date(String command, File outputFile){

        // O(1) because it splits a single line into predictable amount 
        // of parts.
        String[] splits = command.split(" ", 0);
        String unformattedDate = splits[1];

        // basic conversions and 3-part splits for O(1)
        String[] dateSplits = unformattedDate.split("-", 0);
        int year = Integer.valueOf(dateSplits[0]);
        int month = Integer.valueOf(dateSplits[1]);
        int day = Integer.valueOf(dateSplits[2]);
        
        // I don't know the LocalDate library precisely but i would assume 
        // that creating a date isn't more than O(1)
        LocalDate formattedDate = LocalDate.of(year, month, day);
        currentDate = formattedDate;

        // O(n*s*log(n)*log(s)) -> see function analysis below
        removeExpired(currentDate);

        String message; // O(1)

        // this is O(1) simple linear operations
        if (orderRegistry.isEmpty()){
            message = unformattedDate + " OK\n\n";
            write(message, outputFile);
        }

        // this is O(k*log(k)) for 
        else{
            message = unformattedDate + " COMMANDES :\n";
            write(message, outputFile);

            // this is O(k*log(k)) because we search for every single
            // order in the tree k times.
            LinkedList<String> toRemove = new LinkedList<>();
            for (String med : orderRegistry.keySet()){
                Integer quant = orderRegistry.get(med);
                message = med + " " + quant.toString() + "\n";
                write(message, outputFile);
                toRemove.add(med);
            }

            write("\n", outputFile);

            // this is O(k*log(k)), we remove all the orders.
            // sub-optimal because of the usage of a BST instead of
            // something O(1) i.e. LinkedLists.
            while (!toRemove.isEmpty()){

                //O(1) operations on first item of LinkedList.
                String med = toRemove.getFirst();
                // O(log(k)) I should have just used a LinkedList instead
                // of a tree for the orders, but since this homework is themed
                // arount BST, i used a tree instead. LinkedList would've been O(1).
                orderRegistry.remove(med);
                // O(1).
                toRemove.removeFirst();
            }
        }
    }


    
    // Removes the expired med instances based on the current date. 
    // grand total seems to be O(nlog(n)*slog(s) + O(n*s*log(s))
    // simplified to : O(n*s*log(n)*log(s))
    public void removeExpired(LocalDate currentDate){

        // O(1)
        LinkedList<LocalDate> instanceToRemove = new LinkedList<>();

        // O(n)*O(log(n)) = O(nlog(n)) for iteration through all values of the tree
        for (TreeMap<LocalDate, Integer> stock : medsRegistry.values()){

            // O(slog(s)) for iteration through all values of the tree
            for (LocalDate date : stock.keySet()){

                // O(1) for basic tests and adding to a LinkedList
                if (date.isBefore(currentDate) || date.equals(currentDate)){
                    instanceToRemove.add(date);
                }
            }

            // O(ns) worst case every med and it's sub-instances are expired
            // adds up to O(n*s*log(s)) with the operations in the while.
            while (!instanceToRemove.isEmpty()){

                // O(log(s)) for removal of a value in the tree.
                stock.remove(instanceToRemove.getFirst());
                instanceToRemove.removeFirst();
            }
        }
    }

    // whole function's complexity is 
    // O(m*(s*log(s) + log(n) + log(k))) 
    public void prescription(String command, File outputFile){

        // this section is O(1)
        String message = "PRESCRIPTION " + prescriptionNumber + "\n";
        write(message, outputFile);
        prescriptionNumber++;

        // this section is O(m) because of linear treatment of m lines of input.
        String[] splits = command.split("\n", 0);
        for (int i=1; i<splits.length; i++){
            

            // small section is O(1)
            String[] preciseMedSplits = splits[i].split("[\\s\\t]+", 0);

            String[] nonEmptySplits = new String[3];
            int z = 0;
            for (String split : preciseMedSplits) {
                if (!split.isEmpty()) {
                    nonEmptySplits[z] = split;
                    z++;
                }
            }

            // this small section is O(1)
            String med = nonEmptySplits[0];

            int quant = Integer.valueOf(nonEmptySplits[1]);
            int freq = Integer.valueOf(nonEmptySplits[2]);
            int totalQuant = quant*freq;

            boolean prescribed = false;
            LocalDate minExpiryDate = currentDate.plusDays(totalQuant);


            if (medsRegistry.containsKey(med)){ //O(log(n))

                // O(s*log(s) + log(n)) -> see analysis of getTotalStock below
                int totalStock = getTotalStock(med, minExpiryDate, totalQuant);
                if (totalStock >= totalQuant){
                    // O(s*log(s) + log(n)) -> see analysis of prescribeMed below
                    prescribeMed(med, totalQuant);
                    prescribed = true;
                }
                else {
                    // O(log(k))
                    orderMed(med, totalQuant);
                }
            }
            else {
                orderMed(med, totalQuant); // O(log(k))
            }

            // O(1)
            message = med + " " + quant + " " + freq;
            if (prescribed){
                message += " OK\n";
            }
            else {
                message += " COMMANDE\n";
            }

            //O(1)
            write(message, outputFile);            

        }

        // O(1)
        write("\n", outputFile);
    }

    // O(log(k))
    public void orderMed(String med, int totalQuant){
        if (orderRegistry.containsKey(med)){
            int quant = orderRegistry.get(med); // O(log(k))
            quant += totalQuant;
            orderRegistry.put(med, quant); // O(log(k))
        }
        else{
            orderRegistry.put(med, totalQuant); // O(log(k))
        }
    }

    // O(s*log(s) + log(n))
    public void prescribeMed(String med, int quant){

        // O(log(n))
        TreeMap<LocalDate, Integer> medStock = medsRegistry.get(med);
         
        // O(s*log(s))
        for (LocalDate date : medStock.keySet()){
            int instanceQuant = medStock.get(date);
            if (instanceQuant == quant){
                medStock.remove(date);
                break;
            }
            if (instanceQuant > quant){
                instanceQuant -= quant;
                medStock.put(date, instanceQuant);
                break;
            }

        }

        medsRegistry.put(med, medStock);

    }
    
    // getTotalStock is O(s*log(s) + log(n))
    public int getTotalStock(String med, LocalDate minExpiryDate, int totalQuant){

        // beginning section is O(1)
        int totalStock = 0;
        LocalDate totalStockDate = LocalDate.of(2023,12,31);

        // O(log(n))
        TreeMap<LocalDate, Integer> medStock = medsRegistry.get(med);

        // O(s*log(s))
        for (LocalDate date : medStock.keySet()){
            boolean isAfterOrEqual = date.isAfter(minExpiryDate) || date.equals(minExpiryDate);
            if (isAfterOrEqual) {
                int instanceStock = medStock.get(date); // O(log(s))
                if (instanceStock >= totalQuant && date.isBefore(totalStockDate)){ //O(1)
                    totalStock = instanceStock;
                    totalStockDate = date;
                }
            }
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
