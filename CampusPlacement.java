package campus;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

class StudentData {
    int studentId;
    String name, email, password, branch, skills;
    double percentage;
    int backlogs;
    StudentData(String name, String email, String password, String branch,
                double percentage, int backlogs, String skills) {
        this.name = name; this.email = email; this.password = password;
        this.branch = branch; this.percentage = percentage;
        this.backlogs = backlogs; this.skills = skills;
    }
}

class CompanyData {
    int companyId;
    String name, hrEmail, password, criteriaBranch, requiredSkills;
    double minPercentage;
    int maxBacklogs;
    CompanyData(String name, String hrEmail, String password,
                String criteriaBranch, double minPercentage,
                int maxBacklogs, String requiredSkills) {
        this.name = name; this.hrEmail = hrEmail; this.password = password;
        this.criteriaBranch = criteriaBranch; this.minPercentage = minPercentage;
        this.maxBacklogs = maxBacklogs; this.requiredSkills = requiredSkills;
    }
}

class PlacementDriveData {
    int driveId, companyId;
    java.util.Date date;
    String location, status = "PENDING";
    PlacementDriveData(int companyId, java.util.Date date, String location) {
        this.companyId = companyId; this.date = date; this.location = location;
    }
}

class ApplicationData {
    int appId, studentId, driveId;
    String status = "PENDING";
    ApplicationData(int studentId, int driveId) {
        this.studentId = studentId; this.driveId = driveId;
    }
}

/* ==================== DATABASE ==================== */
class DatabaseManager {
    private static DatabaseManager instance;
    private Connection conn;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/campus_placement?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Harish@1011";   // CHANGE TO YOUR MYSQL PASSWORD

