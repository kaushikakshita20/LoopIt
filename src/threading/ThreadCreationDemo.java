package threading;

// 21.11.2026 - AKSHITA KAUSHIK

public class ThreadCreationDemo extends Thread {

    @Override
    public void run() {
        System.out.println("Booking request is being processed...");
    }

    public static void main(String[] args) {

        ThreadCreationDemo t1 = new ThreadCreationDemo();

        t1.start();

        System.out.println("Main thread is running...");
    }
}
