//Ishita
package threading;

import model.Booking;
import model.User;
import service.EarningsService;

import java.util.List;

public class EarningsTask implements Runnable {

    private EarningsService earningsService;
    private User lender;
    private List<Booking> bookings;

    public EarningsTask(EarningsService earningsService,
                        User lender,
                        List<Booking> bookings) {

        this.earningsService = earningsService;
        this.lender = lender;
        this.bookings = bookings;
    }

    @Override
    public void run() {

        System.out.println(
                Thread.currentThread().getName()
                        + " is generating earnings report..."
        );

        earningsService.printEarningsReport(lender, bookings);
    }
}
