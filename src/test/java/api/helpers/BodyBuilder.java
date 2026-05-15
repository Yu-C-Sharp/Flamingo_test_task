package api.helpers;

import api.models.Booking;
import api.models.BookingDates;
import api.models.auth.AuthRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;

@Log
public class BodyBuilder {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static <T> String getBodyBuildAsString(T bodyBuild) {
        try {
            return objectMapper.writeValueAsString(bodyBuild);
        } catch (JsonProcessingException exception) {
            logger.warning(exception.getMessage());
            logger.info("Invalid body was passed");
        }
        throw new IllegalArgumentException("Invalid body build parameter");
    }

    static String buildAuthRequest(String userName, String password) {
        return getBodyBuildAsString(
                AuthRequest.builder()
                        .username(userName)
                        .password(password)
                        .build()
        );
    }

    static String buildBooking(Booking modelObject) {
        return getBodyBuildAsString(
                Booking.builder()
                        .firstname(modelObject.getFirstname())
                        .lastname(modelObject.getLastname())
                        .totalprice(modelObject.getTotalprice())
                        .depositpaid(modelObject.isDepositpaid())
                        .bookingdates(BookingDates.builder()
                                .checkin(modelObject.getBookingdates().getCheckin())
                                .checkout(modelObject.getBookingdates().getCheckout())
                                .build())
                        .additionalneeds(modelObject.getAdditionalneeds())
                        .build()
        );
    }
}
