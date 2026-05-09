import java.util.ArrayList;
import java.util.Scanner;

class Student {
    private String name;
    private double grade;

    public Student(String name, double grade) {
        this.name = name;
        this.grade = grade;
    }

    public String getName() {
        return name;
    }

    public double getGrade() {
        return grade;
    }
}

public class Studentgrade {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Student> students = new ArrayList<>();
        
        System.out.println("Welcome to the Student Grade Manager!");

        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Add a student and grade");
            System.out.println("2. Display summary report");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (choice == 1) {
                System.out.print("Enter student name: ");
                String name = scanner.nextLine();
                
                double grade = -1;
                while (grade < 0 || grade > 100) {
                    System.out.print("Enter student grade (0-100): ");
                    try {
                        grade = Double.parseDouble(scanner.nextLine());
                        if (grade < 0 || grade > 100) {
                            System.out.println("Grade must be between 0 and 100.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a valid number for the grade.");
                    }
                }
                
                students.add(new Student(name, grade));
                System.out.println("Student added successfully!");
                
            } else if (choice == 2) {
                if (students.isEmpty()) {
                    System.out.println("No students added yet.");
                } else {
                    displaySummary(students);
                }
            } else if (choice == 3) {
                System.out.println("Exiting program. Goodbye!");
                break;
            } else {
                System.out.println("Invalid option. Please choose 1, 2, or 3.");
            }
        }
        
        scanner.close();
    }

    private static void displaySummary(ArrayList<Student> students) {
        System.out.println("\n--- Summary Report ---");
        System.out.printf("%-20s %-10s\n", "Student Name", "Grade");
        System.out.println("--------------------------------");
        
        double sum = 0;
        double highest = students.get(0).getGrade();
        double lowest = students.get(0).getGrade();
        String highestStudent = students.get(0).getName();
        String lowestStudent = students.get(0).getName();

        for (Student student : students) {
            System.out.printf("%-20s %-10.2f\n", student.getName(), student.getGrade());
            sum += student.getGrade();
            
            if (student.getGrade() > highest) {
                highest = student.getGrade();
                highestStudent = student.getName();
            }
            
            if (student.getGrade() < lowest) {
                lowest = student.getGrade();
                lowestStudent = student.getName();
            }
        }

        double average = sum / students.size();

        System.out.println("--------------------------------");
        System.out.printf("Total Students: %d\n", students.size());
        System.out.printf("Average Grade: %.2f\n", average);
        System.out.printf("Highest Grade: %.2f (%s)\n", highest, highestStudent);
        System.out.printf("Lowest Grade: %.2f (%s)\n", lowest, lowestStudent);
    }
}
