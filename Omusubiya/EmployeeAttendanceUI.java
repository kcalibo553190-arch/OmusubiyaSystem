import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * EMPLOYEE MANAGEMENT (ATTENDANCE)
 * Lists staff (from the `staff` table) and lets a manager log time-in /
 * time-out per employee per day. Attendance is kept in memory for now;
 * hook loadMockStaff()/attendance list to real DAO calls later.
 */
public class EmployeeAttendanceUI extends JFrame {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalTime SHIFT_START = LocalTime.of(8, 0);

    private final List<Staff> staffList = new ArrayList<>();
    private final List<AttendanceRecord> attendance = new ArrayList<>();
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
        setSize(1020, 660);
        setMinimumSize(new Dimension(900, 600));
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

    private JComponent buildStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(16, 16, 8, 16));

        JLabel title = new JLabel("Staff Directory");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.ACCENT_DARK);
        panel.add(title, BorderLayout.NORTH);

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
        cmbStaffPicker.setPreferredSize(new Dimension(220, 28));

        JButton btnTimeIn = UITheme.button("Time In");
        JButton btnTimeOut = UITheme.secondaryButton("Time Out");
        btnTimeIn.addActionListener(e -> timeIn());
        btnTimeOut.addActionListener(e -> timeOut());

        controls.add(new JLabel("Employee:"));
        controls.add(cmbStaffPicker);
        controls.add(btnTimeIn);
        controls.add(btnTimeOut);
        controls.add(Box.createRigidArea(new Dimension(16, 0)));
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

    // ---------- data actions ----------

    private void loadMockStaff() {
        staffList.add(new Staff(nextStaffId++, "Yuki", "Alvarez", "yuki.alvarez@riceball.ph", "hashed_pw", "Cashier"));
        staffList.add(new Staff(nextStaffId++, "Renzo", "Dela Cruz", "renzo.delacruz@riceball.ph", "hashed_pw", "Kitchen Staff"));
        staffList.add(new Staff(nextStaffId++, "Sakura", "Bautista", "sakura.bautista@riceball.ph", "hashed_pw", "Store Manager"));
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
        cmbStaffPicker.removeAllItems();
        for (Staff s : staffList) {
            cmbStaffPicker.addItem(s.getStaffID() + " - " + s.getFullName());
        }
    }

    private Staff selectedPickerStaff() {
        Object sel = cmbStaffPicker.getSelectedItem();
        if (sel == null) return null;
        int id = Integer.parseInt(sel.toString().split(" - ")[0]);
        for (Staff s : staffList) if (s.getStaffID() == id) return s;
        return null;
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
