package chapter5;

/**
 * 演示：非 final 锁如何导致数据错乱
 */
public class UnsafeLockDemo {
    private Object lock = new Object(); // 非 final！
    private long count = 0;

    public void increment() {
        synchronized (lock) {
            count++; // 非原子操作！
        }
    }

    public void changeLock() {
        lock = new Object(); // 模拟运行时修改
    }

    public static void main(String[] args) throws InterruptedException {
        UnsafeLockDemo demo = new UnsafeLockDemo();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10_000; i++) demo.increment();
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10_000; i++) {
                demo.changeLock(); // 👈 关键：中途换锁！
                demo.increment();
            }
        });

        t1.start(); t2.start();
        t1.join(); t2.join();

        System.out.println("Final count: " + demo.count);
        // 理论应为 20000，实际 ≈ 15000~19999（丢失更新！）
    }
}