    private DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Database connection failed:\n" + e.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    /* ---------- STUDENT ---------- */
    void addStudent(StudentData s) {
        String sql = "INSERT INTO students (name,email,password,branch,percentage,backlogs,skills) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.name); ps.setString(2, s.email); ps.setString(3, s.password);
            ps.setString(4, s.branch); ps.setDouble(5, s.percentage);
            ps.setInt(6, s.backlogs); ps.setString(7, s.skills);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.studentId = rs.getInt(1);
            }
        } catch (SQLException e) { showErr(e); }
    }

    void updateStudent(StudentData s) {
        String sql = "UPDATE students SET name=?,branch=?,percentage=?,backlogs=?,skills=? WHERE email=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.name); ps.setString(2, s.branch);
            ps.setDouble(3, s.percentage); ps.setInt(4, s.backlogs);
            ps.setString(5, s.skills); ps.setString(6, s.email);
            ps.executeUpdate();
        } catch (SQLException e) { showErr(e); }
    }

    StudentData getStudent(String email) {
        String sql = "SELECT * FROM students WHERE email=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StudentData s = new StudentData(
                            rs.getString("name"), rs.getString("email"),
                            rs.getString("password"), rs.getString("branch"),
                            rs.getDouble("percentage"), rs.getInt("backlogs"),
                            rs.getString("skills"));
                    s.studentId = rs.getInt("student_id");
                    return s;
                }
            }
        } catch (SQLException e) { showErr(e); }
        return null;
    }

    StudentData getStudentById(int id) {
        String sql = "SELECT * FROM students WHERE student_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StudentData s = new StudentData(
                            rs.getString("name"), rs.getString("email"),
                            rs.getString("password"), rs.getString("branch"),
                            rs.getDouble("percentage"), rs.getInt("backlogs"),
                            rs.getString("skills"));
                    s.studentId = rs.getInt("student_id");
                    return s;
                }
            }
        } catch (SQLException e) { showErr(e); }
        return null;
    }

    Map<String, StudentData> getAllStudents() {
        Map<String, StudentData> map = new HashMap<>();
        String sql = "SELECT * FROM students";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                StudentData s = new StudentData(
                        rs.getString("name"), rs.getString("email"),
                        rs.getString("password"), rs.getString("branch"),
                        rs.getDouble("percentage"), rs.getInt("backlogs"),
                        rs.getString("skills"));
                s.studentId = rs.getInt("student_id");
                map.put(s.email, s);
            }
        } catch (SQLException e) { showErr(e); }
        return map;
    }

    /* ---------- COMPANY ---------- */
    void addCompany(CompanyData c) {
        String sql = "INSERT INTO companies (name,hrEmail,password,criteria_branch,min_percentage,max_backlogs,required_skills) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.name); ps.setString(2, c.hrEmail); ps.setString(3, c.password);
            ps.setString(4, c.criteriaBranch); ps.setDouble(5, c.minPercentage);
            ps.setInt(6, c.maxBacklogs); ps.setString(7, c.requiredSkills);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.companyId = rs.getInt(1);
            }
        } catch (SQLException e) { showErr(e); }
    }

    CompanyData getCompany(int id) {
        String sql = "SELECT * FROM companies WHERE company_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CompanyData c = new CompanyData(
                            rs.getString("name"),
                            rs.getString("hrEmail"),
                            rs.getString("password"),
                            rs.getString("criteria_branch"),
                            rs.getDouble("min_percentage"),
                            rs.getInt("max_backlogs"),
                            rs.getString("required_skills"));
                    c.companyId = rs.getInt("company_id");
                    return c;
                }
            }
        } catch (SQLException e) { showErr(e); }
        return null;
    }

    List<CompanyData> getAllCompanies() {
        List<CompanyData> list = new ArrayList<>();
        String sql = "SELECT * FROM companies";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                CompanyData c = new CompanyData(
                        rs.getString("name"),
                        rs.getString("hrEmail"),
                        rs.getString("password"),
                        rs.getString("criteria_branch"),
                        rs.getDouble("min_percentage"),
                        rs.getInt("max_backlogs"),
                        rs.getString("required_skills"));
                c.companyId = rs.getInt("company_id");
                list.add(c);
            }
        } catch (SQLException e) { showErr(e); }
        return list;
    }

    /* ---------- DRIVE ---------- */
    void addDrive(PlacementDriveData d) {
        String sql = "INSERT INTO placement_drives (company_id,date,location,status) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.companyId);
            ps.setDate(2, new java.sql.Date(d.date.getTime()));
            ps.setString(3, d.location);
            ps.setString(4, d.status);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) d.driveId = rs.getInt(1);
            }
        } catch (SQLException e) { showErr(e); }
    }

    void updateDriveStatus(int driveId, String status) {
        String sql = "UPDATE placement_drives SET status=? WHERE drive_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status); ps.setInt(2, driveId);
            ps.executeUpdate();
        } catch (SQLException e) { showErr(e); }
    }

    List<PlacementDriveData> getAllDrives() {
        List<PlacementDriveData> list = new ArrayList<>();
        String sql = "SELECT * FROM placement_drives";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PlacementDriveData d = new PlacementDriveData(
                        rs.getInt("company_id"),
                        rs.getDate("date"),
                        rs.getString("location"));
                d.driveId = rs.getInt("drive_id");
                d.status = rs.getString("status");
                if (d.status == null) d.status = "PENDING";
                list.add(d);
            }
        } catch (SQLException e) { showErr(e); }
        return list;
    }

    /* ---------- APPLICATION ---------- */
    void addApplication(ApplicationData a) {
        String sql = "INSERT INTO applications (student_id,drive_id,status) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.studentId); ps.setInt(2, a.driveId); ps.setString(3, a.status);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) a.appId = rs.getInt(1);
            }
        } catch (SQLException e) { showErr(e); }
    }

    void approveApplication(int appId) { updateApplicationStatus(appId, "APPROVED"); }
    void rejectApplication(int appId) { updateApplicationStatus(appId, "REJECTED"); }

    void updateApplicationStatus(int appId, String status) {
        String sql = "UPDATE applications SET status=? WHERE app_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status); ps.setInt(2, appId);
            ps.executeUpdate();
        } catch (SQLException e) { showErr(e); }
    }

    void deleteApplication(int appId) {
        String sql = "DELETE FROM applications WHERE app_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appId);
            ps.executeUpdate();
        } catch (SQLException e) { showErr(e); }
    }

    List<ApplicationData> getApplications() {
        List<ApplicationData> list = new ArrayList<>();
        String sql = "SELECT * FROM applications";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ApplicationData a = new ApplicationData(
                        rs.getInt("student_id"), rs.getInt("drive_id"));
                a.appId = rs.getInt("app_id");
                a.status = rs.getString("status");
                if (a.status == null) a.status = "PENDING";
                list.add(a);
            }
        } catch (SQLException e) { showErr(e); }
        return list;
    }

    /* ---------- ELIGIBILITY (FIXED) ---------- */
    List<PlacementDriveData> getEligibleDrives(StudentData student) {
        List<PlacementDriveData> eligible = new ArrayList<>();
        for (PlacementDriveData d : getAllDrives()) {
            if (!"APPROVED".equalsIgnoreCase(d.status)) continue;
            CompanyData c = getCompany(d.companyId);
            if (c == null) continue;
            if (isEligible(student, c)) eligible.add(d);
        }
        return eligible;
    }

    private boolean isEligible(StudentData s, CompanyData c) {
        if (!s.branch.equalsIgnoreCase(c.criteriaBranch)) return false;
        if (s.percentage < c.minPercentage) return false;
        if (s.backlogs > c.maxBacklogs) return false;
        if (c.requiredSkills == null || c.requiredSkills.trim().isEmpty()) return true;
        String[] req = c.requiredSkills.split("\\s*,\\s*");
        String[] has = s.skills.split("\\s*,\\s*");
        Set<String> hasSet = new HashSet<>(Arrays.asList(has));
        for (String r : req) {
            if (!hasSet.contains(r.trim())) return false;
        }
        return true;
    }

    private void showErr(SQLException e) {
        JOptionPane.showMessageDialog(null,
                "DB Error: " + e.getMessage(),
                "Database", JOptionPane.ERROR_MESSAGE);
    }
}

