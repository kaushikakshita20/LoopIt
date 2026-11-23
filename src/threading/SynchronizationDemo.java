
package threading;
import model.Resource;
public class SynchronizationDemo {
    public synchronized void borrowResource(Resource resource, int borrowerId) {
        System.out.println(Thread.currentThread().getName() + " is trying to borrow " + resource.getName());
        if (resource.isAvailable()) {
            resource.setAvailable(false);
            System.out.println("User " + borrowerId + " successfully borrowed " + resource.getName());
        } else {
            System.out.println(
                    "Sorry! Resource is already borrowed."
            );
        }
    }
    public static void main(String[] args) {
        Resource resource = new Resource(
                101,
                "Scientific Calculator",
                "Electronics",
                "Casio Calculator",
                1,
                true
        );
        SynchronizationDemo demo = new SynchronizationDemo();
        Thread user1 = new Thread(() -> {demo.borrowResource(resource, 2);
            }, "User A");
        Thread user2 = new Thread(() -> {
            demo.borrowResource(resource, 3);}, "User B");
        user1.start();
        user2.start();
    }
}
