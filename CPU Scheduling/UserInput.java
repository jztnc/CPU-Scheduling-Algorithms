import java.util.*;

    //Handles all user input w validation.
public class UserInput {

    private final Scanner _scanner;

    /** Constructor — accepts an external Scanner so it can be shared with Main. */
    public UserInput(Scanner scanner) {
        _scanner = scanner;
    }

        //gathers process data (IDs, AT, BT, Priority)
        public List<Process> collectProcesses() {
        System.out.println("  PROCESS INPUT");
        
      
        // Step 1 — number of processes (minimum 3)
        int n = readInt("\n Enter number of processes (minimum 3): ", 3, Integer.MAX_VALUE);

        List<Process> processes = new ArrayList<>();

        // Step 2 — per-process data entry
        for (int i = 1; i <= n; i++) {
            System.out.println("\n Process #" + i);

            // 2.1 — Process ID auto-generated as P1, P2, ...
            String defaultId = "P" + i;
            // User input process ID
            String id = readString("   Process ID (" + defaultId + ") : ", defaultId);

            // 2.2 — User input Arrival Time
            int at = readInt("   Arrival Time  (≥ 0): ", 0, Integer.MAX_VALUE);

            // 2.3 — User input Burst Time (at least 1)
            int bt = readInt("   Burst Time    (≥ 1): ", 1, Integer.MAX_VALUE);

            // 2.4 — User input priority (lower = higher priority)
            int pr = readInt("   Priority      (≥ 0, lower = higher priority): ", 0, Integer.MAX_VALUE);

            processes.add(new Process(id, at, bt, pr));
        }

        return processes;
    }


     //selectAlgorithm() — displays the algorithm menu and ask user choice
    public int selectAlgorithm() {
        System.out.println("\n" + "─".repeat(60));
        System.out.println("  SELECT SCHEDULING ALGORITHM");
        System.out.println("─".repeat(60));
        System.out.println("|  1. FCFS   (First-Come, First-Served)       [Non-Preemptive)]");
        System.out.println("|  2. SJF    (Shortest Job First      )       [Non-Preemptive)]");
        System.out.println("|  3. SRT    (Shortest Remaining Time )       [Preemptive)    ]");
        System.out.println("|  4. RR     (Round Robin             )       [Preemptive)    ]");
        System.out.println("|  5. PNP    (Priority Scheduling     )       [Non-Preemptive)]");
        System.out.println("|  6. PP     (Priority Scheduling     )       [Preemptive)    ]");
        System.out.println("|  7. PRR    (Priority + Round Robin  )       [Preemptive)    ]");
        System.out.println("─".repeat(60));
        return readInt("  Enter choice (1–7): ", 1, 7);
    }


        //Quantum Input
        //prompts for and validates the time quantum
    public int getQuantum() {
        return readInt("Enter Time Quantum (≥ 1): ", 1, Integer.MAX_VALUE);
    }

    //askRerun() — asks whether the user wants to run another algorithm on the same process
    // y or Y Return true
    public boolean askRerun() {
        System.out.print("\nRun another algorithm with the same processes? (y/n): ");
        String response = _scanner.nextLine().trim().toLowerCase();
        return response.equals("y");
    }

    //Handles non-numeric input. reads and validates an integer.
    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(_scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.printf("  \n Please enter a value between %d and so on...", min, max);
            } catch (NumberFormatException e) {
                System.out.println("   Invalid input. please enter a whole number.");
            }
        }
    }
    //readString() — reads a line of text if the press enter without typing anythinhg then promt the catch
    public String readString(String prompt, String defaultValue) {
        System.out.print(prompt);
        String input = _scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }
}