/* ==================== LOGIN PAGE ==================== */
class LoginPage extends JFrame {
    private final JTextField txtUser = new JTextField(20);
    private final JPasswordField txtPass = new JPasswordField(20);
    private final JComboBox<String> cmbRole = new JComboBox<>(new String[]{"Student", "Company", "Coordinator"});

    LoginPage() {
        setTitle("Campus Placement – Login");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("CAMPUS PLACEMENT SYSTEM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 144, 255));
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        add(lblTitle, gc);

        gc.gridwidth = 1; gc.gridy++;
        add(new JLabel("Role:"), gc);
        gc.gridx = 1;
        add(cmbRole, gc);

        gc.gridx = 0; gc.gridy++;
        add(new JLabel("Username / ID:"), gc);
        gc.gridx = 1;
        add(txtUser, gc);

        gc.gridx = 0; gc.gridy++;
        add(new JLabel("Password:"), gc);
        gc.gridx = 1;
        add(txtPass, gc);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(30, 144, 255));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        gc.gridx = 0; gc.gridy++; gc.gridwidth = 2;
        add(btnLogin, gc);

        JButton btnRegister = new JButton("Register as Student");
        btnRegister.setBackground(new Color(40, 167, 69));
        btnRegister.setForeground(Color.WHITE);
        gc.gridy++;
        add(btnRegister, gc);

        btnLogin.addActionListener(e -> doLogin());
        btnRegister.addActionListener(e -> new StudentRegistration());
        setVisible(true);
    }

    private void doLogin() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());
        String role = (String) cmbRole.getSelectedItem();
        DatabaseManager db = DatabaseManager.getInstance();
        try {
            if ("Student".equals(role)) {
                StudentData s = db.getStudent(user);
                if (s != null && s.password.equals(pass)) {
                    dispose();
                    new StudentDashboard(s);
                    return;
                }
            } else if ("Company".equals(role)) {
                int id = Integer.parseInt(user);
                CompanyData c = db.getCompany(id);
                if (c != null && c.password.equals(pass)) {
                    dispose();
                    new CompanyDashboard(c);
                    return;
                }
            } else if ("Coordinator".equals(role)) {
                if ("coord".equals(user) && "admin".equals(pass)) {
                    dispose();
                    new CoordinatorDashboard();
                    return;
                }
            }
        } catch (NumberFormatException ex) { }
        JOptionPane.showMessageDialog(this, "Invalid credentials", "Login", JOptionPane.ERROR_MESSAGE);
    }
}

/* ==================== STUDENT REGISTRATION ==================== */
class StudentRegistration extends JFrame {
    StudentRegistration() {
        setTitle("Student Registration");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField[] fields = new JTextField[7];
        String[] labels = {"Name:", "Email:", "Password:", "Branch:", "Percentage:", "Backlogs:", "Skills (comma sep):"};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i;
            add(new JLabel(labels[i]), gc);
            gc.gridx = 1;
            fields[i] = new JTextField(20);
            add(fields[i], gc);
        }

        JButton btnReg = new JButton("Register");
        btnReg.setBackground(new Color(40, 167, 69));
        btnReg.setForeground(Color.WHITE);
        gc.gridx = 0; gc.gridy = 7; gc.gridwidth = 2;
        add(btnReg, gc);

