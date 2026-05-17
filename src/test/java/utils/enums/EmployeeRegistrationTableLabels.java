package utils.enums;

import lombok.Getter;

@Getter
public enum EmployeeRegistrationTableLabels {

    FIRST_NAME("First Name", 0),
    LAST_NAME("Last Name", 1),
    AGE("Age", 2),
    EMAIL("Email", 3),
    SALARY("Salary", 4),
    DEPARTMENT("Department", 5);

    private final String value;
    private final int index;

    EmployeeRegistrationTableLabels(String label, int index) {
        this.value = label;
        this.index = index;
    }
}

