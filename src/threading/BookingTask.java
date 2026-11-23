//23.11.2026- AKSHITA KAUSHIK
package threading;
import model.Resource;
public class BookingTask implements Runnable {
    private Resource resource;
    private int borrowerId;
    public BookingTask(Resource resource, int borrowerId) {
        this.resource = resource;
        this.borrowerId = borrowerId;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " is requesting resource: " + resource.getName());
        System.out.println("Borrower ID: " + borrowerId);
        System.out.println(Thread.currentThread().getName() + " completed booking request.");
    }
}