        btnReg.addActionListener(e -> {
            try {
                StudentData s = new StudentData(
                    fields[0].getText(), fields[1].getText(), fields[2].getText(),
                    fields[3].getText(), Double.parseDouble(fields[4].getText()),
                    Integer.parseInt(fields[5].getText()), fields[6].getText()
                );
                DatabaseManager.getInstance().addStudent(s);
                JOptionPane.showMessageDialog(this, "Registered! Login now.");
                dispose();
                new LoginPage();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        setVisible(true);
    }
}

/* ==================== COMPANY REGISTRATION ==================== */
class CompanyRegistration extends JFrame {
    CompanyRegistration() {
        setTitle("Company Registration");
        setSize(520, 560);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField[] fields = new JTextField[7];
        String[] labels = {
                "Company Name:", "HR Email:", "Password:",
                "Criteria Branch:", "Min Percentage:", "Max Backlogs:",
                "Required Skills (comma sep):"
        };
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i;
            add(new JLabel(labels[i]), gc);
            gc.gridx = 1;
            fields[i] = new JTextField(20);
            add(fields[i], gc);
        }

        JButton btnReg = new JButton("Register Company");
        btnReg.setBackground(new Color(40, 167, 69));
        btnReg.setForeground(Color.WHITE);
        gc.gridx = 0; gc.gridy = 7; gc.gridwidth = 2;
        add(btnReg, gc);

        btnReg.addActionListener(e -> {
            try {
                CompanyData c = new CompanyData(
                        fields[0].getText(),
                        fields[1].getText(),
                        fields[2].getText(),
                        fields[3].getText(),
                        Double.parseDouble(fields[4].getText()),
                        Integer.parseInt(fields[5].getText()),
                        fields[6].getText()
                );
                DatabaseManager.getInstance().addCompany(c);
                JOptionPane.showMessageDialog(this,
                        "Company registered! Company ID: " + c.companyId +
                        "\nLogin with ID = " + c.companyId + " and password you entered.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        setVisible(true);
    }
}

/* ==================== STUDENT DASHBOARD ==================== */
class StudentDashboard extends JFrame {
    private final StudentData student;
    private final DatabaseManager db = DatabaseManager.getInstance();
    private JPanel infoPanel;
    private JPanel eligiblePanel;

    StudentDashboard(StudentData s) {
        super("Student Dashboard");
        this.student = s;
        setSize(920, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        buildHeader();
        buildInfoPanel();
        buildActionsPanel();
        buildEligiblePanel();
        buildLogout();
        setVisible(true);
    }

    private void buildHeader() {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(30, 144, 255));
        p.setBounds(0, 0, 920, 80);
        JLabel t = new JLabel("Student Dashboard");
        t.setFont(new Font("Arial", Font.BOLD, 24));
        t.setForeground(Color.WHITE);
        t.setBounds(30, 20, 300, 30);
        p.add(t);
        JLabel w = new JLabel("Welcome, " + student.name + "!");
        w.setFont(new Font("Arial", Font.PLAIN, 18));
        w.setForeground(Color.WHITE);
        w.setBounds(340, 20, 400, 30);
        p.add(w);
        add(p);
    }

    private void buildInfoPanel() {
        infoPanel = new JPanel(null);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBounds(30, 100, 400, 220);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Personal Information"));
        refreshInfoPanel();
        add(infoPanel);
    }

    private void refreshInfoPanel() {
        infoPanel.removeAll();
        int y = 30;
        addLine(infoPanel, "Name:", student.name, y); y += 30;
        addLine(infoPanel, "Email:", student.email, y); y += 30;
        addLine(infoPanel, "Branch:", student.branch, y); y += 30;
        addLine(infoPanel, "Percentage:", String.valueOf(student.percentage), y); y += 30;
        addLine(infoPanel, "Backlogs:", String.valueOf(student.backlogs), y); y += 30;
        addLine(infoPanel, "Skills:", student.skills, y);
        infoPanel.revalidate();
        infoPanel.repaint();
    }

    private void addLine(JPanel p, String label, String value, int y) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setBounds(20, y, 120, 25);
        p.add(l);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Arial", Font.PLAIN, 13));
        v.setBounds(150, y, 230, 25);
        p.add(v);
    }

    private void buildActionsPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        p.setBounds(450, 100, 440, 220);
        p.setBorder(BorderFactory.createTitledBorder("Quick Actions"));

        JButton b1 = styledBtn("View Eligible Drives", 20, 30);
        b1.addActionListener(e -> showEligible());
        p.add(b1);

        JButton b2 = styledBtn("Apply to Drive", 240, 30);
        b2.addActionListener(e -> applyDrive());
        p.add(b2);

        JButton b3 = styledBtn("Update Profile", 20, 90);
        b3.addActionListener(e -> updateProfile());
        p.add(b3);

        JButton b4 = styledBtn("My Applications", 240, 90);
        b4.addActionListener(e -> showMyApps());
        p.add(b4);

        add(p);
    }

    private void buildEligiblePanel() {
        eligiblePanel = new JPanel(new BorderLayout());
        eligiblePanel.setBackground(Color.WHITE);
        eligiblePanel.setBounds(30, 340, 860, 220);
        eligiblePanel.setBorder(BorderFactory.createTitledBorder("Eligible Drives"));
        refreshEligibleTable();               // <-- Fixed method
        add(eligiblePanel);
    }

    // ------------------- FIXED refreshEligibleTable -------------------
    private void refreshEligibleTable() {
        eligiblePanel.removeAll();

        String[] cols = {"Drive ID", "Company", "Date", "Location", "Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0);
        JTable tbl = new JTable(m);
        tbl.setRowHeight(30);
        eligiblePanel.add(new JScrollPane(tbl), BorderLayout.CENTER);

        boolean hasData = false;

        for (PlacementDriveData d : db.getAllDrives()) {
            if (!"APPROVED".equalsIgnoreCase(d.status)) continue;   // only approved drives

            CompanyData c = db.getCompany(d.companyId);
            if (c == null) continue;

            // Eligibility checks
            if (!student.branch.equalsIgnoreCase(c.criteriaBranch)) continue;
            if (student.percentage < c.minPercentage) continue;
            if (student.backlogs > c.maxBacklogs) continue;

            if (c.requiredSkills != null && !c.requiredSkills.trim().isEmpty()) {
                String[] req = c.requiredSkills.split("\\s*,\\s*");
                String[] has = student.skills.split("\\s*,\\s*");
                Set<String> hasSet = new HashSet<>(Arrays.asList(has));
                boolean ok = true;
                for (String r : req) {
                    if (!hasSet.contains(r.trim())) { ok = false; break; }
                }
                if (!ok) continue;
            }

            // Passed all checks → add row
            m.addRow(new Object[]{
                d.driveId,
                c.name,
                new SimpleDateFormat("yyyy-MM-dd").format(d.date),
                d.location,
                d.status
            });
            hasData = true;
        }

        if (!hasData) {
            m.addRow(new Object[]{"-", "No eligible drives found", "-", "-", "-"});
        }

        eligiblePanel.revalidate();
        eligiblePanel.repaint();
    }

    private JButton styledBtn(String text, int x, int y) {
        JButton b = new JButton(text);
        b.setBounds(x, y, 180, 40);
        b.setBackground(new Color(70, 130, 180));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private void showEligible() {
        List<PlacementDriveData> list = db.getEligibleDrives(student);
        String[] cols = {"Drive ID", "Company", "Date", "Location", "Status"};
        Object[][] data = new Object[list.size()][5];
        for (int i = 0; i < list.size(); i++) {
            PlacementDriveData d = list.get(i);
            CompanyData c = db.getCompany(d.companyId);
            data[i][0] = d.driveId;
            data[i][1] = c != null ? c.name : "-";
            data[i][2] = new SimpleDateFormat("yyyy-MM-dd").format(d.date);
            data[i][3] = d.location;
            data[i][4] = d.status;
        }
        JTable tbl = new JTable(data, cols);
        JOptionPane.showMessageDialog(this, new JScrollPane(tbl),
                "Eligible Drives", JOptionPane.INFORMATION_MESSAGE);
    }

    private void applyDrive() {
        String input = JOptionPane.showInputDialog(this, "Enter Drive ID:");
        if (input == null || input.trim().isEmpty()) return;
        try {
            int id = Integer.parseInt(input.trim());
            ApplicationData a = new ApplicationData(student.studentId, id);
            db.addApplication(a);
            JOptionPane.showMessageDialog(this, "Applied! App ID: " + a.appId);
            refreshEligibleTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateProfile() {
        JTextField fName = new JTextField(student.name, 20);
        JTextField fBranch = new JTextField(student.branch, 20);
        JTextField fPerc = new JTextField(String.valueOf(student.percentage), 20);
        JTextField fBack = new JTextField(String.valueOf(student.backlogs), 20);
        JTextField fSkills = new JTextField(student.skills, 20);
        Object[] fields = {
                "Name:", fName,
                "Branch:", fBranch,
                "Percentage:", fPerc,
                "Backlogs:", fBack,
                "Skills:", fSkills
        };
        int rc = JOptionPane.showConfirmDialog(this, fields, "Update Profile", JOptionPane.OK_CANCEL_OPTION);
        if (rc == JOptionPane.OK_OPTION) {
            try {
                student.name = fName.getText();
                student.branch = fBranch.getText();
                student.percentage = Double.parseDouble(fPerc.getText());
                student.backlogs = Integer.parseInt(fBack.getText());
                student.skills = fSkills.getText();
                db.updateStudent(student);
                refreshInfoPanel();
                refreshEligibleTable();
                JOptionPane.showMessageDialog(this, "Profile updated!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid data: " + ex.getMessage());
            }
        }
    }

    private void showMyApps() {
        List<ApplicationData> all = db.getApplications();
        List<ApplicationData> mine = new ArrayList<>();
        for (ApplicationData a : all)
            if (a.studentId == student.studentId) mine.add(a);
        if (mine.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No applications yet.");
            return;
        }

        String[] cols = {"App ID", "Drive ID", "Status", "Delete"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        for (ApplicationData a : mine) {
            model.addRow(new Object[]{a.appId, a.driveId, a.status, "Delete"});
        }

        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);

        TableColumn deleteCol = tbl.getColumnModel().getColumn(3);
        deleteCol.setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JButton btn = new JButton("Delete");
            String status = (String) model.getValueAt(row, 2);
            btn.setEnabled(!"APPROVED".equals(status));
            btn.setBackground(new Color(220, 53, 69));
            btn.setForeground(Color.WHITE);
            return btn;
        });

        deleteCol.setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            JButton button = new JButton("Delete");
            { button.setBackground(new Color(220, 53, 69)); button.setForeground(Color.WHITE); }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                button.addActionListener(e -> {
                    int appId = (int) model.getValueAt(row, 0);
                    String status = (String) model.getValueAt(row, 2);
                    if ("APPROVED".equals(status)) {
                        JOptionPane.showMessageDialog(StudentDashboard.this, "Cannot delete approved application.");
                        return;
                    }
                    if (JOptionPane.showConfirmDialog(StudentDashboard.this,
                            "Delete application ID " + appId + "?", "Confirm Delete",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        db.deleteApplication(appId);
                        model.removeRow(row);
                        JOptionPane.showMessageDialog(StudentDashboard.this, "Application deleted.");
                    }
                    fireEditingStopped();
                });
                return button;
            }

            @Override
            public Object getCellEditorValue() { return "Delete"; }
        });

        JOptionPane.showMessageDialog(this, new JScrollPane(tbl),
                "My Applications", JOptionPane.INFORMATION_MESSAGE);
        refreshEligibleTable();
    }

    private void buildLogout() {
        JButton b = new JButton("Logout");
        b.setBounds(790, 580, 100, 40);
        b.setBackground(new Color(220, 53, 69));
        b.setForeground(Color.WHITE);
        b.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose();
                new LoginPage();
            }
        });
        add(b);
    }
}

