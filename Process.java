public class Process {

    public String pid;
    public int arrivalTime;
    public int burstTime;
    public int remainingTime;
    public int priority = -1;
    public int completionTime = -1;
    public int start = -1;

    public Process(String pid, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }

    public Process(String pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
    }

    public int turnaroundTime() {
        return completionTime - arrivalTime;
    }

    public int waitingTime() {
        return turnaroundTime() - burstTime;
    }
    
}
