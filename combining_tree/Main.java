package combining_tree;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void testPerformance(Runnable task, String taskName, int threadNum) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        long start = System.nanoTime();
        for (int i = 0; i < threadNum; ++i) {
            executor.submit(task);
        }
        executor.shutdown();
        if (executor.awaitTermination(20, TimeUnit.SECONDS)) {
            long end = System.nanoTime();
            double duration = (double) (end - start) / 1000; // us
            System.out.format("%s Finished.\n", taskName);
            System.out.format("Duration: %s us\n\n", duration);
        } else {
            System.out.format("%s timeout!", taskName);
        }
    }

    public static void main(String[] args) throws Exception {
        int threadNum = 10;
        int eachRepeat = 1;
        AtomicInteger ai = new AtomicInteger(0);
        CombiningTree ct2 = new CombiningTree(threadNum, 2);
        CombiningTree ct3 = new CombiningTree(threadNum, 3);
        testPerformance(() -> {
            for (int i = 0; i < eachRepeat; ++i) {
                ai.getAndIncrement();
            }
        }, "AtomicInteger", threadNum);
        testPerformance(() -> {
            try {
                for (int i = 0; i < eachRepeat; ++i) {
                    ct2.getAndIncrement();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Binary Combining Tree", threadNum);
        testPerformance(() -> {
            try {
                for (int i = 0; i < eachRepeat; ++i) {
                    ct3.getAndIncrement();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Trinary Combining Tree", threadNum);
        boolean flag = ai.get() == ct2.get() && ai.get() == ct3.get();
        if (flag) {
            System.out.println("Yes! Results are correct!");
        } else {
            System.out.println("No! Results are incorrect!");
        }
    }
}
