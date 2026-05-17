package api.models.booking;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    private String firstname;
    private String lastname;
    private int totalprice;
    private boolean depositpaid;
    private BookingDates bookingdates;
    private String additionalneeds;
}
