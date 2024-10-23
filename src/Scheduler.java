import java.util.*;

public class Scheduler {
    private Queue<Process> readyQueue = new LinkedList<>();
    private List<Process> allProcesses = new ArrayList<>();
    private final int quantum;
    private final MemoryManager memoryManager;
    private int currentTime = 0;

    public Scheduler(int quantum, MemoryManager memoryManager) {
        this.quantum = quantum;
        this.memoryManager = memoryManager;
    }

    // Add a process to the scheduler
    public void addProcess(Process process) {
        readyQueue.offer(process);
        allProcesses.add(process);
    }

    // Main run method to start scheduling
    public void run() {
        // Initial page faults at t=0 for all processes
        for (Process process : allProcesses) {
            if (process.hasMorePages()) {
                int page = process.nextPage();
                boolean fault = memoryManager.accessPage(process.getId(), page, currentTime);
                if (fault) {
                    process.addFaultTime(currentTime);
                    process.blockForIO();
                    readyQueue.add(process); // Add to ready queue for further execution
                   // System.out.println("DEBUG: Initial page fault for " + process.getName() + " at t=0");
                }
            }
        }

        // Start actual processing from t=1
        currentTime = 1;

        // Main execution loop
        while (!readyQueue.isEmpty()) {
            Process current = readyQueue.poll();
            int timeSpent = 0;

            // Set start time when the process starts execution
            if (current.getStartTime() == -1 || current.getStartTime() == 0) {
                current.setStartTime(currentTime);
               // System.out.println("DEBUG: Process " + current.getName() + " starts execution at t=" + currentTime);
            }

            // Execute pages within the given quantum
            while (timeSpent < quantum && current.hasMorePages()) {
                // Handle blocked processes
                if (current.isBlocked()) {
                    current.decrementIoTime();
                    if (!current.isBlocked()) {
                        readyQueue.add(current); // Re-add to ready queue when unblocked
                       // System.out.println("DEBUG: Process " + current.getName() + " unblocked at t=" + currentTime);
                    }
                    currentTime++;
                    break; // Move to next process
                }

                // Access the next page
                int page = current.nextPage();
                boolean fault = memoryManager.accessPage(current.getId(), page, currentTime);

                if (fault) {
                    current.addFaultTime(currentTime);
                    current.blockForIO();
                    //System.out.println("DEBUG: Page fault for " + current.getName() + " at t=" + currentTime);
                    currentTime++;
                    break; // Move to next process after fault
                }

                // Execute page
                timeSpent++;
                currentTime++;
               // System.out.println("DEBUG: Process " + current.getName() + " executing page at t=" + currentTime);
            }

            // Check if process has completed all its pages
            if (!current.hasMorePages()) {
                if (current.getEndTime() == 0) {
                    current.setEndTime(currentTime);
                   // System.out.println("DEBUG: Process " + current.getName() + " finishes at t=" + currentTime);
                }
            } else {
                readyQueue.add(current); // Re-add if more pages are left
            }
        }

        // Display results after completion
        displayResults();
    }

    // Display the results for all processes
    private void displayResults() {
        System.out.println("LRU - " + (memoryManager.isVariable() ? "Variable-Global Replacement:" : "Fixed-Local Replacement:"));
        System.out.printf("%-4s %-15s %-18s %-9s %s\n", "PID", "Process Name", "Turnaround Time", "# Faults", "Fault Times");

        for (Process process : allProcesses) {
            System.out.printf("%-4d %-15s %-18d %-9d %s\n",
                    process.getId(),
                    process.getName().replace(".txt", ""),
                    process.getTurnaroundTime(),
                    process.getTotalFaults(),
                    process.getFaultTimes().toString().replace("[", "{").replace("]", "}")
            );
        }

        System.out.println("------------------------------------------------------------");
    }
}
