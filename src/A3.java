// Author: Ezaz Ahamd
// ID: C3412618
// This class serves as the entry point for the program, handling input and output for memory management.
import java.io.IOException;

public class A3 {
    public static void main(String[] args) {
        // Check if the correct number of arguments are provided
        if (args.length < 3) {
            System.out.println("Usage: java A3 <frames> <quantum> <process file 1> ... <process file n>");
            System.exit(1);
        }

        // Parsing frames and quantum from command line arguments
        int frames = Integer.parseInt(args[0]);
        int quantum = Integer.parseInt(args[1]);

        // Number of process files would be args.length - 2 (excluding frames and quantum)
        int numProcesses = args.length - 2;

        // Initialize MemoryManager and Scheduler for fixed and variable strategies
        MemoryManager memoryManagerFixed = new MemoryManager(frames, false, numProcesses);
        MemoryManager memoryManagerVariable = new MemoryManager(frames, true, numProcesses);
        Scheduler schedulerFixed = new Scheduler(quantum, memoryManagerFixed);
        Scheduler schedulerVariable = new Scheduler(quantum, memoryManagerVariable);

        // Load processes from provided files and add them to both schedulers
        for (int i = 2; i < args.length; i++) {
            try {
                Process processFixed = Process.loadFromFile(args[i]);  // Load the process for the fixed scheduler
                Process processVariable = processFixed.clone();        // Clone the process for the variable scheduler
                schedulerFixed.addProcess(processFixed);
                schedulerVariable.addProcess(processVariable);
            } catch (IOException e) {
                System.err.println("Error loading file: " + args[i]);
                e.printStackTrace();
                return; // Exit if any file fails to load
            }
        }

        // Run the scheduler and print results for fixed allocation
        System.out.println("Running with Fixed-Local Replacement Strategy:");
        schedulerFixed.run();
       // schedulerFixed.printResults();

        // Run the scheduler and print results for variable allocation
        System.out.println("Running with Variable-Global Replacement Strategy:");
        schedulerVariable.run();
        //schedulerVariable.printResults();
    }
}
