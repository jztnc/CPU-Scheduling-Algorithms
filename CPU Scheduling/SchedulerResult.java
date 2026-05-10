import java.util.*;

 // Immutable container returned by every scheduling algorithm in Scheduler.java.

public class SchedulerResult {

    public final List<Process>    processes;           // Completed processes with WT / TAT filled
    public final List<GanttChart> ganttChart;          // Ordered execution timeline blocks
    public final double           avgWaitingTime;      // Mean WT  across all n processes
    public final double           avgTurnaroundTime;   // Mean TAT across all n processes

    /**
     * receives the final process list and Gantt chart,
     * then computes both average metrics.
     */
    public SchedulerResult(List<Process> processes, List<GanttChart> ganttChart) {
        this.processes  = processes;
        this.ganttChart = ganttChart;

        // Compute averages
        // Sum WT and TAT across all processes, then divide by count.
        double totalWT = 0, totalTAT = 0;
        for (Process p : processes) {
            totalWT  += p.getWaitingTime();
            totalTAT += p.getTurnaroundTime();
        }
        int n = processes.size();
        this.avgWaitingTime     = (n > 0) ? totalWT  / n : 0;
        this.avgTurnaroundTime  = (n > 0) ? totalTAT / n : 0;

       
    }
        public List<Process>    getProcesses()          { return processes; }
        public List<GanttChart> getGanttChart()         { return ganttChart; }
        public double           getAvgWaitingTime()     { return avgWaitingTime; }
        public double           getAvgTurnaroundTime()  { return avgTurnaroundTime; }
}