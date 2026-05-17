package utils.enums;

import lombok.Getter;

@Getter
public enum StudentRegistrationTableLabels {

    STUDENT_FULL_NAME("Student Name"),
    STUDENT_EMAIL("Student Email"),
    GENDER("Gender"),
    PHONE("Mobile"),
    BIRTHDAY("Date of Birth"),
    SUBJECTS("Subjects"),
    HOBBIES("Hobbies"),
    FULL_FILE_NAME("Picture"),
    ADDRESS("Address"),
    STATE_AND_CITY("State and City");

    private final String value;

    StudentRegistrationTableLabels(String name) {
        this.value = name;
    }
}
