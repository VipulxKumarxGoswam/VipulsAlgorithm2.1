import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

class VipulThread {

    int id;
    int priority;
    long arrivalTime;
    int predictedExecution;

    public VipulThread(int id, int priority, int predictedExecution) {
        this.id = id;
        this.priority = priority;
        this.predictedExecution = predictedExecution;
        this.arrivalTime = System.currentTimeMillis();
    }
}

class VipulScheduler {

    PriorityQueue<VipulThread> queue;

    Semaphore semaphore;
    ReentrantLock mutex = new ReentrantLock();

    int k = 10; // aging control parameter

    public VipulScheduler(int contenders) {

        semaphore = new Semaphore(contenders);

        queue = new PriorityQueue<>((a, b) -> {
            double scoreA = computeScore(a);
            double scoreB = computeScore(b);
            return Double.compare(scoreB, scoreA); // max score first
        });
    }

    // improved score calculation
    double computeScore(VipulThread t) {

        long waiting = System.currentTimeMillis() - t.arrivalTime;

        double aging = Math.log(1 + (double) waiting / k);

        return t.priority + aging - t.predictedExecution;
    }

    // thread arrival
    public void addThread(VipulThread t) {

        queue.offer(t);
    }

    // select best thread
    VipulThread selectThread() {

        return queue.poll();
    }

    // run critical section
    public void execute() throws InterruptedException {

        VipulThread t = selectThread();

        if (t == null)
            return;

        semaphore.acquire();

        mutex.lock();

        try {

            System.out.println("Thread " + t.id + " entering critical section");

            Thread.sleep(t.predictedExecution * 100);

            System.out.println("Thread " + t.id + " leaving critical section");

        } finally {

            mutex.unlock();
            semaphore.release();
        }
    }
}

public class VipulsAlgorithm2 {

    public static void main(String[] args) throws Exception {

        VipulScheduler scheduler = new VipulScheduler(3);

        scheduler.addThread(new VipulThread(1, 5, 2));
        scheduler.addThread(new VipulThread(2, 4, 3));
        scheduler.addThread(new VipulThread(3, 6, 1));
        scheduler.addThread(new VipulThread(4, 3, 10));
        scheduler.addThread(new VipulThread(5, 7, 2));

        while (true) {

            scheduler.execute();
            Thread.sleep(500);
        }
    }
}