/* ==================== COMPANY DASHBOARD ==================== */
class CompanyDashboard extends JFrame {
    private final CompanyData company;
    private final DatabaseManager db = DatabaseManager.getInstance();

    CompanyDashboard(CompanyData c) {
        super("Company Dashboard");
        this.company = c;
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        buildHeader();
        buildInfoPanel();
        buildActionsPanel();
        buildDrivesPanel();
        buildLogout();
        setVisible(true);
    }

    private void buildHeader() {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(30, 144, 255));
        p.setBounds(0, 0, 1000, 80);
        JLabel t = new JLabel("Company Dashboard");
        t.setFont(new Font("Arial", Font.BOLD, 24));
        t.setForeground(Color.WHITE);
        t.setBounds(30, 20, 300, 30);
        p.add(t);
        JLabel w = new JLabel("Welcome, " + company.name + "!");
        w.setFont(new Font("Arial", Font.PLAIN, 18));
        w.setForeground(Color.WHITE);
        w.setBounds(340, 20, 400, 30);
        p.add(w);
        add(p);
    }

    private void buildInfoPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        p.setBounds(30, 100, 400, 220);
        p.setBorder(BorderFactory.createTitledBorder("Company Information"));
        int y = 30;
        addLine(p, "Name:", company.name, y); y += 30;
        addLine(p, "HR Email:", company.hrEmail, y); y += 30;
        addLine(p, "Branch:", company.criteriaBranch, y); y += 30;
        addLine(p, "Min %:", String.valueOf(company.minPercentage), y); y += 30;
        addLine(p, "Max Backlogs:", String.valueOf(company.maxBacklogs), y); y += 30;
        addLine(p, "Skills:", company.requiredSkills, y);
        add(p);
    }

    private void addLine(JPanel p, String label, String value, int y) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setBounds(20, y, 130, 25);
        p.add(l);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Arial", Font.PLAIN, 13));
        v.setBounds(160, y, 220, 25);
        p.add(v);
    }

    private void buildActionsPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        p.setBounds(450, 100, 520, 220);
        p.setBorder(BorderFactory.createTitledBorder("Quick Actions"));

        JButton b1 = styledBtn("Post New Drive", 20, 30);
        b1.addActionListener(e -> postDrive());
        p.add(b1);

        JButton b2 = styledBtn("View Applications", 300, 30);
        b2.addActionListener(e -> viewApps());
        p.add(b2);

        add(p);
    }

    private JButton styledBtn(String text, int x, int y) {
        JButton b = new JButton(text);
        b.setBounds(x, y, 200, 40);
        b.setBackground(new Color(70, 130, 180));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private void buildDrivesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBounds(30, 340, 940, 220);
        p.setBorder(BorderFactory.createTitledBorder("My Drives"));

        String[] cols = {"Drive ID", "Date", "Location", "Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0);
        JTable tbl = new JTable(m);
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);

        for (PlacementDriveData d : db.getAllDrives()) {
            if (d.companyId == company.companyId) {
                m.addRow(new Object[]{d.driveId,
                        new SimpleDateFormat("yyyy-MM-dd").format(d.date),
                        d.location, d.status});
            }
        }
        add(p);
    }

    private void postDrive() {
        JTextField fDate = new JTextField(15);
        JTextField fLoc = new JTextField(15);
        Object[] msg = {"Date (yyyy-MM-dd):", fDate, "Location:", fLoc};
        if (JOptionPane.showConfirmDialog(this, msg, "Post Drive", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        try {
            java.util.Date d = new SimpleDateFormat("yyyy-MM-dd").parse(fDate.getText());
            PlacementDriveData pd = new PlacementDriveData(company.companyId, d, fLoc.getText());
            db.addDrive(pd);
            JOptionPane.showMessageDialog(this, "Drive posted – ID: " + pd.driveId);
            dispose();
            new CompanyDashboard(company);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date: " + ex.getMessage());
        }
    }

    private void viewApps() {
        List<ApplicationData> all = db.getApplications();
        List<ApplicationData> mine = new ArrayList<>();
        for (ApplicationData a : all) {
            PlacementDriveData d = driveById(a.driveId);
            if (d != null && d.companyId == company.companyId) {
                mine.add(a);
            }
        }
        if (mine.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No applications for your drives.");
            return;
        }

        String[] cols = {"App ID", "Student ID", "Name", "Email", "%", "Skills", "Status", "Approve", "Reject"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (ApplicationData a : mine) {
            StudentData s = db.getStudentById(a.studentId);
            if (s == null) continue;
            String status = a.status != null ? a.status : "PENDING";
            String approveText = "APPROVED".equals(status) ? "Approved" : "Approve";
            String rejectText = "REJECTED".equals(status) ? "Rejected" : "Reject";
            model.addRow(new Object[]{
                a.appId, s.studentId, s.name, s.email,
                s.percentage, s.skills, status, approveText, rejectText
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);

        new ButtonColumn(table, 7, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                int appId = (int) model.getValueAt(row, 0);
                StudentData s = db.getStudentById((int) model.getValueAt(row, 1));
                db.approveApplication(appId);
                JOptionPane.showMessageDialog(CompanyDashboard.this, s.name + " APPROVED!");
                viewApps();
            }
        }, "Approved");

        new ButtonColumn(table, 8, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                int appId = (int) model.getValueAt(row, 0);
                StudentData s = db.getStudentById((int) model.getValueAt(row, 1));
                db.rejectApplication(appId);
                JOptionPane.showMessageDialog(CompanyDashboard.this, s.name + " REJECTED!");
                viewApps();
            }
        }, "Rejected");

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(900, 400));
        JOptionPane.showMessageDialog(this, scroll, "Student Applications", JOptionPane.PLAIN_MESSAGE);
    }

    private PlacementDriveData driveById(int id) {
        for (PlacementDriveData d : db.getAllDrives())
            if (d.driveId == id) return d;
        return null;
    }

    private void buildLogout() {
        JButton b = new JButton("Logout");
        b.setBounds(870, 580, 100, 40);
        b.setBackground(new Color(220, 53, 69));
        b.setForeground(Color.WHITE);
        b.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose();
                new LoginPage();
            }
        });
        add(b);
    }
}

