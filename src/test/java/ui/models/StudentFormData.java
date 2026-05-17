package ui.models;

import lombok.Data;

@Data
public class StudentFormData implements IFormData {
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private String phone;
    private Birth birth;
    private String subject;
    private String hobby;
    private String address;
    private String state;
    private String city;

    @Data
    public static class Birth {
        String month;
        String year;
        int day;
    }
}
