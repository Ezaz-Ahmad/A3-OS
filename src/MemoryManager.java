// Author: Ezaz Ahamd
// ID: C3412618
// This class manages memory allocation and page replacement strategies for processes
import java.util.*;

public class MemoryManager {
    private int[] frames; // Array to hold pages in memory frames
    private Map<Integer, Integer> pageTable; // Maps pages to frames for quick lookup
    private Map<Integer, LinkedList<Integer>> lruQueues; // LRU queues for each process (for fixed-local)
    private LinkedList<Integer> globalLruQueue; // Global LRU queue for variable-global
    private boolean isVariable;
    private int totalFrames;
    private int numProcesses;
    private Map<Integer, List<Integer>> faultTimes; // Tracks fault times for each process

    public MemoryManager(int totalFrames, boolean isVariable, int numProcesses) {
        this.totalFrames = totalFrames;
        this.isVariable = isVariable;
        this.numProcesses = numProcesses;
        this.frames = new int[totalFrames];
        Arrays.fill(frames, -1); // Initialize frames to -1 (empty)
        this.pageTable = new HashMap<>();
        this.lruQueues = new HashMap<>();
        this.faultTimes = new HashMap<>(); // Initialize faultTimes map
        this.globalLruQueue = new LinkedList<>();

        // Initialize LRU queues and fault times for each process
        for (int i = 0; i < numProcesses; i++) {
            lruQueues.put(i, new LinkedList<>()); // Create a new LRU queue for each process
            faultTimes.put(i, new ArrayList<>()); // Create a list to store fault times for each process
        }
    }


    private void recordPageFault(int processId, int currentTime) {
        // Ensure the fault times list is initialized for the process
        faultTimes.computeIfAbsent(processId, k -> new ArrayList<>());
        faultTimes.get(processId).add(currentTime); // Add the current time to the fault times
    }


    public boolean accessPage(int processId, int page, int currentTime) {
        // Ensure the LRU queue exists for the process
        lruQueues.computeIfAbsent(processId, k -> new LinkedList<>()); // Initialize if absent

        LinkedList<Integer> lruQueue = lruQueues.get(processId);

        // Check if the page is already in memory
        if (pageTable.containsKey(page)) {
            // Update the LRU queue since the page is accessed again
            if (isVariable) {
                globalLruQueue.remove(Integer.valueOf(page));
                globalLruQueue.addFirst(page);
            } else {
                lruQueue.remove(Integer.valueOf(page));
                lruQueue.addFirst(page);
            }
            return false; // No page fault
        }

        // Page fault occurs
        recordPageFault(processId, currentTime);

        // Page replacement logic
        if (isVariable) {
            replacePageGlobal(page);
        } else {
            replacePageLocal(processId, page);
        }

        return true; // Page fault occurred
    }




    private boolean handlePageFault(int pageNumber, int processId) {
        return isVariable ? handleVariableGlobalReplacement(pageNumber) : handleFixedLocalReplacement(pageNumber, processId);
    }

    private boolean handleVariableGlobalReplacement(int pageNumber) {
        if (globalLruQueue.size() < totalFrames) {
            return addToMemory(pageNumber);
        } else {
            int leastUsedFrame = globalLruQueue.poll();
            return replaceInMemory(pageNumber, leastUsedFrame);
        }
    }

    private boolean handleFixedLocalReplacement(int pageNumber, int processId) {
        LinkedList<Integer> queue = lruQueues.get(processId);
        int framesPerProcess = totalFrames / numProcesses;

        if (queue.size() < framesPerProcess) {
            return addToMemory(pageNumber);
        } else {
            int leastUsedFrame = queue.poll();
            return replaceInMemory(pageNumber, leastUsedFrame);
        }
    }

    private boolean addToMemory(int pageNumber) {
        int freeFrame = findFreeFrame();
        frames[freeFrame] = pageNumber;
        pageTable.put(pageNumber, freeFrame);
        if (isVariable) {
            globalLruQueue.addLast(freeFrame);
        } else {
            lruQueues.values().forEach(queue -> queue.addLast(freeFrame));
        }
        return true;
    }
    private void replacePageGlobal(int page) {
        if (globalLruQueue.size() < totalFrames) {
            // Add the page if there is available space
            globalLruQueue.addFirst(page);
        } else {
            // Replace the least recently used page
            int lruPage = globalLruQueue.removeLast();
            pageTable.remove(lruPage);
            globalLruQueue.addFirst(page);
        }
        // Update the page table
        pageTable.put(page, 1); // 1 indicates it's in memory
    }

    private void replacePageLocal(int processId, int page) {
        // Ensure the LRU queue exists for the process
        lruQueues.computeIfAbsent(processId, k -> new LinkedList<>());

        LinkedList<Integer> lruQueue = lruQueues.get(processId);

        if (lruQueue.size() < totalFrames / numProcesses) {
            // Add the page if there is space for the process
            lruQueue.addFirst(page);
        } else {
            // Replace the least recently used page for the process
            int lruPage = lruQueue.removeLast();
            pageTable.remove(lruPage);
            lruQueue.addFirst(page);
        }
        // Update the page table
        pageTable.put(page, processId);
    }



    private boolean replaceInMemory(int pageNumber, int frameIndex) {
        int oldPage = frames[frameIndex];
        frames[frameIndex] = pageNumber;
        pageTable.remove(oldPage);
        pageTable.put(pageNumber, frameIndex);
        if (isVariable) {
            globalLruQueue.addLast(frameIndex);
        } else {
            lruQueues.values().forEach(queue -> queue.addLast(frameIndex));
        }
        return true;
    }

    private void refreshGlobalLRU(int frameIndex) {
        globalLruQueue.remove(Integer.valueOf(frameIndex));
        globalLruQueue.addLast(frameIndex);
    }

    private void refreshFixedLRU(int frameIndex, int processId) {
        LinkedList<Integer> queue = lruQueues.get(processId);
        if (queue != null) {
            queue.remove(Integer.valueOf(frameIndex));
            queue.addLast(frameIndex);
        }
    }

    private int findFreeFrame() {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] == -1) {
                return i;
            }
        }
        return -1; // Should not happen as we check the size before calling this method
    }

    public void setIsVariable(boolean isVariable) {
        this.isVariable = isVariable;
    }

    public boolean isVariable() {
        return this.isVariable;
    }
}
