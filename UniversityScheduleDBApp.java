import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UniversityScheduleDBApp {
    private static final String DB_URL = "jdbc:sqlite:university.db"; // Database URL (university.db)

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("University Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout());

        // Login Panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(2, 2));

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();

        JButton loginButton = new JButton("Login");

        loginPanel.add(emailLabel);
        loginPanel.add(emailField);
        loginPanel.add(new JLabel());  // Empty cell for layout
        loginPanel.add(loginButton);

        frame.add(loginPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim().toLowerCase(); // Trim and lowercase the email

            if (email.isEmpty()) {
                return; // Don't proceed if the email is empty
            }

            // Validate user login based on the email domain
            if (isValidEmail(email)) {
                if (email.endsWith("@ustc.bd")) {
                    // Teacher login
                    showTeacherDashboard(email);
                } else {
                    // Student login
                    showStudentDashboard();
                }
                frame.dispose(); // Close login screen after successful login
            }
        });

        frame.setVisible(true);
    }

    // Check if the email domain is valid (for teacher or student)
    private static boolean isValidEmail(String email) {
        return email.contains("@");
    }

    private static void showTeacherDashboard(String teacherEmail) {
        JFrame teacherFrame = new JFrame("Teacher Dashboard");
        teacherFrame.setSize(600, 400);
        teacherFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        teacherFrame.setLayout(new BorderLayout());

        JTextArea scheduleArea = new JTextArea();
        scheduleArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(scheduleArea);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(5, 2));

        JLabel courseLabel = new JLabel("Course:");
        JTextField courseField = new JTextField();
        JLabel roomLabel = new JLabel("Room:");
        JTextField roomField = new JTextField();
        JLabel timeLabel = new JLabel("Time:");
        JTextField timeField = new JTextField();
        JLabel batchLabel = new JLabel("Batch:");
        JTextField batchField = new JTextField();
        JLabel departmentLabel = new JLabel("Department:");
        JTextField departmentField = new JTextField();
        JButton assignButton = new JButton("Assign Class");

        controlPanel.add(courseLabel);
        controlPanel.add(courseField);
        controlPanel.add(roomLabel);
        controlPanel.add(roomField);
        controlPanel.add(timeLabel);
        controlPanel.add(timeField);
        controlPanel.add(batchLabel);
        controlPanel.add(batchField);
        controlPanel.add(departmentLabel);
        controlPanel.add(departmentField);

        controlPanel.add(new JLabel());  // Empty cell for layout
        controlPanel.add(assignButton);

        // Fetch and display teacher's current schedule
        try (Connection conn = connect()) {
            if (conn != null) {
                String query = "SELECT * FROM Schedule WHERE teacher_email = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, teacherEmail);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String course = rs.getString("course");
                    String room = rs.getString("room");
                    String time = rs.getString("time");
                    String batch = rs.getString("batch");
                    String department = rs.getString("department");
                    scheduleArea.append("Course: " + course + ", Room: " + room + ", Time: " + time + 
                                         ", Batch: " + batch + ", Department: " + department + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Assign new class to the teacher
        assignButton.addActionListener(e -> {
            String course = courseField.getText().trim();
            String room = roomField.getText().trim();
            String time = timeField.getText().trim();
            String batch = batchField.getText().trim();
            String department = departmentField.getText().trim();

            if (course.isEmpty() || room.isEmpty() || time.isEmpty() || batch.isEmpty() || department.isEmpty()) {
                return; // Don't proceed if any field is empty
            }

            // Add new class to the schedule
            try (Connection conn = connect()) {
                if (conn != null) {
                    String insertQuery = "INSERT INTO Schedule (course, teacher_email, room, time, batch, department) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(insertQuery);
                    pstmt.setString(1, course);
                    pstmt.setString(2, teacherEmail);
                    pstmt.setString(3, room);
                    pstmt.setString(4, time);
                    pstmt.setString(5, batch);
                    pstmt.setString(6, department);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(teacherFrame, "Class assigned successfully!");
                    courseField.setText("");
                    roomField.setText("");
                    timeField.setText("");
                    batchField.setText("");
                    departmentField.setText("");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        teacherFrame.add(scrollPane, BorderLayout.CENTER);
        teacherFrame.add(controlPanel, BorderLayout.SOUTH);

        teacherFrame.setVisible(true);
    }

    private static void showStudentDashboard() {
        JFrame studentFrame = new JFrame("Student Dashboard");
        studentFrame.setSize(400, 200);
        studentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        studentFrame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel batchLabel = new JLabel("Batch:");
        JTextField batchField = new JTextField();
        JLabel departmentLabel = new JLabel("Department:");
        JTextField departmentField = new JTextField();

        JButton viewScheduleButton = new JButton("View Schedule");

        panel.add(batchLabel);
        panel.add(batchField);
        panel.add(departmentLabel);
        panel.add(departmentField);
        panel.add(viewScheduleButton);

        studentFrame.add(panel, BorderLayout.CENTER);

        viewScheduleButton.addActionListener(e -> {
            String batch = batchField.getText();
            String department = departmentField.getText();

            if (batch.isEmpty() || department.isEmpty()) {
                return; // Don't proceed if batch or department is empty
            }

            showStudentSchedule(batch, department);
        });

        studentFrame.setVisible(true);
    }

    private static void showStudentSchedule(String batch, String department) {
        JFrame scheduleFrame = new JFrame("Your Schedule");
        scheduleFrame.setSize(400, 300);
        scheduleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea scheduleArea = new JTextArea();
        scheduleArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(scheduleArea);

        try (Connection conn = connect()) {
            if (conn != null) {
                String query = "SELECT * FROM Schedule WHERE batch = ? AND department = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, batch);
                pstmt.setString(2, department);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String course = rs.getString("course");
                    String teacher = rs.getString("teacher_email");
                    String room = rs.getString("room");
                    String time = rs.getString("time");
                    scheduleArea.append("Course: " + course + ", Teacher: " + teacher + ", Room: " + room + ", Time: " + time + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        scheduleFrame.add(scrollPane, BorderLayout.CENTER);
        scheduleFrame.setVisible(true);
    }

    // Database connection method
    private static Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL);
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
