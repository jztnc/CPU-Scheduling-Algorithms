class Process{
    //These are the input fields provide by the user
    private String id; //For process identifier lable like P1
    
    private int arrivalTime, // When the process enters the ready queue 
        burstTime, //Total CPU Time the process requires
        priority; // lower value = higher priority
    
    private int remainingTime, // CPU time needed decremented each tick
        completionTime, // Clock thick when the process finish
        waitingTime, // Waiting time = Turnaround Time - Burst time
         turnaroundTime, // Turnaround Time = Completion Time - Arrival Time
        startTime = -1; // First tick the process actually uses the CPU( -1 = not started)


    public Process(String id, int arrivalTime, int burstTime, int priority) {
        this.id            = id;
        this.arrivalTime   = arrivalTime;
        this.burstTime     = burstTime;
        this.priority      = priority;
        this.remainingTime = burstTime; // Starts equal to burstTime; preemptive algos decrement this
    }

    //It reset all the computed fields back to zero so the same process can be use to other algorithms
    public void reset() {
        remainingTime  = burstTime; // Full burst again
        completionTime = 0;
        turnaroundTime = 0;
        waitingTime    = 0;
        startTime      = -1;        // -1 signals "not yet started"
    }

    //These methods expose the process’s data to other classes (like Scheduler or OutputPrinter)
    //without directly accessing the fields.

    public String getId()            { return id; }
    public int    getArrivalTime()   { return arrivalTime; }
    public int    getBurstTime()     { return burstTime; }
    public int    getPriority()      { return priority; }
    public int    getRemainingTime() { return remainingTime; }
    public int    getCompletionTime(){ return completionTime; }
    public int    getTurnaroundTime(){ return turnaroundTime; }
    public int    getWaitingTime()   { return waitingTime; }
    public int    getStartTime()     { return startTime; }
    
    //These allow the scheduler to update the process’s computed values as the simulation runs.
    public void setRemainingTime(int remainingTime)   { this.remainingTime  = remainingTime; }
    public void setCompletionTime(int completionTime) { this.completionTime = completionTime; }
    public void setTurnaroundTime(int turnaroundTime) { this.turnaroundTime = turnaroundTime; }
    public void setWaitingTime(int waitingTime)       { this.waitingTime    = waitingTime; }
    public void setStartTime(int startTime)           { this.startTime      = startTime; }

     @Override
    public String toString() {
        return id;
    }

}