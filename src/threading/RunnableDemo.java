
package threading;

// 21.11.2026 - AKSHITA KAUSHIK

public class RunnableDemo implements Runnable {

    @Override
    public void run() {
        System.out.println("Processing resource booking...");
    }

    public static void main(String[] args) {

        RunnableDemo task = new RunnableDemo();

        Thread t1 = new Thread(task);

        t1.start();

        System.out.println("Main thread is running...");
    }
}
