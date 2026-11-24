//Ishita
package service;

import model.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EarningsService {

    public void printEarningsReport(User lender, List<Booking> bookings) {

        double totalEarnings = 0;
        boolean found = false;

        System.out.println("\n--- LENDER EARNINGS REPORT ---");
        System.out.println("Lender: " + lender.getName());

        for (Booking booking : bookings) {

            Resource resource = booking.getResource();

            if (resource.getOwnerId() == lender.getUserId()
                    && "PAID".equalsIgnoreCase(resource.getBorrowingType())
                    && booking.getStatus() == BookingStatus.RETURNED
                    && booking.getReturnDate() != null) {

                long days = ChronoUnit.DAYS.between(
                        booking.getBorrowDate(),
                        booking.getReturnDate()
                );

                if (days < 1) {
                    days = 1;
                }

                double earnings =
                        resource.getPricePerDay() * days;

                totalEarnings += earnings;
                found = true;

                System.out.println(
                        resource.getName()
                                + " | Days: " + days
                                + " | Earnings: Rs. " + earnings
                );
            }
        }

        if (!found) {
            System.out.println("No completed paid bookings found.");
        }

        System.out.println("Total Earnings: Rs. " + totalEarnings);
    }
}
