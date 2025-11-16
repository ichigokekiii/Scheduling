import java.util.Scanner;

public class MainPage {
    public static void main(String[] args) {

        Scanner obj = new Scanner(System.in);
        

        System.out.println("MAIN MENU");
        System.out.println("[A] SRTF (Shortest Remaining Time First) Scheduling Algorithm");
        System.out.println("[B] Priority (Non-Preemptive) Scheduling Algorithm");
        System.out.println("[C] C-LOOK Disk Scheduling Algorithm");
        System.out.println("[D] SCAN Disk Scheduling Algorithm");
        System.out.print("What do you want to do today? ");
        String mainChoice = obj.nextLine();

        switch(mainChoice) {
            case "A":
                SRTF srtf = new SRTF();
                srtf.srtfResult();
                break;
            case "B":
                Priority prio = new Priority();
                prio.prioResult();
                break;
            case "C":
                CLook clook = new CLook();
                clook.cLookResult();
                break;
            case "D":
                Skan scan = new Skan();
                scan.scanResult();
                break;
            case "X":
                System.out.println("Exiting the program. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please select a valid option from the menu.");
                break;
        }
    }
}
