
//records WHICH process (or IDLE) occupied the CPU from time `start` to time `end`. 
//Example list: [P1: 0→4]  [P2: 4→7]  [IDLE: 7→9]  [P3: 9→14] 
 
public class GanttChart {

    public String processId; // "P1", "P2", … or "IDLE" when CPU is idle
    public int    start;     // Tick at which this block begins
    public int    end;       // Tick at which this block ends (exclusive)

    public GanttChart(String processId, int start, int end) {
        this.processId = processId;
        this.start     = start;
        this.end       = end;
    }
    public String getProcessId() { return processId; }
    public int    getStart()     { return start; }
    public int    getEnd()       { return end; }

     //readable form used in raw text output.
     // ex "P2[4-7]"
    @Override
    public String toString() {
        return processId + "[" + start + "-" + end + "]";
    }
}