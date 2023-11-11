import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendarWindow extends JFrame {
    private Calendar calendar;
    private JLabel monthLabel;
    private JPanel calendarPanel;
    private JButton prevButton, nextButton;
    private final String[] dayNames = {"일", "월", "화", "수", "목", "금", "토"};
    private Map<Integer, List<String>> dailySchedules = new HashMap<>(); // 각 날짜별 일정을 저장하는 맵

    public CalendarWindow() {
        calendar = Calendar.getInstance(Locale.getDefault());
        setTitle("캘린더");
        setSize(800, 1000);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel monthPanel = new JPanel();
        monthPanel.setBorder(BorderFactory.createLineBorder(Color.white, 1));
        monthPanel.setLayout(new BoxLayout(monthPanel, BoxLayout.X_AXIS));

        prevButton = new JButton("<");
        prevButton.addActionListener(e -> navigateMonths(-1));
        nextButton = new JButton(">");
        nextButton.addActionListener(e -> navigateMonths(1));

        monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 24));

        monthPanel.add(Box.createHorizontalGlue());
        monthPanel.add(prevButton);
        monthPanel.add(Box.createHorizontalStrut(10));
        monthPanel.add(monthLabel);
        monthPanel.add(Box.createHorizontalStrut(10));
        monthPanel.add(nextButton);
        monthPanel.add(Box.createHorizontalGlue());

        add(monthPanel, BorderLayout.NORTH);

        calendarPanel = new JPanel();
        calendarPanel.setLayout(new BorderLayout());
        add(calendarPanel, BorderLayout.CENTER);

        JPanel headerPanel = new JPanel(new GridLayout(1, 7, 5, 5));
        for (String dayName : dayNames) {
            JLabel dayLabel = new JLabel(dayName, JLabel.CENTER);
            if ("일".equals(dayName)) {
                dayLabel.setForeground(Color.RED);
            } else if ("토".equals(dayName)) {
                dayLabel.setForeground(Color.BLUE);
            }
            headerPanel.add(dayLabel);
        }
        calendarPanel.add(headerPanel, BorderLayout.NORTH);

        updateCalendar();
        setVisible(true);
    }

    private void navigateMonths(int delta) {
        calendar.add(Calendar.MONTH, delta);
        updateCalendar();
    }

    private void updateCalendar() {
        JPanel daysPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        daysPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        calendarPanel.add(daysPanel, BorderLayout.CENTER);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MMMM", Locale.KOREA);
        monthLabel.setText(sdf.format(calendar.getTime()));

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i < startDayOfWeek; i++) {
            daysPanel.add(new JLabel(""));
        }

        for (int day = 1; day <= maxDay; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setHorizontalAlignment(SwingConstants.LEFT);
            dayButton.setVerticalAlignment(SwingConstants.TOP);

            final int finalDay = day;
            dayButton.addActionListener(e -> showDateWindow(finalDay, dayButton));

            if ((day + startDayOfWeek - 1) % 7 == 0) {
                dayButton.setForeground(Color.BLUE);
            } else if ((day + startDayOfWeek - 1) % 7 == 1) {
                dayButton.setForeground(Color.RED);
            }

            if (dailySchedules.containsKey(day)) {
                String scheduleText = dailySchedules.get(day).stream()
                        .collect(Collectors.joining(", "));
                dayButton.setText(day + " (" + scheduleText + ")");
            }

            daysPanel.add(dayButton);
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private void showDateWindow(int day, JButton dayButton) {
        DateDetailDialog dateDetailDialog = new DateDetailDialog(this, "날짜 정보", true, day, dayButton);
        dateDetailDialog.setVisible(true);
    }

    public void updateDayButton(int day, List<String> schedules) {
        dailySchedules.put(day, schedules);
        updateCalendar(); // 캘린더를 다시 그려서 버튼에 일정을 표시
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalendarWindow());
    }
}
