import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * EMPLOYEE MANAGEMENT (ATTENDANCE)
 * Lists staff (from the `staff` table) and lets a manager log time-in /
 * time-out per employee per day. Data is kept in memory for now;
 * hook loadMockStaff()/attendance list to real DAO calls later.
 *
 * CRUD:
 *   Staff Directory -> Add Staff / Edit Staff / Delete Staff / Refresh
 *   Attendance      -> Time In (create) / Time Out (update) / Edit Record / Delete Record
 */
public class EmployeeAttendanceUI extends JFrame {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    // Lenient parser so users can type "8:05 am" or "08:05 AM"
    private static final DateTimeFormatter TIME_PARSER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive().appendPattern("h:mm a").toFormatter(Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalTime SHIFT_START = LocalTime.of(8, 0);
    private static final String[] ROLES = {"Cashier", "Kitchen Staff", "Store Manager", "Staff"};
    private static final String[] STATUSES = {"Present", "Late", "Absent"};

    private final List<Staff> staffList = new ArrayList<>();
    private final List<AttendanceRecord> attendance = new ArrayList<>();
    /** Keeps each staff member's password hash so edits don't wipe it (no setters needed on Staff). */
    private final Map<Integer, String> staffPasswords = new HashMap<>();
    private int nextStaffId = 1;
    private int nextAttendanceId = 1;

    private DefaultTableModel staffTableModel;
    private JTable staffTable;
    private DefaultTableModel attendanceTableModel;
    private JTable attendanceTable;
    private JLabel lblClock;
    private JComboBox<String> cmbStaffPicker;

    public EmployeeAttendanceUI() {
        UITheme.applyGlobalDefaults();
        setTitle("Employee Management - Attendance");
        setSize(1020, 680);
        setMinimumSize(new Dimension(960, 620));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(UITheme.headerPanel("Employee Management", "Staff directory and daily attendance tracking"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildStaffPanel(), buildAttendancePanel());
        split.setResizeWeight(0.42);
        split.setBorder(null);
        split.setBackground(UITheme.BACKGROUND);
        add(split, BorderLayout.CENTER);

        loadMockStaff();
        refreshStaffTable();
        refreshStaffPicker();
        startClock();
    }

    // ================= UI BUILDERS =================

    private JComponent buildStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(16, 16, 8, 16));

        // Title on the left, CRUD buttons on the right
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.BACKGROUND);

