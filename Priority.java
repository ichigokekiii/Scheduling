import java.util.Scanner;

public class Priority {

    public void prioResult() {

        Scanner obj = new Scanner(System.in);

        System.out.print("Input number of processes: ");
        int n = obj.nextInt();

        String pid[] = new String[n];
        int arrivalTime[] = new int[n];
        int burstTime[] = new int[n];
        int priority[] = new int[n];

        for (int i = 0; i < n; i++) {
            pid[i] = "P" + (i + 1);
            System.out.print("Arrival Time of " + pid[i] + ": ");
            arrivalTime[i] = obj.nextInt();
        }

        for (int i = 0; i < n; i++) {
            System.out.print("Burst Time of " + pid[i] + ": ");
            burstTime[i] = obj.nextInt();
        }

        for (int i = 0; i < n; i++) {
            System.out.print("Priority of " + pid[i] + ": ");
            priority[i] = obj.nextInt();
        }

        Process[] p = new Process[n];
        for (int i = 0; i < n; i++) {
            p[i] = new Process(pid[i], arrivalTime[i], burstTime[i], priority[i]);
        }

        computePriority(p, n);
    }

    private void computePriority(Process[] p, int n) {

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (p[j].arrivalTime > p[j + 1].arrivalTime) {
                    Process temp = p[j];
                    p[j] = p[j + 1];
                    p[j + 1] = temp;
                }
            }
        }

        int time = 0;
        int completed = 0;

        String gantt[] = new String[n]; 
        int gIndex = 0;

        boolean done[] = new boolean[n];

        while (completed < n) {

            int chosen = -1;

            for (int i = 0; i < n; i++) {

                if (!done[i] && p[i].arrivalTime <= time) {

                    if (chosen == -1) {
                        chosen = i;
                    }
                    else {

                        if (p[i].priority < p[chosen].priority)
                            chosen = i;

                        else if (p[i].priority == p[chosen].priority) {

                            if (p[i].burstTime < p[chosen].burstTime)
                                chosen = i;

                            else if (p[i].burstTime == p[chosen].burstTime) {

                                if (p[i].arrivalTime < p[chosen].arrivalTime)
                                    chosen = i;

                                else if (p[i].arrivalTime == p[chosen].arrivalTime) {
                                    if (p[i].pid.compareTo(p[chosen].pid) < 0)
                                        chosen = i;
                                }
                            }
                        }
                    }
                }
            }

            if (chosen == -1) {
                time++;
                continue;
            }

            if (p[chosen].start == -1)
                p[chosen].start = time;

            time += p[chosen].burstTime;
            p[chosen].completionTime = time;
            done[chosen] = true;
            completed++;

            gantt[gIndex++] = p[chosen].pid + "(" + p[chosen].burstTime + ")";
        }

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (p[j].pid.compareTo(p[j + 1].pid) > 0) {
                    Process temp = p[j];
                    p[j] = p[j + 1];
                    p[j + 1] = temp;
                }
            }
        }

        System.out.println("\nPROCESS TABLE:");
        System.out.println("PID\tAT\tBT\tPRIO\tCT\tTAT\tWT");

        double totalWT = 0, totalTAT = 0, totalCT = 0;

        for (int i = 0; i < n; i++) {

            int tat = p[i].turnaroundTime();
            int wt = p[i].waitingTime();

            totalTAT += tat;
            totalWT += wt;
            totalCT += p[i].completionTime;

            System.out.println(
                p[i].pid + "\t" +
                p[i].arrivalTime + "\t" +
                p[i].burstTime + "\t" +
                p[i].priority + "\t" +
                p[i].completionTime + "\t" +
                tat + "\t" + wt
            );
        }

        System.out.println("\nAverage Turnaround Time: " + (totalTAT / n));
        System.out.println("Average Waiting Time: " + (totalWT / n));
        System.out.println("Average Completion Time: " + (totalCT / n));

        System.out.println("\nGANTT CHART:");
        for (int i = 0; i < gIndex; i++) {
            System.out.print("| " + gantt[i] + " ");
        }
        System.out.println("|");
    }
}
