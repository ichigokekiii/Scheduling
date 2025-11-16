import java.util.Scanner;

public class SRTF {

    public void srtfResult() {

        Scanner obj = new Scanner(System.in);

        System.out.print("Input number of processes: ");
        int n = obj.nextInt();

        String pid[] = new String[n];
        int arrivalTime[] = new int[n];
        int burstTime[] = new int[n];

        for (int i = 0; i < n; i++) {
            pid[i] = "P" + (i + 1);
            System.out.print("Arrival Time of " + pid[i] + ": ");
            arrivalTime[i] = obj.nextInt();
        }

        for (int i = 0; i < n; i++) {
            System.out.print("Burst Time of " + pid[i] + ": ");
            burstTime[i] = obj.nextInt();
        }

        Process[] p = new Process[n];
        for (int i = 0; i < n; i++) {
            p[i] = new Process(pid[i], arrivalTime[i], burstTime[i]);
        }
        computeSRTF(p, n);
    }


    private void computeSRTF(Process[] p, int n) {

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (p[j].arrivalTime > p[j + 1].arrivalTime) {
                    Process temp = p[j];
                    p[j] = p[j + 1];
                    p[j + 1] = temp;
                }
            }
        }

        int totalBurst = 0;
        for (int i = 0; i < n; i++){
            totalBurst += p[i].burstTime;
        }

        String gantt[] = new String[totalBurst];
        int gIndex = 0;

        int time = 0;
        int completed = 0;

        while (completed < n) {

            int chosen = -1;
            int smallestRemaining = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {

                if (p[i].arrivalTime <= time && p[i].remainingTime > 0) {

                    if (p[i].remainingTime < smallestRemaining) {
                        smallestRemaining = p[i].remainingTime;
                        chosen = i;
                    }
                    else if (p[i].remainingTime == smallestRemaining) {

                        if (p[i].arrivalTime < p[chosen].arrivalTime)
                            chosen = i;

                        else if (p[i].arrivalTime == p[chosen].arrivalTime) {

                            if (p[i].pid.compareTo(p[chosen].pid) < 0)
                                chosen = i;
                        }
                    }
                }
            }

            //Napagod Nako in-AI ko nalang tong part nato wahhahaha
            if (chosen == -1) {
                gantt[gIndex++] = "idle";
                time++;
                continue;
            }

            if (p[chosen].start == -1)
                p[chosen].start = time;

            p[chosen].remainingTime--;
            gantt[gIndex++] = p[chosen].pid;
            time++;

            if (p[chosen].remainingTime == 0) {
                p[chosen].completionTime = time;
                completed++;
            }
        }

        String[] grouped = new String[totalBurst];
        int groupedSize = 0;

        int count = 1;
        for (int i = 1; i < totalBurst; i++) {

            if (gantt[i].equals(gantt[i - 1])) {
                count++;
            } else {
                grouped[groupedSize++] = gantt[i - 1] + "(" + count + ")";
                count = 1;
            }
        }

        grouped[groupedSize++] = gantt[totalBurst - 1] + "(" + count + ")";

        System.out.println("\nPROCESS TABLE:");
        System.out.println("PID\tAT\tBT\tCT\tTAT\tWT");

        double totalWT = 0, totalTAT = 0, totalCT = 0;

        for (int i = 0; i < n; i++) {
            Process pr = p[i];

            int tat = pr.turnaroundTime();
            int wt = pr.waitingTime();

            totalWT += wt;
            totalTAT += tat;
            totalCT += pr.completionTime;

            System.out.println(
                pr.pid + "\t" +
                pr.arrivalTime + "\t" +
                pr.burstTime + "\t" +
                pr.completionTime + "\t" +
                tat + "\t" + wt
            );
        }

        System.out.println("\nAverage Turnaround Time: " + (totalTAT / n));
        System.out.println("Average Waiting Time: " + (totalWT / n));
        System.out.println("Average Completion Time: " + (totalCT / n));

        System.out.println("\nGANTT CHART:");
        for (int i = 0; i < groupedSize; i++) {
            System.out.print("| " + grouped[i] + " ");
        }
        System.out.println("|");
    }
}
