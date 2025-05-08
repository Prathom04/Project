import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UniversityScheduleApp {
    private static final String DB_URL = "jdbc:sqlite:university.db";
    private JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UniversityScheduleApp().showLoginScreen());
    }

    private void showLoginScreen() {
        frame = new JFrame("University Class Scheduler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        JTextField emailField = new JTextField();
        JButton loginBtn = new JButton("Login");

        panel.add(new JLabel("Enter Email:"));
        panel.add(emailField);
        panel.add(loginBtn);

        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.endsWith("@ustc.bd")) {
                showTeacherDashboard(email);
            } else {
                showStudentSearchOption(email);
            }
        });

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    private void showTeacherDashboard(String email) {
        JFrame teacherFrame = new JFrame("Teacher Dashboard");
        teacherFrame.setSize(500, 400);
        teacherFrame.setLayout(new GridLayout(6, 1, 5, 5));

        JButton assignClassBtn = new JButton("Assign Class");
        JButton viewAllBtn = new JButton("View All Assigned Classes");
        JButton cancelClassBtn = new JButton("Cancel Class by ID");
        JButton backBtn = new JButton("Logout");

        assignClassBtn.addActionListener(e -> assignClassScreen(email));
        viewAllBtn.addActionListener(e -> showAllClasses());
        cancelClassBtn.addActionListener(e -> cancelClassScreen());
        backBtn.addActionListener(e -> {
            teacherFrame.dispose();
            showLoginScreen();
        });

        teacherFrame.add(new JLabel("Logged in as: " + email));
        teacherFrame.add(assignClassBtn);
        teacherFrame.add(viewAllBtn);
        teacherFrame.add(cancelClassBtn);
        teacherFrame.add(backBtn);

        teacherFrame.setVisible(true);
    }

    private void assignClassScreen(String email) {
        JFrame assignFrame = new JFrame("Assign Class");
        assignFrame.setSize(400, 400);
        assignFrame.setLayout(new GridLayout(9, 2, 5, 5));

        JTextField nameField = new JTextField();
        JTextField courseField = new JTextField();
        JTextField roomField = new JTextField();
        JTextField timeField = new JTextField();
        JTextField batchField = new JTextField();
        JTextField deptField = new JTextField();

        JButton submit = new JButton("Assign");
        JButton back = new JButton("Back");

        assignFrame.add(new JLabel("Teacher Name:"));
        assignFrame.add(nameField);
        assignFrame.add(new JLabel("Course:"));
        assignFrame.add(courseField);
        assignFrame.add(new JLabel("Room:"));
        assignFrame.add(roomField);
        assignFrame.add(new JLabel("Time:"));
        assignFrame.add(timeField);
        assignFrame.add(new JLabel("Batch:"));
        assignFrame.add(batchField);
        assignFrame.add(new JLabel("Department:"));
        assignFrame.add(deptField);
        assignFrame.add(submit);
        assignFrame.add(back);

        submit.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                String sql = "INSERT INTO Schedule (teacher_email, teacher_name, course, room, time, batch, department) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, email);
                pstmt.setString(2, nameField.getText());
                pstmt.setString(3, courseField.getText());
                pstmt.setString(4, roomField.getText());
                pstmt.setString(5, timeField.getText());
                pstmt.setString(6, batchField.getText());
                pstmt.setString(7, deptField.getText());
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(assignFrame, "Class Assigned Successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        back.addActionListener(e -> assignFrame.dispose());

        assignFrame.setVisible(true);
    }

    private void cancelClassScreen() {
        JFrame cancelFrame = new JFrame("Cancel Class");
        cancelFrame.setSize(300, 150);
        cancelFrame.setLayout(new GridLayout(3, 1, 5, 5));

        JTextField idField = new JTextField();
        JButton cancelBtn = new JButton("Cancel");
        JButton backBtn = new JButton("Back");

        cancelFrame.add(new JLabel("Enter Class ID to cancel:"));
        cancelFrame.add(idField);
        cancelFrame.add(cancelBtn);
        cancelFrame.add(backBtn);

        cancelBtn.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                String sql = "DELETE FROM Schedule WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(idField.getText()));
                int rows = pstmt.executeUpdate();
                JOptionPane.showMessageDialog(cancelFrame, rows > 0 ? "Class Canceled!" : "ID not found.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        backBtn.addActionListener(e -> cancelFrame.dispose());

        cancelFrame.setVisible(true);
    }

    private void showStudentSearchOption(String email) {
        JFrame studentFrame = new JFrame("Student Dashboard");
        studentFrame.setSize(400, 250);
        studentFrame.setLayout(new GridLayout(4, 1, 5, 5));

        JButton searchByTeacher = new JButton("Teacher's Schedule");
        JButton searchByBatch = new JButton("Student Schedule (Batch + Department)");
        JButton viewAll = new JButton("See All Classes");
        JButton back = new JButton("Logout");

        searchByTeacher.addActionListener(e -> searchByTeacherName());
        searchByBatch.addActionListener(e -> searchByBatchAndDepartment());
        viewAll.addActionListener(e -> showAllClasses());
        back.addActionListener(e -> {
            studentFrame.dispose();
            showLoginScreen();
        });

        studentFrame.add(new JLabel("What are you looking for?"));
        studentFrame.add(searchByTeacher);
        studentFrame.add(searchByBatch);
        studentFrame.add(viewAll);
        studentFrame.add(back);

        studentFrame.setVisible(true);
    }

    private void searchByTeacherName() {
        JFrame searchFrame = new JFrame("Search by Teacher");
        searchFrame.setSize(400, 200);
        searchFrame.setLayout(new GridLayout(3, 1, 5, 5));

        JTextField nameField = new JTextField();
        JButton searchBtn = new JButton("Search");

        searchFrame.add(new JLabel("Enter Teacher Name:"));
        searchFrame.add(nameField);
        searchFrame.add(searchBtn);

        searchBtn.addActionListener(e -> {
            List<String> results = getClasses("SELECT * FROM Schedule WHERE teacher_name = ?", nameField.getText());
            showResults(results, "Classes for Teacher: " + nameField.getText());
        });

        searchFrame.setVisible(true);
    }

    private void searchByBatchAndDepartment() {
        JFrame searchFrame = new JFrame("Search Student Schedule");
        searchFrame.setSize(400, 250);
        searchFrame.setLayout(new GridLayout(4, 2, 5, 5));

        JTextField batchField = new JTextField();
        JTextField deptField = new JTextField();
        JButton searchBtn = new JButton("Search");

        searchFrame.add(new JLabel("Batch:"));
        searchFrame.add(batchField);
        searchFrame.add(new JLabel("Department:"));
        searchFrame.add(deptField);
        searchFrame.add(searchBtn);

        searchBtn.addActionListener(e -> {
            String sql = "SELECT * FROM Schedule WHERE batch = ? AND department = ?";
            List<String> results = getClasses(sql, batchField.getText(), deptField.getText());
            showResults(results, "Schedule for " + batchField.getText() + " - " + deptField.getText());
        });

        searchFrame.setVisible(true);
    }

    private void showAllClasses() {
        List<String> classes = getClasses("SELECT * FROM Schedule");
        showResults(classes, "All Assigned Classes");
    }

    private List<String> getClasses(String query, String... params) {
        List<String> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setString(i + 1, params[i]);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String line = "ID: " + rs.getInt("id") +
                        ", Teacher: " + rs.getString("teacher_name") +
                        ", Email: " + rs.getString("teacher_email") +
                        ", Course: " + rs.getString("course") +
                        ", Room: " + rs.getString("room") +
                        ", Time: " + rs.getString("time") +
                        ", Batch: " + rs.getString("batch") +
                        ", Dept: " + rs.getString("department");
                results.add(line);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    private void showResults(List<String> data, String title) {
        JFrame resultFrame = new JFrame(title);
        resultFrame.setSize(600, 400);
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        for (String line : data) {
            textArea.append(line + "\n");
        }
        resultFrame.add(new JScrollPane(textArea));
        resultFrame.setVisible(true);
    }
}
