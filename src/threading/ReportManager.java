//Ishita
package threading;

import model.Booking;
import model.User;
import service.EarningsService;

import java.util.List;

public class ReportManager {

    public void generateEarningsReport(User lender,
                                       List<Booking> bookings) {

        EarningsService earningsService = new EarningsService();

        EarningsTask task = new EarningsTask(
                earningsService,
                lender,
                bookings
        );

        Thread reportThread = new Thread(task);

        reportThread.start();

        System.out.println("Earnings report started in background.");
    }
}
