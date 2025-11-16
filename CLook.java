import java.util.Scanner;

public class CLook {

    public void cLookResult() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of requests: ");
        int n = sc.nextInt();

        DiskRequest req[] = new DiskRequest[n];

        for (int i = 0; i < n; i++) {
            System.out.print("Request " + (i + 1) + ": ");
            int track = sc.nextInt();
            req[i] = new DiskRequest(track);
        }

        System.out.print("Enter head position: ");
        int head = sc.nextInt();

        computeCLook(req, n, head);
    }

    private void computeCLook(DiskRequest[] req, int n, int head) {

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (req[j].track > req[j + 1].track) {
                    DiskRequest temp = req[j];
                    req[j] = req[j + 1];
                    req[j + 1] = temp;
                }
            }
        }

        int startIndex = -1;

        for (int i = 0; i < n; i++) {
            if (req[i].track >= head) {
                startIndex = i;
                break;
            }
        }

        if (startIndex == -1) startIndex = n;

        int gantt[] = new int[n];
        int gIndex = 0;

        int totalMovement = 0;
        int current = head;

        for (int i = startIndex; i < n; i++) {

            req[i].movement = Math.abs(req[i].track - current);
            totalMovement += req[i].movement;

            current = req[i].track;
            req[i].served = true;

            gantt[gIndex++] = current;
        }

        if (startIndex != 0) {

            int highest = req[n - 1].track;
            int lowest  = req[0].track;

            int wrapMovement = Math.abs(highest - lowest);

            totalMovement += wrapMovement;

            req[0].movement = wrapMovement;

            current = lowest;
            gantt[gIndex++] = current;
        }

        for (int i = 1; i < startIndex; i++) {
            req[i].movement = Math.abs(req[i].track - current);
            totalMovement += req[i].movement;

            current = req[i].track;
            req[i].served = true;

            gantt[gIndex++] = current;
        }

        System.out.println("\nORDER OF SERVICE (C-LOOK):");
        for (int i = 0; i < gIndex; i++) {
            System.out.print(gantt[i]);
            if (i < gIndex - 1) System.out.print(" -> ");
        }

        System.out.println("\n\nTotal Track Movement: " + totalMovement + " cylinders");

        System.out.println("\nDETAILS PER REQUEST (For animations):");
        System.out.println("Track\tMovement\tServed");

        for (int i = 0; i < n; i++) {
            System.out.println(
                req[i].track + "\t" +
                req[i].movement + "\t\t" +
                req[i].served
            );
        }
    }
}