        JLabel title = new JLabel("Staff Directory");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.ACCENT_DARK);
        top.add(title, BorderLayout.WEST);

        JPanel staffButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        staffButtons.setBackground(UITheme.BACKGROUND);
        JButton btnAddStaff = UITheme.button("Add Staff");
        JButton btnEditStaff = UITheme.button("Edit Staff");
        JButton btnDeleteStaff = UITheme.secondaryButton("Delete Staff");
        JButton btnRefreshStaff = UITheme.secondaryButton("Refresh");
        btnAddStaff.addActionListener(e -> addStaff());
        btnEditStaff.addActionListener(e -> editStaff());
        btnDeleteStaff.addActionListener(e -> deleteStaff());
        btnRefreshStaff.addActionListener(e -> refreshAll());
        staffButtons.add(btnAddStaff);
        staffButtons.add(btnEditStaff);
        staffButtons.add(btnDeleteStaff);
        staffButtons.add(btnRefreshStaff);
        top.add(staffButtons, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        staffTableModel = new DefaultTableModel(
                new Object[]{"Staff ID", "First Name", "Last Name", "Email", "Role"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        staffTable = new JTable(staffTableModel);
        UITheme.styleTable(staffTable);
        JScrollPane scroll = new JScrollPane(staffTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(8, 16, 16, 16));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controls.setBackground(UITheme.BACKGROUND);
        controls.setBorder(UITheme.titled("Time Clock"));

        lblClock = new JLabel();
        lblClock.setFont(UITheme.FONT_MONO_BOLD);
        lblClock.setForeground(UITheme.ACCENT_DARK);

        cmbStaffPicker = new JComboBox<>();
        cmbStaffPicker.setPreferredSize(new Dimension(200, 28));

        JButton btnTimeIn = UITheme.button("Time In");
        JButton btnTimeOut = UITheme.secondaryButton("Time Out");
        JButton btnEditRecord = UITheme.button("Edit Record");
        JButton btnDeleteRecord = UITheme.secondaryButton("Delete Record");
        btnTimeIn.addActionListener(e -> timeIn());
        btnTimeOut.addActionListener(e -> timeOut());
        btnEditRecord.addActionListener(e -> editAttendanceRecord());
        btnDeleteRecord.addActionListener(e -> deleteAttendanceRecord());

        controls.add(new JLabel("Employee:"));
        controls.add(cmbStaffPicker);
        controls.add(btnTimeIn);
        controls.add(btnTimeOut);
        controls.add(Box.createRigidArea(new Dimension(8, 0)));
        controls.add(btnEditRecord);
        controls.add(btnDeleteRecord);
        controls.add(Box.createRigidArea(new Dimension(8, 0)));
        controls.add(lblClock);
        panel.add(controls, BorderLayout.NORTH);

        attendanceTableModel = new DefaultTableModel(
                new Object[]{"Att. ID", "Staff ID", "Name", "Date", "Time In", "Time Out", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        attendanceTable = new JTable(attendanceTableModel);
        UITheme.styleTable(attendanceTable);
        JScrollPane scroll = new JScrollPane(attendanceTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> lblClock.setText(
                LocalDate.now().format(DATE_FMT) + "   " + LocalTime.now().withNano(0).format(TIME_FMT)));
        timer.setInitialDelay(0);
        timer.start();
    }

    // ================= STAFF: DATA + CRUD =================

    private void loadMockStaff() {
        addMockStaff("Yuki", "Alvarez", "yuki.alvarez@riceball.ph", "Cashier");
        addMockStaff("Renzo", "Dela Cruz", "renzo.delacruz@riceball.ph", "Kitchen Staff");
        addMockStaff("Sakura", "Bautista", "sakura.bautista@riceball.ph", "Store Manager");
    }

    private void addMockStaff(String first, String last, String email, String role) {
        int id = nextStaffId++;
        staffList.add(new Staff(id, first, last, email, "hashed_pw", role));
        staffPasswords.put(id, "hashed_pw");
    }

    private void refreshStaffTable() {
        staffTableModel.setRowCount(0);
        for (Staff s : staffList) {
            staffTableModel.addRow(new Object[]{
                    s.getStaffID(), s.getFirstName(), s.getLastName(), s.getEmail(), s.getRole()
            });
        }
    }

    private void refreshStaffPicker() {
        Object previous = cmbStaffPicker.getSelectedItem();
        cmbStaffPicker.removeAllItems();
        for (Staff s : staffList) {
            cmbStaffPicker.addItem(s.getStaffID() + " - " + s.getFullName());
        }
        if (previous != null) cmbStaffPicker.setSelectedItem(previous);
    }

    /** READ: reloads every table and the picker. */
    private void refreshAll() {
        refreshStaffTable();
        refreshStaffPicker();
        refreshAttendanceTable();
    }

    private Staff getSelectedStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0) return null;
        int id = ((Number) staffTableModel.getValueAt(row, 0)).intValue();
        return findStaffById(id);
    }

    private Staff findStaffById(int id) {
        for (Staff s : staffList) if (s.getStaffID() == id) return s;
        return null;
    }

    private boolean emailTaken(String email, int ignoreStaffId) {
        for (Staff s : staffList) {
            if (s.getStaffID() != ignoreStaffId && s.getEmail().equalsIgnoreCase(email)) return true;
        }
        return false;
    }

    /** CREATE */
    private void addStaff() {
        StaffForm form = new StaffForm(null);
        if (!form.prompt("Add Staff")) return;

        int id = nextStaffId++;
        String hash = "hashed_" + form.password().hashCode(); // TODO: use a real hash (e.g. BCrypt) with the DB
        staffList.add(new Staff(id, form.first(), form.last(), form.email(), hash, form.role()));
        staffPasswords.put(id, hash);
        refreshAll();
    }

    /** UPDATE */
    private void editStaff() {
        Staff old = getSelectedStaff();
        if (old == null) {
            JOptionPane.showMessageDialog(this, "Select a staff member from the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        StaffForm form = new StaffForm(old);
        if (!form.prompt("Edit Staff #" + old.getStaffID())) return;

        String hash = staffPasswords.getOrDefault(old.getStaffID(), "hashed_pw");
        if (!form.password().isEmpty()) hash = "hashed_" + form.password().hashCode();

        Staff updated = new Staff(old.getStaffID(), form.first(), form.last(), form.email(), hash, form.role());
        staffList.set(staffList.indexOf(old), updated);
        staffPasswords.put(old.getStaffID(), hash);

        // Keep the name shown on existing attendance rows in sync
        for (int i = 0; i < attendance.size(); i++) {
            AttendanceRecord r = attendance.get(i);
            if (r.getStaffID() == old.getStaffID()) {
                attendance.set(i, new AttendanceRecord(r.getAttendanceID(), r.getStaffID(), updated.getFullName(),
                        r.getDate(), r.getTimeIn(), r.getTimeOut(), r.getStatus()));
            }
        }
        refreshAll();
    }

    /** DELETE (also removes that person's attendance records) */
    private void deleteStaff() {
        Staff s = getSelectedStaff();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Select a staff member from the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long records = attendance.stream().filter(r -> r.getStaffID() == s.getStaffID()).count();
        String msg = "Delete staff \"" + s.getFullName() + "\"?"
                + (records > 0 ? "\n\nTheir " + records + " attendance record(s) will also be removed." : "");
        int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        staffList.remove(s);
        staffPasswords.remove(s.getStaffID());
        attendance.removeIf(r -> r.getStaffID() == s.getStaffID());
        refreshAll();
    }

    /** Add/Edit dialog for a staff member. Pass null for a new staff member. */
    private class StaffForm {
        private final Staff existing;
        private final JTextField txtFirst = new JTextField(18);
        private final JTextField txtLast = new JTextField(18);
        private final JTextField txtEmail = new JTextField(18);
        private final JComboBox<String> cmbRole = new JComboBox<>(ROLES);
        private final JPasswordField txtPassword = new JPasswordField(18);

        StaffForm(Staff existing) {
            this.existing = existing;
            cmbRole.setEditable(true);
            if (existing != null) {
                txtFirst.setText(existing.getFirstName());
                txtLast.setText(existing.getLastName());
                txtEmail.setText(existing.getEmail());
                cmbRole.setSelectedItem(existing.getRole());
            }
        }

        String first() { return txtFirst.getText().trim(); }
        String last() { return txtLast.getText().trim(); }
        String email() { return txtEmail.getText().trim(); }
        String role() { return String.valueOf(cmbRole.getSelectedItem()).trim(); }
        String password() { return new String(txtPassword.getPassword()); }

        /** Shows the dialog repeatedly until the input is valid. Returns false if cancelled. */
        boolean prompt(String title) {
            JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
            p.add(new JLabel("First Name:"));
            p.add(txtFirst);
            p.add(new JLabel("Last Name:"));
            p.add(txtLast);
            p.add(new JLabel("Email:"));
            p.add(txtEmail);
            p.add(new JLabel("Role:"));
            p.add(cmbRole);
            p.add(new JLabel(existing == null ? "Password:" : "New Password:"));
            p.add(txtPassword);
            if (existing != null) {
                p.add(new JLabel());
                JLabel hint = new JLabel("(leave blank to keep current)");
                hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
                p.add(hint);
            }

            while (true) {
                int result = JOptionPane.showConfirmDialog(EmployeeAttendanceUI.this, p, title,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result != JOptionPane.OK_OPTION) return false;

                String error = validate();
                if (error == null) return true;
                JOptionPane.showMessageDialog(EmployeeAttendanceUI.this, error,
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
            }
        }

        private String validate() {
            if (first().isEmpty() || last().isEmpty()) return "First Name and Last Name are required.";
            if (!email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) return "Please enter a valid email address.";
            if (emailTaken(email(), existing == null ? -1 : existing.getStaffID()))
                return "That email is already used by another staff member.";
            if (role().isEmpty()) return "Role is required.";
            if (existing == null && password().isEmpty()) return "A password is required for new staff.";
            return null;
        }
    }

    // ================= ATTENDANCE: DATA + CRUD =================

    private Staff selectedPickerStaff() {
        Object sel = cmbStaffPicker.getSelectedItem();
        if (sel == null) return null;
        int id = Integer.parseInt(sel.toString().split(" - ")[0]);
        return findStaffById(id);
    }

    /** Finds today's open (not timed-out) attendance record for a staff member, if any. */
    private AttendanceRecord findOpenRecord(int staffId) {
        LocalDate today = LocalDate.now();
        for (AttendanceRecord r : attendance) {
            if (r.getStaffID() == staffId && r.getDate().equals(today) && r.getTimeOut() == null) {
                return r;
            }
        }
        return null;
    }

    /** CREATE */
    private void timeIn() {
        Staff s = selectedPickerStaff();
        if (s == null) return;
        if (findOpenRecord(s.getStaffID()) != null) {
            JOptionPane.showMessageDialog(this, s.getFullName() + " is already timed in today.",
                    "Already Clocked In", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LocalTime now = LocalTime.now().withNano(0);
        String status = now.isAfter(SHIFT_START.plusMinutes(10)) ? "Late" : "Present";
        AttendanceRecord rec = new AttendanceRecord(nextAttendanceId++, s.getStaffID(), s.getFullName(),
                LocalDate.now(), now, null, status);
        attendance.add(0, rec);
        refreshAttendanceTable();
    }

    /** UPDATE (clock out) */
    private void timeOut() {
        Staff s = selectedPickerStaff();
        if (s == null) return;
        AttendanceRecord rec = findOpenRecord(s.getStaffID());
        if (rec == null) {
            JOptionPane.showMessageDialog(this, s.getFullName() + " has not timed in today.",
                    "Not Clocked In", JOptionPane.WARNING_MESSAGE);
            return;
        }
        rec.setTimeOut(LocalTime.now().withNano(0));
        refreshAttendanceTable();
    }

    private int selectedAttendanceIndex() {
        int row = attendanceTable.getSelectedRow();
        if (row < 0) return -1;
        int id = ((Number) attendanceTableModel.getValueAt(row, 0)).intValue();
        for (int i = 0; i < attendance.size(); i++) {
            if (attendance.get(i).getAttendanceID() == id) return i;
        }
        return -1;
    }

    /** UPDATE (manual correction of time in / time out / status) */
    private void editAttendanceRecord() {
        int idx = selectedAttendanceIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select an attendance record from the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AttendanceRecord r = attendance.get(idx);

        JTextField txtIn = new JTextField(r.getTimeIn().format(TIME_FMT));
        JTextField txtOut = new JTextField(r.getTimeOut() == null ? "" : r.getTimeOut().format(TIME_FMT));
        JComboBox<String> cmbStatus = new JComboBox<>(STATUSES);
        cmbStatus.setSelectedItem(r.getStatus());

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Employee:"));
        p.add(new JLabel(r.getStaffName() + "  (" + r.getDate().format(DATE_FMT) + ")"));
        p.add(new JLabel("Time In (e.g. 08:05 AM):"));
        p.add(txtIn);
        p.add(new JLabel("Time Out (blank = still in):"));
        p.add(txtOut);
        p.add(new JLabel("Status:"));
        p.add(cmbStatus);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, p, "Edit Attendance #" + r.getAttendanceID(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return;

            try {
                LocalTime in = LocalTime.parse(txtIn.getText().trim(), TIME_PARSER);
                String outText = txtOut.getText().trim();
                LocalTime out = (outText.isEmpty() || outText.equals("-")) ? null : LocalTime.parse(outText, TIME_PARSER);
                if (out != null && out.isBefore(in)) {
                    JOptionPane.showMessageDialog(this, "Time Out cannot be earlier than Time In.",
                            "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                attendance.set(idx, new AttendanceRecord(r.getAttendanceID(), r.getStaffID(), r.getStaffName(),
                        r.getDate(), in, out, String.valueOf(cmbStatus.getSelectedItem())));
                refreshAttendanceTable();
                return;
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Please enter times like 08:05 AM or 5:30 PM.",
                        "Invalid Time", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /** DELETE */
    private void deleteAttendanceRecord() {
        int idx = selectedAttendanceIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select an attendance record from the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AttendanceRecord r = attendance.get(idx);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete the attendance record of " + r.getStaffName() + " on " + r.getDate().format(DATE_FMT) + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            attendance.remove(idx);
            refreshAttendanceTable();
        }
    }

    private void refreshAttendanceTable() {
        attendanceTableModel.setRowCount(0);
        for (AttendanceRecord r : attendance) {
            attendanceTableModel.addRow(new Object[]{
                    r.getAttendanceID(), r.getStaffID(), r.getStaffName(), r.getDate().format(DATE_FMT),
                    r.getTimeIn().format(TIME_FMT),
                    r.getTimeOut() == null ? "-" : r.getTimeOut().format(TIME_FMT),
                    r.getStatus()
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EmployeeAttendanceUI().setVisible(true));
    }
}
