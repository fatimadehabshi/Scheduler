import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class MainThread implements Runnable {
    public Scanner scanner;
    public FileWriter writer;
    public int taskNum = 0;
    public static int cycle = 1;
    public static int quantum = 2;
    public static int runPro = 0;
    public static List<List<Task>> waitingQueue = new ArrayList<>();
    public static List<List<Task>> readyQueue = new ArrayList<>();
    public static List<Resource> resources = new ArrayList<>(3);
    public List<Processor> processors = new ArrayList<>(4);

    public MainThread(Scanner scanner, FileWriter writer) {
        this.scanner = scanner;
        this.writer = writer;
    }

    @Override
    public void run() {
        try {
            synchronized (MainThread.class) {
                for (int i = 0; i < 3; i++) {
                    List<Task> row = new ArrayList<>();
                    readyQueue.add(row);
                }
                for (int i = 0; i < 3; i++) {
                    List<Task> row = new ArrayList<>();
                    waitingQueue.add(row);
                }
                for (int i = 0; i < 3; i++) {
                    int temp = scanner.nextInt();
                    String n = "R";
                    n += String.valueOf(i + 1);
                    resources.add(new Resource(n, temp, temp));
                }
                taskNum = scanner.nextInt();
                for (int i = 0; i < taskNum; i++) {
                    addTask(new Task(scanner.next(), scanner.next(), scanner.nextInt()), readyQueue);
                }
                for (int i = 0; i < 4; i++) {
                    String s = "Core";
                    s += String.valueOf(i + 1);
                    processors.add(new Processor(resources, s));
                }

                //main loop:
                while (taskNum != 0) {
                    runPro = 0;
                    List<Thread> TP = new ArrayList<>();
                    writer.write("Cycle: " + cycle + "\n");

                    //System.out.println("Cycle: " + cycle);
                    for (int i = 0; i < 4; i++) {
                        Processor p = processors.get(i);
                        TP.add(new Thread(p, p.name));
                    }
                    for (int i = 0; i < 4; i++) {
                        TP.get(i).start();
                    }
                    for (int i = 0; i < 4; i++)
                        TP.get(i).join();

                    if (runPro < 4) {
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < readyQueue.get(i).size(); j++) {
                                Task t = readyQueue.get(i).get(j);
                                if (t.state.equals("Waiting")) {
                                    readyQueue.get(i).remove(t);
                                    j--;
                                    addTask(t, waitingQueue);
                                    t.waitingStartTime = cycle;
                                    sortWaiting();
                                }
                            }
                        }
                    }
                    for (int i = 0; i < 4; i++) {
                        Task t = processors.get(i).currentTask;
                        if (t == null) {
                            //System.out.println(processors.get(i).name + " is Idle.");
                            writer.write(processors.get(i).name + " is Idle.\n");
                        } else {
                            if (t.state.equals("Running")) {
                                //System.out.println(t.processor.name + " is running " + t.name + ".");
                                writer.write(t.processor.name + " is running " + t.name + ".\n");
                                if(t.duration == 0){
                                    //System.out.println("from duration = 0 Adding resource of " + t.name);
                                    addResource(t.requirements);
                                    taskNum--;
                                    t.processor.currentTask = null;
                                    t = null;
                                    sortWaiting();
                                    Task t_prime;
                                    do {
                                        t_prime = getFromWaiting();
                                    } while (t_prime != null);
                                }
                            }
                        }
                        if(cycle % quantum == 0){
                            if(t != null) {
                                //System.out.println("from quantum Adding resource of " + t.name);
                                addResource(t.requirements);
                                addTask(t, readyQueue);
                                t.processor.currentTask = null;
                            }
                        }
                    }
                    if(cycle % quantum == 0)
                        compareR_W();
                    cycle++;
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void compareR_W() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < readyQueue.get(i).size(); j++) {
                for (int k = 0; k < waitingQueue.get(i).size(); k++) {
                    Task tR = readyQueue.get(i).get(j);
                    Task tW = waitingQueue.get(i).get(k);
                    if (checkResources(tW)) {
                        if (tR.duration < tW.duration) {
                            waitingQueue.get(i).remove(tW);
                            k--;
                            tW.waitingStartTime = 0;
                            tW.processor = null;
                            tW.state = "New";
                            addTask(tW, readyQueue);
                        }
                    }
                }
            }
        }
    }
    public Task getFromWaiting() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < waitingQueue.get(i).size(); j++) {
                Task t = waitingQueue.get(i).get(j);
                if (t != null)
                    if (0 < resources.get(t.requirements[0]).remainder && 0 < resources.get(t.requirements[1]).remainder) {
                        t.waitingStartTime = 0;
                        t.processor = null;
                        t.state = "New";
                        addTask(t, readyQueue);
                        waitingQueue.get(i).remove(t);
                        return t;
                    }
            }
        }
        return null;
    }
    public void sortWaiting() {
        for (int i = 0; i < 3; i++) {
            waitingQueue.get(i).sort(new Comparator<Task>() {
                @Override
                public int compare(Task t1, Task t2) {
                    int durationComparison = Integer.compare(t2.duration, t1.duration);
                    if (durationComparison == 0) {
                        return Integer.compare((cycle - t2.waitingStartTime), (cycle - t1.waitingStartTime));
                    } else {
                        return durationComparison;
                    }
                }
            });
        }
    }
    class Processor implements Runnable {
        public List<Resource> resources = new ArrayList<>(3);
        String name, state;
        public Task currentTask;

        public Processor(List<Resource> resources, String name) {
            this.resources = resources;
            this.name = name;
            this.currentTask = null;
            this.state = "Ready";
        }

        @Override
        public void run() {
            synchronized (CommonUtil.mLock) {
                try {
                    if (this.currentTask == null)
                        this.currentTask = scheduleRR();

                    if (this.currentTask == null) {
                        this.state = "Idle";
                        CommonUtil.mLock.notify();
                        return;
                    }
                    this.currentTask.duration--;
                    this.state = "Running";
                    runPro++;
                    CommonUtil.mLock.notify();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        public synchronized Task scheduleRR() {
            synchronized (CommonUtil.mLock) {
                for (int i = 0; i < 3; i++) {
                    readyQueue.get(i).sort(new Comparator<Task>() {
                        @Override
                        public int compare(Task t1, Task t2) {
                            return Integer.compare(t2.duration, t1.duration);
                        }
                    });
                }
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < readyQueue.get(i).size(); j++) {
                        Task t = readyQueue.get(i).get(j);
//                        for (int k = 0; k < resources.size(); k++) {
//                            System.out.print(resources.get(k).name + " " + resources.get(k).remainder + " ");
//                        }
//                        System.out.println();
                        if (checkResources(t)) {
                            readyQueue.get(i).remove(t);
                            t.state = "Running";
                            if(cycle % quantum == 1 || t.processor == null) {
                                //System.out.println("Get Resource");
                                getResource(t.requirements);
                            }
                            t.processor = this;
                            CommonUtil.mLock.notify();
                            return t;
                        } else {
                            t.state = "Waiting";
                            t.processor = null;
                        }
                    }
                }
                CommonUtil.mLock.notify();
                return null;
            }
        }
    }

    static class CommonUtil {
        final static Object mLock = new Object();
    }

    public void addTask(Task task, List<List<Task>> queue) {
        switch (task.type) {
            case "X":
                queue.get(2).add(task);
                return;
            case "Y":
                queue.get(1).add(task);
                return;
            case "Z":
                queue.get(0).add(task);
                return;
        }
    }

    public synchronized boolean checkResources(Task t) {
        int[] req = t.requirements;
        return 0 < resources.get(req[0]).remainder && 0 < resources.get(req[1]).remainder;
    }

    public synchronized void getResource(int[] req) {
        resources.get(req[0]).remainder--;
        resources.get(req[1]).remainder--;
    }

    public synchronized void addResource(int[] req) {
        Resource r1 = resources.get(req[0]);
        Resource r2 = resources.get(req[1]);
        if(r1.remainder + 1 <= r1.total && r2.remainder + 1 <= r2.total){
            r1.remainder++;
            r2.remainder++;
        }
    }
}