/* ==================== ButtonColumn ==================== */
class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
    private final JTable table;
    private final Action action;
    private final JButton renderButton;
    private final JButton editButton;
    private final String disabledText;

    public ButtonColumn(JTable table, int column, Action action, String disabledText) {
        this.table = table;
        this.action = action;
        this.disabledText = disabledText;
        renderButton = new JButton();
        editButton = new JButton();
        editButton.setFocusPainted(false);
        editButton.addActionListener(this);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(column).setCellRenderer(this);
        columnModel.getColumn(column).setCellEditor(this);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        renderButton.setText(value.toString());
        boolean enabled = !disabledText.equals(value);
        renderButton.setEnabled(enabled);
        renderButton.setBackground(enabled ? new Color(40, 167, 69) : Color.LIGHT_GRAY);
        renderButton.setForeground(Color.WHITE);
        return renderButton;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        editButton.setText(value.toString());
        return editButton;
    }

    @Override
    public Object getCellEditorValue() { return editButton.getText(); }

    @Override
    public void actionPerformed(ActionEvent e) {
        int row = table.convertRowIndexToModel(table.getEditingRow());
        fireEditingStopped();
        ActionEvent event = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "" + row);
        action.actionPerformed(event);
    }
}

/* ==================== COORDINATOR DASHBOARD ==================== */
class CoordinatorDashboard extends JFrame {
    private final DatabaseManager db = DatabaseManager.getInstance();

