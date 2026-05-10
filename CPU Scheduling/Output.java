import java.util.*;


  //Handles ALL console output for the scheduler.
 

public class Output {

    //These are the visual separators for console output
    private static final int    MIN_CELL_WIDTH = 4;  // each ganttchart cell has atleast 4 character
    private static final String DIVIDER        = "=".repeat(85);
    private static final String THIN_DIVIDER   = "-".repeat(85);

  
    public static void printResult(SchedulerResult result, String algoName) {
        System.out.println("\n" + DIVIDER);
        System.out.println("  The result for " + algoName);
       

        printGantt(result.getGanttChart());
        printTable(result.getProcesses());
        printAverages(result.getAvgWaitingTime(), result.getAvgTurnaroundTime());
    }


  
    public static void printGantt(List<GanttChart> gantt) {
        if (gantt == null || gantt.isEmpty()) {
            System.out.println("\n  (Gantt chart is empty)");
            return;
        }
        //output section
        System.out.println("     |==============================================|");
        System.out.println("     |                   Outputs                    |");
        System.out.println("     |  Gantt Chart | WT / TAT Table | Averages     |");
        System.out.println("     |==============================================|");
        System.out.println("\nGantt Chart:");
        
        //  compute in advanced each cell's display width 
        int[] cellWidths = new int[gantt.size()];
        for (int i = 0; i < gantt.size(); i++) {
            GanttChart g = gantt.get(i);
            // Width = max of (duration, label length + padding)
            cellWidths[i] = Math.max(MIN_CELL_WIDTH,
                                     Math.max(g.getEnd() - g.getStart(), g.getProcessId().length() + 2));
        }

        // Row 1: top border  draws the top border of the chart
        StringBuilder topBorder = new StringBuilder("  +");
        for (int w : cellWidths) topBorder.append("-".repeat(w)).append("+");
        System.out.println(topBorder);

        // Row 2: process labels, centers each process lables inside it cell
        StringBuilder labelRow = new StringBuilder("  |");
        for (int i = 0; i < gantt.size(); i++) {
            String label    = gantt.get(i).getProcessId();
            int    w        = cellWidths[i];
            int    padLeft  = (w - label.length()) / 2;
            int    padRight = w - label.length() - padLeft;
            labelRow.append(" ".repeat(padLeft))
                    .append(label)
                    .append(" ".repeat(padRight))
                    .append("|");
        }
        System.out.println(labelRow);

        //  Row 3: bottom border same pattern as top border
        System.out.println(topBorder); 

        //  Row 4: time ticks below each cell boundary 
        // Each tick is printed at the position of its cell's left edge.
        // The final tick is printed after the last cell.
        StringBuilder tickRow = new StringBuilder("  ");
        for (int i = 0; i < gantt.size(); i++) {
            String tick = String.valueOf(gantt.get(i).getStart());
            tickRow.append(tick);
            // Pad to align the next tick at the start of the next cell
            // (+1 for the border character "|")
            int advance = cellWidths[i] + 1 - tick.length();
            if (advance > 0) tickRow.append(" ".repeat(advance));
        }
        // Append the final end-time tick
        tickRow.append(gantt.get(gantt.size() - 1).getEnd());
        System.out.println(tickRow);
    }


        //Prints table headers with aligned columns.
    public static void printTable(List<Process> processes) {
        System.out.println("\nProcess Summary Table:");
        System.out.println(THIN_DIVIDER);

        //  Column header 
        System.out.printf("%-10s %-10s %-10s %-10s %-14s %-16s %-14s%n",
                "Process", "Arrival", "Burst", "Priority",
                "Completion", "Turnaround(TAT)", "Waiting(WT)");
        System.out.println(THIN_DIVIDER);

        //  One data row per process 
        for (Process p : processes) {
            System.out.printf("%-10s %-10d %-10d %-10d %-14d %-16d %-14d%n",
                    p.getId(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getPriority(),
                    p.getCompletionTime(),
                    p.getTurnaroundTime(),
                    p.getWaitingTime());
        }

        System.out.println(THIN_DIVIDER);
    }

    /**
     * printAverages() — displays the computed mean Waiting Time and mean
     * Turnaround Time, formatted to two decimal places.
     *
     * Formulas:
     *   Average WT  = sumWT  / nprocess
     *   Average TAT = sumTAT / nprocess
    */
    public static void printAverages(double avgWT, double avgTAT) {
        System.out.printf("%nAverage Turnaround Time (TAT) : %.2f%n", avgTAT);
        System.out.printf("Average Waiting Time (WT)     : %.2f%n",   avgWT);
        System.out.println(DIVIDER);
    }
}