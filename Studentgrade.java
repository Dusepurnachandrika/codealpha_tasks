import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

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

public class Studentgrade extends JFrame {
    private ArrayList<Student> students;
    private DefaultTableModel tableModel;
    private JLabel totalLabel, avgLabel, highLabel, lowLabel;
    private JTextField nameField, gradeField;

    public Studentgrade() {
        students = new ArrayList<>();
        setTitle("Student Grade Manager");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout(10, 10));

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(mainPanel);

        // Top Panel for Input
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Student"));
        
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField(15);
        inputPanel.add(nameField);
        
        inputPanel.add(new JLabel("Grade (0-100):"));
        gradeField = new JTextField(5);
        inputPanel.add(gradeField);
        
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addStudent());
        inputPanel.add(addButton);
        
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Center Panel for Table
        String[] columnNames = {"Student Name", "Grade"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Panel for Summary
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary Report"));
        
        totalLabel = new JLabel("Total Students: 0");
        avgLabel = new JLabel("Average Grade: 0.00");
        highLabel = new JLabel("Highest Grade: N/A");
        lowLabel = new JLabel("Lowest Grade: N/A");
        
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        avgLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        highLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        lowLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        summaryPanel.add(totalLabel);
        summaryPanel.add(avgLabel);
        summaryPanel.add(highLabel);
        summaryPanel.add(lowLabel);
        
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        // Add Enter key listener for fields
        gradeField.addActionListener(e -> addStudent());
        nameField.addActionListener(e -> gradeField.requestFocus());
    }

    private void addStudent() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double grade = Double.parseDouble(gradeField.getText().trim());
            // Double.isNaN is important because "NaN" parses successfully but bypasses < 0 and > 100 checks
            if (grade < 0 || grade > 100 || Double.isNaN(grade)) {
                JOptionPane.showMessageDialog(this, "Grade must be between 0 and 100.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Student student = new Student(name, grade);
            students.add(student);
            tableModel.addRow(new Object[]{name, String.format("%.2f", grade)});
            
            updateSummary();
            
            nameField.setText("");
            gradeField.setText("");
            nameField.requestFocus();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid grade. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSummary() {
        if (students.isEmpty()) return;

        double sum = 0;
        double highest = students.get(0).getGrade();
        double lowest = students.get(0).getGrade();
        String highestStudent = students.get(0).getName();
        String lowestStudent = students.get(0).getName();

        for (Student s : students) {
            double g = s.getGrade();
            sum += g;
            if (g > highest) {
                highest = g;
                highestStudent = s.getName();
            }
            if (g < lowest) {
                lowest = g;
                lowestStudent = s.getName();
            }
        }

        double average = sum / students.size();

        totalLabel.setText(String.format("Total Students: %d", students.size()));
        avgLabel.setText(String.format("Average Grade: %.2f", average));
        highLabel.setText(String.format("Highest Grade: %.2f (%s)", highest, highestStudent));
        lowLabel.setText(String.format("Lowest Grade: %.2f (%s)", lowest, lowestStudent));
    }

    public static void main(String[] args) {
        // Set system look and feel for a better GUI appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new Studentgrade().setVisible(true);
        });
    }
}
