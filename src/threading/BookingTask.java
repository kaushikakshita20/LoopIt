// 23.11.2026- AKSHITA KAUSHIK
package threading;
import model.User;
import service.BookingService;
public class BookingTask implements Runnable {
    private final BookingService bookingService;
    private final User borrower;
    private final int resourceId;
    public BookingTask(BookingService bookingService,
                       User borrower,
                       int resourceId) {
        this.bookingService = bookingService;
        this.borrower = borrower;
        this.resourceId = resourceId;
    }
    @Override
    public void run() {
        bookingService.processBorrowRequest(borrower, resourceId);
    }
}