// Author: Ezaz Ahamd
// ID: C3412618
// This class represents a process that manages its pages and records page faults.
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Process implements Cloneable {
    private static int nextId = 1;
    private int id;
    private String name;
    private Queue<Integer> pages = new LinkedList<>();
    private LinkedList<Integer> faultTimes = new LinkedList<>();
    private int totalFaults = 0;
    private int startTime = 0;
    private int endTime = 0;
    private int ioTimeRemaining = 0;

    public Process(String name) {
        this.id = nextId++;
        this.name = name;
    }

    public static Process loadFromFile(String filename) throws FileNotFoundException {
        Process process = new Process(filename);
        Scanner scanner = new Scanner(new File(filename));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("page:")) {
                String number = line.substring(5).trim();
                number = number.replaceAll("[^0-9]", "");
                if (!number.isEmpty()) {
                    int pageId = Integer.parseInt(number);
                    process.pages.add(pageId);
                }
            }
        }
        scanner.close();
        return process;
    }

    @Override
    public Process clone() {
        try {
            Process cloned = (Process) super.clone();
            cloned.pages = new LinkedList<>(this.pages);  // Clone the pages queue
            cloned.faultTimes = new LinkedList<>(this.faultTimes);  // Clone the fault times
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer requestPage() {
        if (hasMorePages()) {
            Integer page = pages.poll();
            return page;
        }
        return null; // No more pages to request
    }

    public boolean hasMorePages() {
        return !pages.isEmpty();
    }
    public int nextPage() {
        if (!pages.isEmpty()) {
            return pages.poll();
        }
        return -1; // No more pages to request
    }

    public void recordFault(int currentTime) {
        totalFaults++;
        faultTimes.add(currentTime);
        ioTimeRemaining = 4; // Assuming I/O operations take 4 time units
        // Uncomment below if needed for debugging
        // System.out.println("Fault recorded for process " + id + " at time " + currentTime);
    }
    public void addFaultTime(int currentTime) {
        totalFaults++;
        faultTimes.add(currentTime);
        ioTimeRemaining = 4; // Set I/O time after fault
    }
    public void blockForIO() {
        ioTimeRemaining = 4; // Assume I/O operation takes 4 time units
    }


    public void decrementIoTime() {
        if (ioTimeRemaining > 0) {
            ioTimeRemaining--;
        }
    }


    public boolean isBlocked() {
        return ioTimeRemaining > 0;
    }

    public void setStartTime(int time) {
        startTime = time;
    }

    public void setEndTime(int time) {
        endTime = time;
    }

    public int getTotalFaults() {
        return totalFaults;
    }

    public LinkedList<Integer> getFaultTimes() {
        return faultTimes;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public int getTurnaroundTime() {
        return endTime - startTime;
    }
}
