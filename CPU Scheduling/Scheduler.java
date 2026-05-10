import java.util.*;
// All scheduling algorithms
public class Scheduler {
   
    public static SchedulerResult fcfs(List<Process> original) {
        List<Process>    processes = newProcess(original);
        List<GanttChart> gantt     = new ArrayList<>();

        // Step 1 — sort by arrival time; identical arrivals ordered by ID
        processes.sort(Comparator.comparingInt((Process p) -> p.getArrivalTime())
                                 .thenComparing(p -> p.getId()));

        int time = 0; // Current clock tick

        for (Process p : processes) {
            // Step 2 — if CPU is free before process arrives, insert an IDLE block
            if (time < p.getArrivalTime()) {
                gantt.add(new GanttChart("IDLE", time, p.getArrivalTime()));
                time = p.getArrivalTime();
            }

            // Step 3 — record first-time-on-CPU and run to completion
            p.setStartTime(time);
            gantt.add(new GanttChart(p.getId(), time, time + p.getBurstTime()));
            time += p.getBurstTime();
            
            // Step 4 — compute output metrics
            p.setCompletionTime(time);
            p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime()); // TAT = CT - AT
            p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());      // WT = TAT - BT
        }

        return new SchedulerResult(processes, gantt);
    }


    /**
     * SJF — at each dispatch point pick the ARRIVED process with the
     * smallest burst time; run it to completion without interruption.
     *   1. At current time, find all processes that have arrived.
     *   2. Among them, choose the one with the minimum burst time.
     *   3. Execute it fully, then repeat.
     *   4. If no process is ready, advance the clock to the next arrival.
     */
    public static SchedulerResult sjf(List<Process> original) {
        List<Process>    processes = newProcess(original);
        List<GanttChart> gantt     = new ArrayList<>();
        List<Process>    remaining = new ArrayList<>(processes); // Processes not yet executed
        List<Process>    done      = new ArrayList<>();           // Finished processes (ordered)

        int time = 0;

        while (!remaining.isEmpty()) {
            // Step 1 — collect all processes that have arrived by `time`
            List<Process> ready = new ArrayList<>();
            for (Process p : remaining)
                if (p.getArrivalTime() <= time) ready.add(p);

            if (ready.isEmpty()) {
                // CPU is idle — jump clock to the next process arrival
                int nextArrival = remaining.stream()
                                           .mapToInt(p -> p.getArrivalTime())
                                           .min().orElse(time + 1);
                gantt.add(new GanttChart("IDLE", time, nextArrival));
                time = nextArrival;
                continue;
            }

            // Step 2 — pick shortest burst; tie-break: earlier arrival, then ID
           ready.sort(Comparator.comparingInt(Process::getBurstTime)
                                    .thenComparingInt(Process::getArrivalTime)
                                    .thenComparing(Process::getId));

            Process p = ready.get(0);

            remaining.remove(p);

            // Step 3 — run to completion
            p.setStartTime(time);
            gantt.add(new GanttChart(p.getId(), time, time + p.getBurstTime()));
            time += p.getBurstTime();

            // Step 4 — compute metrics
            p.setCompletionTime(time);
            p.setTurnaroundTime(p.getCompletionTime()- p.getArrivalTime());
            p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
            done.add(p);
        }

        return new SchedulerResult(done, gantt);
    }

    /**
     * SRT — preemptive version of SJF.  At EVERY clock tick, the process
     * with the sm burst time is given the CPU.  If a newly
     * arrived process has a shorter remaining time than the running process,
     * the running process is immediately preempted.
     * 
     *   1. At each tick, find the arrived process with minimum remainingTime.
     *   2. Run it for exactly one tick (decrement remainingTime by 1).
     *   3. If it finishes (remainingTime == 0), compute metrics.
     *   4. Track Gantt slices: record a new entry whenever the active process
     *      changes, then merge consecutive same-process blocks at the end.
     */
    public static SchedulerResult srt(List<Process> original) {
        List<Process>    processes = newProcess(original);
        List<GanttChart> gantt     = new ArrayList<>();

        int n         = processes.size();
        int completed = 0;
        int time      = 0;

        // Upper-bound clock: worst case is all processes run sequentially
        int totalBurst = processes.stream().mapToInt(p -> p.getBurstTime()).sum();
        int maxTime    = processes.stream().mapToInt(p -> p.getArrivalTime()).max().orElse(0)
                         + totalBurst + 1;

        // Gantt slice tracking — record changes in the running process
        String lastId    = null;
        int    sliceStart = 0;

        while (completed < n && time <= maxTime) {

            // Step 1 — find arrived process with minimum remaining time
            Process current = null;
            for (Process p : processes) {
                if (p.getArrivalTime() <= time && p.getRemainingTime() > 0) {
                    if (current == null
                            || p.getRemainingTime() < current.getRemainingTime()
                            || (p.getRemainingTime() == current.getRemainingTime()
                                && p.getArrivalTime()  < current.getArrivalTime())) {
                        current = p;
                    }
                }
            }

            // CPU is idle — no process has arrived yet
            if (current == null) {
                if (!"IDLE".equals(lastId)) {
                    if (lastId != null) gantt.add(new GanttChart(lastId, sliceStart, time));
                    lastId    = "IDLE";
                    sliceStart = time;
                }
                time++;
                continue;
            }

            // Record first CPU usage for this process
            if (current.getStartTime() == -1) current.setStartTime(time);

            // Step 4 — detect process switch; open a new Gantt slice
            if (!current.getId().equals(lastId)) {
                if (lastId != null) gantt.add(new GanttChart(lastId, sliceStart, time));
                lastId     = current.getId();
                sliceStart = time;
            }

            // Step 2 — run for exactly one tick
            current.setRemainingTime(current.getRemainingTime() - 1);
            time++;

            // Step 3 — process just finished
            if (current.getRemainingTime() == 0) {
                current.setCompletionTime(time);
                current.setTurnaroundTime(current.getCompletionTime() - current.getArrivalTime());
                current.setWaitingTime(current.getTurnaroundTime() - current.getBurstTime());
                completed++;
            }
        }

        // Close the final open Gantt slice
        if (lastId != null) gantt.add(new GanttChart(lastId, sliceStart, time));

        // Merge back-to-back same-process entries created by tick simulation
        return new SchedulerResult(sortById(processes), mergeB(gantt));
    }


  
    /**
     * Round Robin — each process gets a fixed time slice (quantum).
     * When a process exhausts its quantum without finishing, it is moved
     * to the back of the ready queue and the next process runs.
     * 
     *   1. Maintain a FIFO ready queue.  Seed it with processes whose
     *      arrival time ≤ current time.
     *   2. Dequeue the front process; run it for min(quantum, remainingTime).
     *   3. After the slice completes, enqueue any processes that arrived
     *      DURING the slice (before re-queuing the current process).
     *   4. If the process is unfinished, append it to the back of the queue.
     *   5. Repeat until all processes are done.
     */
    public static SchedulerResult roundRobin(List<Process> original, int quantum) {
        List<Process>    processes   = newProcess(original);
        List<GanttChart> gantt       = new ArrayList<>();

        // notArrived holds processes sorted by arrival — we pull from it as time advances
        List<Process>    notArrived  = new ArrayList<>(processes);
        notArrived.sort(Comparator.comparingInt((Process p) -> p.getArrivalTime())
                                  .thenComparing(p -> p.getId()));

        Queue<Process> readyQueue = new LinkedList<>();
        int time      = 0;
        int completed = 0;
        int n         = processes.size();

        // Seed the queue with processes already available at t=0
        drainArrivals(readyQueue, notArrived, time);

        while (completed < n) {

            if (readyQueue.isEmpty()) {
                // CPU is idle — jump to the next process arrival
                if (notArrived.isEmpty()) break;
                int next = notArrived.get(0).getArrivalTime();
                gantt.add(new GanttChart("IDLE", time, next));
                time = next;
                drainArrivals(readyQueue, notArrived, time);
                continue;
            }

            // Step 2 — dequeue next process and run for up to `quantum` ticks
            Process p    = readyQueue.poll();
            if (p.getStartTime() == -1) p.setStartTime(time);

            int execTime = Math.min(quantum, p.getRemainingTime());
            gantt.add(new GanttChart(p.getId(), time, time + execTime));
            time           += execTime;
            p.setRemainingTime(p.getRemainingTime() - execTime);

            // Step 3 — enqueue processes that arrived during this slice
            drainArrivals(readyQueue, notArrived, time);

            // Step 4 — process finished or gets re-queued
            if (p.getRemainingTime() == 0) {
                p.setCompletionTime(time);
                p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime()); 
                p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
                completed++;
            } else {
                readyQueue.add(p); // Unfinished — goes to the back of the queue
            }
        }

        return new SchedulerResult(sortById(processes), gantt);
    }


    /**
     * Priority (Non-Preemptive) — at each dispatch point pick the ARRIVED
     * process with the LOWEST priority number (= highest urgency); run it
     * to completion without interruption.
     * 
     *   1. At current time, collect all arrived processes.
     *   2. Pick the one with the smallest priority value.
     *   3. Run to completion; compute metrics.
     *   4. Repeat until all processes are done.
     */
    public static SchedulerResult priorityNP(List<Process> original) {
        List<Process>    processes = newProcess(original);
        List<GanttChart> gantt     = new ArrayList<>();
        List<Process>    remaining = new ArrayList<>(processes);
        List<Process>    done      = new ArrayList<>();

        int time = 0;

        while (!remaining.isEmpty()) {
            // Step 1 — collect arrived processes
            List<Process> ready = new ArrayList<>();
            for (Process p : remaining)
                if (p.getArrivalTime() <= time) ready.add(p);

            if (ready.isEmpty()) {
                int next = remaining.stream().mapToInt(p -> p.getArrivalTime()).min().orElse(time + 1);
                gantt.add(new GanttChart("IDLE", time, next));
                time = next;
                continue;
            }

            // Step 2 — pick highest-priority (lowest number); tie-break by arrival, then ID
            ready.sort(Comparator.comparingInt((Process p) -> p.getPriority())
                                 .thenComparingInt(p -> p.getArrivalTime())
                                 .thenComparing(p -> p.getId()));
            Process p = ready.get(0);
            remaining.remove(p);

            // Step 3 — run to completion
            p.setStartTime(time);
            gantt.add(new GanttChart(p.getId(), time, time + p.getBurstTime()));
            time += p.getBurstTime();

            p.setCompletionTime(time);
            p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
            p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
            done.add(p);
        }

        return new SchedulerResult(done, gantt);
    }

    /**
     * Priority (Preemptive) — at EVERY clock tick run the arrived process with
     * the LOWEST priority number.  A newly arrived higher-priority process
     * immediately preempts the currently running one.
     * 
     *   1. Each tick: find the arrived process with minimum priority value.
     *   2. Run it for one tick.
     *   3. Track process switches for the Gantt chart.
     *   4. When a process finishes, compute its metrics.
     */
    public static SchedulerResult priorityP(List<Process> original) {
        List<Process>    processes = newProcess(original);
        List<GanttChart> gantt     = new ArrayList<>();

        int n         = processes.size();
        int completed = 0;
        int time      = 0;
        int totalBurst = processes.stream().mapToInt(p -> p.getBurstTime()).sum();
        int maxTime    = processes.stream().mapToInt(p -> p.getArrivalTime()).max().orElse(0)
                         + totalBurst + 1;

        String lastId    = null;
        int    sliceStart = 0;

        while (completed < n && time <= maxTime) {

            // Step 1 — find arrived process with the highest priority (lowest number)
            Process current = null;
            for (Process p : processes) {
                if (p.getArrivalTime() <= time && p.getRemainingTime() > 0) {
                    if (current == null
                            || p.getPriority() < current.getPriority()
                            || (p.getPriority() == current.getPriority()
                                && p.getArrivalTime() < current.getArrivalTime())) {
                        current = p;
                    }
                }
            }

            if (current == null) {
                // CPU idle
                if (!"IDLE".equals(lastId)) {
                    if (lastId != null) gantt.add(new GanttChart(lastId, sliceStart, time));
                    lastId = "IDLE"; sliceStart = time;
                }
                time++;
                continue;
            }

            if (current.getStartTime() == -1) current.setStartTime(time);

            // Step 3 — open a new Gantt slice when process changes
            if (!current.getId().equals(lastId)) {
                if (lastId != null) gantt.add(new GanttChart(lastId, sliceStart, time));
                lastId = current.getId(); sliceStart = time;
            }

            // Step 2 — run for one tick
            current.setRemainingTime(current.getRemainingTime() - 1);
            time++;

            // Step 4 — process just finished
            if (current.getRemainingTime() == 0) {
                current.setCompletionTime(time);
                current.setTurnaroundTime(current.getCompletionTime() - current.getArrivalTime());
                current.setWaitingTime(current.getTurnaroundTime() - current.getBurstTime());
                completed++;
            }
        }

        if (lastId != null) gantt.add(new GanttChart(lastId, sliceStart, time));
        return new SchedulerResult(sortById(processes), mergeB(gantt));
    }

    /**
     * Priority with Round Robin — extends pure Priority scheduling by applying
     * Round Robin time-sharing among processes that share the SAME priority level.
     * 
     *   1. Group processes by priority value using a sorted TreeMap
     *      (natural ascending order → lower value = higher-priority group runs first).
     *   2. For each priority group, apply Round Robin with the given quantum.
     *      The entire group must complete before the next lower-priority group starts.
     *   3. Within each group, newly arrived processes join the ready queue as the
     *      clock advances (mirrors standard Round Robin behaviour).
     */
    public static SchedulerResult priorityRR(List<Process> original, int quantum) {
        List<Process>    processes = newProcess(original);
        List<GanttChart> gantt     = new ArrayList<>();
        List<Process>    done      = new ArrayList<>();

        // Step 1 — group processes by priority; TreeMap keeps keys in ascending order
        Map<Integer, List<Process>> groups = new TreeMap<>();
        for (Process p : processes)
            groups.computeIfAbsent(p.getPriority(), k -> new ArrayList<>()).add(p);

        int time = 0;

        // Step 2 — process each priority group in ascending priority order
        for (Map.Entry<Integer, List<Process>> entry : groups.entrySet()) {
            List<Process> group = new ArrayList<>(entry.getValue());
            group.sort(Comparator.comparingInt((Process p) -> p.getArrivalTime())
                                 .thenComparing(p -> p.getId()));

            // Step 3 — apply Round Robin within this group
            Queue<Process> queue      = new LinkedList<>();
            List<Process>  notArrived = new ArrayList<>(group);
            drainArrivals(queue, notArrived, time);

            // If nothing arrived yet, jump the clock to the group's first arrival
            if (queue.isEmpty() && !notArrived.isEmpty()) {
                int next = notArrived.get(0).getArrivalTime();
                if (next > time) { gantt.add(new GanttChart("IDLE", time, next)); time = next; }
                drainArrivals(queue, notArrived, time);
            }

            int groupDone = 0;

            while (groupDone < group.size()) {
                if (queue.isEmpty()) {
                    if (notArrived.isEmpty()) break;
                    int next = notArrived.get(0).getArrivalTime();
                    gantt.add(new GanttChart("IDLE", time, next));
                    time = next;
                    drainArrivals(queue, notArrived, time);
                    continue;
                }

                Process p = queue.poll();
                if (p.getStartTime() == -1) p.setStartTime(time);

                int execTime = Math.min(quantum, p.getRemainingTime());
                gantt.add(new GanttChart(p.getId(), time, time + execTime));
                time           += execTime;
                p.setRemainingTime(p.getRemainingTime() - execTime);
                drainArrivals(queue, notArrived, time);

                if (p.getRemainingTime() == 0) {
                    p.setCompletionTime(time); 
                    p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
                    p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
                    done.add(p);
                    groupDone++;
                } else {
                    queue.add(p); // Back of the queue for next slice
                }
            }
        }

        return new SchedulerResult(done, gantt);
    }

    /**
     * newProcess() — creates fresh Process copies from the original list so
     * algorithms never mutate the user's input data.  Each copy is reset()
     * to clear any previously computed metrics.

     */
    private static List<Process> newProcess(List<Process> original) {
        List<Process> copy = new ArrayList<>();
        for (Process p : original) {
            Process np = new Process(p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getPriority());
            np.reset();
            copy.add(np);
        }
        return copy;
    }

    /**
     * sortById() — returns a new list sorted lexicographically by process ID
     * (P1 < P2 < … < P10) so the results table is always presented in a
     * consistent, reader-friendly order regardless of completion order.
     */
    private static List<Process> sortById(List<Process> processes) {
        List<Process> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparing(p -> p.getId()));
        return sorted;
    }

    /**
     * mergeB() — collapses consecutive GanttEntry blocks that share the
     * same processId into a single block.  This is necessary because tick-by-
     * tick simulations (SRT, Priority-P) produce one entry per tick, but the
     * Gantt chart display should show one contiguous bar per uninterrupted run.
     *
     * Example:
     *   [P1:0-1][P1:1-2][P2:2-3][P1:3-4]
     *   →  [P1:0-2][P2:2-3][P1:3-4]
     */
    private static List<GanttChart> mergeB(List<GanttChart> gantt) {
        if (gantt.isEmpty()) return gantt;

        List<GanttChart> merged  = new ArrayList<>();
        GanttChart       current = gantt.get(0);

        for (int i = 1; i < gantt.size(); i++) {
            GanttChart next = gantt.get(i);
            // Merge if same process AND contiguous time (no gap between blocks)
            if (next.processId.equals(current.processId) && next.start == current.end) {
                current = new GanttChart(current.processId, current.start, next.end);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }

    /**
     * drainArrivals() — moves processes from the notArrived list into the
     * ready queue when their arrival time ≤ current clock time.  Used by
     * Round Robin and Priority+RR to add new processes mid-simulation.
     *
     * Precondition: notArrived is sorted by arrivalTime ascending.
     */
    private static void drainArrivals(Queue<Process>  queue,
                                       List<Process>   notArrived,
                                       int             time) {
        Iterator<Process> it = notArrived.iterator();
        while (it.hasNext()) {
            Process p = it.next();
            if (p.getArrivalTime() <= time) { queue.add(p); it.remove(); }
            else break; // List is sorted — no need to scan further
        }
    }
}