    CoordinatorDashboard() {
        super("Coordinator Dashboard");
        setSize(920, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        buildHeader();
        buildActions();
        buildOverview();
        buildLogout();
        setVisible(true);
    }

    private void buildHeader() {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(30, 144, 255));
        p.setBounds(0, 0, 920, 80);
        JLabel t = new JLabel("Coordinator Dashboard");
        t.setFont(new Font("Arial", Font.BOLD, 24));
        t.setForeground(Color.WHITE);
        t.setBounds(30, 20, 350, 30);
        p.add(t);
        add(p);
    }

    private void buildActions() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        p.setBounds(30, 100, 860, 80);
        p.setBorder(BorderFactory.createTitledBorder("Quick Actions"));

        JButton b1 = styledBtn("All Students", 20, 20);
        b1.addActionListener(e -> viewStudents());
        p.add(b1);

        JButton b2 = styledBtn("All Companies", 200, 20);
        b2.addActionListener(e -> viewCompanies());
        p.add(b2);

        JButton b3 = styledBtn("Approve Drive", 380, 20);
        b3.addActionListener(e -> approveDrive());
        p.add(b3);

        JButton b4 = styledBtn("All Applications", 560, 20);
        b4.addActionListener(e -> viewAllApps());
        p.add(b4);

        JButton b5 = styledBtn("Add Company", 740, 20);
        b5.addActionListener(e -> new CompanyRegistration());
        p.add(b5);

