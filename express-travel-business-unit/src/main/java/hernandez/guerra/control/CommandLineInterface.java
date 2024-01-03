package hernandez.guerra.control;

import java.util.Scanner;

public class CommandLineInterface {
    private final DatamartInitializer dataMartInitializer;
    private final ExpressTravelDatamart datamart;

    public CommandLineInterface(DatamartInitializer dataMartInitializer, ExpressTravelDatamart datamart) {
        this.dataMartInitializer = dataMartInitializer;
        this.datamart = datamart;
    }
    public  void run() {
        /*
        String dbPath = args[0];
        String eventStoreDirectory = "eventStore";

        DatamartInitializer dataMartInitializer = new DatamartInitializer(eventStoreDirectory);
        ExpressTravelDatamart datamart = new ExpressTravelSQLiteDatamart(dbPath);


         */

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. Show all data");
            System.out.println("2. Surprise me!");
            System.out.println("3. Set my preferences");
            System.out.println("4. Recommendations");
            System.out.println("5. Exit");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    break;
                case 2:
                    //Show the best option
                    break;
                case 3:
                    //Show options to set preferences
                    break;
                case 4:
                    //Show recommendations
                    break;
                case 5:
                    System.out.println("Leaving...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }
}
