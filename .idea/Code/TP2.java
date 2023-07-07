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


        }
        catch (FileNotFoundException e){
            System.out.println("The file " + args[0] + " doesn't exist");
        }

    }
}