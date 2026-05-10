import java.util.*;

public class Main {

    public static void main(String[] args) {
        Scanner      scanner = new Scanner(System.in);
        UserInput input   = new UserInput(scanner);

        printBanner();

        // Step 1: collect process data (done once per program run) 
        List<Process> processes = input.collectProcesses();

        // Step 2: Select loop 
        do {
            // 2.1 — let the user choose an algorithm
            int choice  = input.selectAlgorithm();
            int quantum = 0;

            // 2.2 — request time quantum only for RR-based algorithms
            if (choice == 4 || choice == 7) {
                quantum = input.getQuantum();
            }

            // 2.3 — run the chosen algorithm
            SchedulerResult result = dispatch(choice, processes, quantum);

            // 2.4 — print the full output (Gantt + table + averages)
            Output.printResult(result, getAlgorithmName(choice));

        } while (input.askRerun()); // Step 3 — Ask if re-run with same processes
            //if no then goodbye
        System.out.println("\nThank you for using the CPU Scheduling Program. Babye!");
        scanner.close();
    }


    // DISPATCH — routes algorithm choice to the correct Scheduler method.

    private static SchedulerResult dispatch(int choice,
                                             List<Process> processes,
                                             int quantum) {
        switch (choice) {
            case 1:  return Scheduler.fcfs(processes);
            case 2:  return Scheduler.sjf(processes);
            case 3:  return Scheduler.srt(processes);
            case 4:  return Scheduler.roundRobin(processes, quantum);
            case 5:  return Scheduler.priorityNP(processes);
            case 6:  return Scheduler.priorityP(processes);
            case 7:  return Scheduler.priorityRR(processes, quantum);
            default: return Scheduler.fcfs(processes); // Return scheduler to fcfs simply it falls back
        }
    }

     //getAlgorithmName() — returns the full display name when the user choose
    private static String getAlgorithmName(int choice) {
        switch (choice) {
            case 1: return "First-Come, First-Served (FCFS) — Non-Preemptive";
            case 2: return "Shortest Job First (SJF) — Non-Preemptive";
            case 3: return "Shortest Remaining Time (SRT) — Preemptive";
            case 4: return "Round Robin (RR) — Preemptive";
            case 5: return "Priority Scheduling — Non-Preemptive   (lower value = higher priority)";
            case 6: return "Priority Scheduling — Preemptive       (lower value = higher priority)";
            case 7: return "Priority + Round Robin                 (lower value = higher priority)";
            default: return "Unknown Algorithm";
        }
    }

    
     //printBanner() — displays the welcome screen.
    private static void printBanner() {
        System.out.println("|==============================================|");
        System.out.println("|            CPU SCHEDULING PROGRAM            |");
        System.out.println("|==============================================|");
    }
}