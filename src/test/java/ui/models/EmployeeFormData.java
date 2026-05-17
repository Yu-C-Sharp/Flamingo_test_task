package ui.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeFormData implements IFormData {

    private String firstName;
    private String lastName;
    private int age;
    private String email;
    private int salary;
    private String department;
}