        add(p);
    }

    private JButton styledBtn(String text, int x, int y) {
        JButton b = new JButton(text);
        b.setBounds(x, y, 150, 40);
        b.setBackground(new Color(70, 130, 180));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private void buildOverview() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBounds(30, 200, 860, 360);
        p.setBorder(BorderFactory.createTitledBorder("Records Overview"));

        String[] cols = {"Type", "Count"};
        Object[][] data = {
                {"Students", db.getAllStudents().size()},
                {"Companies", db.getAllCompanies().size()},
                {"Drives", db.getAllDrives().size()},
                {"Applications", db.getApplications().size()}
        };
        JTable tbl = new JTable(data, cols);
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(p);
    }

    private void viewStudents() {
        Map<String, StudentData> map = db.getAllStudents();
        if (map.isEmpty()) { JOptionPane.showMessageDialog(this, "No students."); return; }
        String[] cols = {"ID", "Name", "Email", "Branch", "%", "Backlogs"};
        Object[][] data = new Object[map.size()][6];
        int i = 0;
        for (StudentData s : map.values()) {
            data[i][0] = s.studentId; data[i][1] = s.name; data[i][2] = s.email;
            data[i][3] = s.branch; data[i][4] = s.percentage; data[i][5] = s.backlogs;
            i++;
        }
        showTable(data, cols, "All Students");
    }

    private void viewCompanies() {
        List<CompanyData> list = db.getAllCompanies();
        if (list.isEmpty()) { JOptionPane.showMessageDialog(this, "No companies."); return; }
        String[] cols = {"ID", "Name", "HR Email", "Branch", "Min %", "Max Back", "Skills"};
        Object[][] data = new Object[list.size()][7];
        for (int i = 0; i < list.size(); i++) {
            CompanyData c = list.get(i);
            data[i][0] = c.companyId; data[i][1] = c.name; data[i][2] = c.hrEmail;
            data[i][3] = c.criteriaBranch; data[i][4] = c.minPercentage;
            data[i][5] = c.maxBacklogs; data[i][6] = c.requiredSkills;
        }
        showTable(data, cols, "All Companies");
    }

    private void approveDrive() {
        String idStr = JOptionPane.showInputDialog(this, "Drive ID:");
        if (idStr == null || idStr.trim().isEmpty()) return;
        try {
            int id = Integer.parseInt(idStr.trim());
            String[] opts = {"APPROVED", "REJECTED"};
            String sel = (String) JOptionPane.showInputDialog(this,
                    "New status:", "Approve Drive",
                    JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
            if (sel != null) {
                db.updateDriveStatus(id, sel);
                JOptionPane.showMessageDialog(this, "Drive " + id + " → " + sel);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void viewAllApps() {
        List<ApplicationData> list = db.getApplications();
        if (list.isEmpty()) { JOptionPane.showMessageDialog(this, "No applications."); return; }
        String[] cols = {"App ID", "Student ID", "Drive ID", "Status"};
        Object[][] data = new Object[list.size()][4];
        for (int i = 0; i < list.size(); i++) {
            ApplicationData a = list.get(i);
            data[i][0] = a.appId; data[i][1] = a.studentId;
            data[i][2] = a.driveId; data[i][3] = a.status;
        }
        showTable(data, cols, "All Applications");
    }

    private void showTable(Object[][] data, String[] cols, String title) {
        JTable tbl = new JTable(data, cols);
        JOptionPane.showMessageDialog(this, new JScrollPane(tbl), title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void buildLogout() {
        JButton b = new JButton("Logout");
        b.setBounds(790, 580, 100, 40);
        b.setBackground(new Color(220, 53, 69));
        b.setForeground(Color.WHITE);
        b.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose();
                new LoginPage();
            }
        });
        add(b);
    }
}

/* ==================== MAIN ==================== */
public class CampusPlacement {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}