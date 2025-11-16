import java.util.Scanner;

public class Skan {

    public void scanResult() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of requests: ");
        int n = sc.nextInt();

        DiskRequest req[] = new DiskRequest[n];

        for (int i = 0; i < n; i++) {
            System.out.print("Request " + (i + 1) + ": ");
            req[i] = new DiskRequest(sc.nextInt());
        }

        System.out.print("Enter head position: ");
        int head = sc.nextInt();

        computeSCAN(req, n, head);
    }

    private void computeSCAN(DiskRequest[] req, int n, int head) {

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (req[j].track > req[j + 1].track) {
                    DiskRequest t = req[j];
                    req[j] = req[j + 1];
                    req[j + 1] = t;
                }
            }
        }

        int startIndex = n;
        for (int i = 0; i < n; i++) {
            if (req[i].track >= head) {
                startIndex = i;
                break;
            }
        }

        int gantt[] = new int[n + 2];
        int gIndex = 0;

        int totalMovement = 0;
        int current = head;

        System.out.println("\nSCAN SERVICE ORDER (Professor Version):");
        System.out.print(current);

        for (int i = startIndex; i < n; i++) {
            req[i].movement = Math.abs(req[i].track - current);
            totalMovement += req[i].movement;
            current = req[i].track;
            req[i].served = true;

            gantt[gIndex++] = current;
            System.out.print(" -> " + current);
        }

        int movementTo199 = Math.abs(199 - current);
        totalMovement += movementTo199;

        current = 199;

        gantt[gIndex++] = 199;
        System.out.print(" -> " + 199 + " (boundary)");

        for (int i = startIndex - 1; i >= 0; i--) {

            req[i].movement = Math.abs(req[i].track - current);
            totalMovement += req[i].movement;

            current = req[i].track;
            req[i].served = true;

            gantt[gIndex++] = current;
            System.out.print(" -> " + current);
        }

        if (current != 0) {
            int movementTo0 = current;
            totalMovement += movementTo0;

            current = 0;
            gantt[gIndex++] = 0;
            System.out.print(" -> 0 (boundary)");
        }

        System.out.println("\n\nTotal Track Movement: " + totalMovement + " cylinders");

        System.out.println("\nDETAILS PER REQUEST (For animations):");
        System.out.println("Track\tMovement\tServed");
        for (int i = 0; i < n; i++) {
            System.out.println(req[i].track + "\t" + req[i].movement + "\t\t" + req[i].served);
        }
    }
}
