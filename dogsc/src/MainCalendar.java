import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 메인 캘린더를 표시하는 JPanel입니다.
 */
public class MainCalendar extends JPanel {
    private Date currentDate;
    private Date currentStartDate;
    private JButton prevButton;
    private JButton nextButton;
    private JButton openCalendarButton;
    private JPanel calendarPanel;
    private CalendarDBConnection calendarDB;

    /**
     * MainCalendar 클래스의 생성자입니다.
     */
    public MainCalendar() {
        calendarDB = new CalendarDBConnection();
        setLayout(new BorderLayout());

        // 이번주의 첫날과 끝날을 가지고 초기화하기
        Calendar calendar = Calendar.getInstance();
        currentStartDate = getStartOfWeek(calendar.getTime());
        currentDate = currentStartDate; // Initialize currentDate

        //메인 캘린더 버튼 컨트롤러
        JPanel calendarControlPanel = new JPanel();

        // 캘린더 버튼 생성
        openCalendarButton = new JButton("CALENDAR");
        // 캘린더 버튼 작동
        openCalendarButton.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CalendarWindow();
            }
        }));

        // 이전 주 버튼
        prevButton = new JButton("<");
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPreviousWeek();
            }
        });

        // 다음 주 버튼
        nextButton = new JButton(">");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNextWeek();
            }
        });

        calendarControlPanel.add(prevButton);
        calendarControlPanel.add(openCalendarButton);
        calendarControlPanel.add(nextButton);

        // 캘린더 패널
        calendarPanel = new JPanel();
        calendarPanel.setLayout(new GridLayout(3, 7)); // 7일씩 나열

        updateCalendar(); // 캘린더 업데이트

        // 버튼과 캘린더 패널을 프레임에 추가
        add(calendarControlPanel, BorderLayout.NORTH);
        add(calendarPanel, BorderLayout.CENTER);
    }

    //초기화를 위한 이번 주 첫 날 가져오기
    private Date getStartOfWeek(Date startDate) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        return calendar.getTime();
    }

    //이전주 보여주기
    private void showPreviousWeek() {
        currentDate = getPreviousWeekStart(currentDate);
        currentStartDate = getPreviousWeekStart(currentStartDate);
        updateCalendar();
    }

    //다음주로 보여주기
    private void showNextWeek() {
        currentDate = getNextWeekStart(currentDate);
        currentStartDate = getNextWeekStart(currentStartDate);
        updateCalendar();
    }

    //메인페이지 캘린더 보이게 하기
    private void updateCalendar() {
        calendarPanel.removeAll();

        // 요일 표시
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : daysOfWeek) {
            JLabel dayLabel = new JLabel(day, JLabel.CENTER);
            if ("Sun".equals(day)){
                dayLabel.setForeground(Color.RED);
            } else if ("Sat".equals(day)) {
                dayLabel.setForeground(Color.BLUE);
            }
            calendarPanel.add(dayLabel);
        }

        // 현재 주 표시
        List<Date> weekDates = getWeekDates(currentDate);
        for (Date date : weekDates) {
            JPanel datePanel = new JPanel(new BorderLayout());

            JLabel dateLabel = new JLabel(new SimpleDateFormat("MM-dd").format(date), JLabel.LEFT);
            dateLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add border for better visibility

            if (isToday(date)) {
                dateLabel.setBackground(Color.PINK); // 오늘 날짜에 PINK로 하이라이트 하기
                dateLabel.setOpaque(true);
            }

            datePanel.add(dateLabel, BorderLayout.NORTH);

            JLabel scheduleLabel = new JLabel(getScheduleText(date), JLabel.CENTER);
            scheduleLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add border for better visibility
            datePanel.add(scheduleLabel, BorderLayout.CENTER);

            calendarPanel.add(datePanel);
        }

        // 다음 주 표시
        List<Date> nextWeekDates = getWeekDates(getNextWeekStart(currentStartDate));
        for (Date date : nextWeekDates) {
            JPanel datePanel = new JPanel(new BorderLayout());

            JLabel dateLabel = new JLabel(new SimpleDateFormat("MM-dd").format(date), JLabel.LEFT);
            dateLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add border for better visibility

            if (isToday(date)) {
                dateLabel.setBackground(Color.PINK); // 오늘 날짜에 PINK로 하이라이트 하기
                dateLabel.setOpaque(true);
            }

            datePanel.add(dateLabel, BorderLayout.NORTH);

            JLabel scheduleLabel = new JLabel(getScheduleText(date), JLabel.CENTER);
            scheduleLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add border for better visibility
            datePanel.add(scheduleLabel, BorderLayout.CENTER);
            calendarPanel.add(datePanel);
        }
        // UI 업데이트
        revalidate();
        repaint();
    }

    //날짜에 맞춰 스케쥴 보여주기
    private String getScheduleText(Date date) {
        List<String> schedules = calendarDB.getSchedulesForDate(date);
        StringBuilder message = new StringBuilder();
        for (String schedule : schedules) {
            message.append("- ").append(schedule).append("<br>");
        }
        return "<html>" + message + "</html>";
    }

    //이번주 날짜 가져오기
    private List<Date> getWeekDates(Date startDate) {
        // 주의 시작일을 기준으로 7일간의 일자를 가져옴
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // 주의 시작일로 설정
        Date current = calendar.getTime();

        // 7일간의 일자를 리스트에 추가
        List<Date> weekDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDates.add(current);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            current = calendar.getTime();
        }
        return weekDates;
    }

    //이전주 날짜 가져오기
    private Date getPreviousWeekStart(Date startDate) {
        // 이전 주의 시작일을 가져옴
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, -7); // 7일 전으로 이동
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // 주의 시작일로 설정
        return calendar.getTime();
    }

    //다음주 날짜 가져오기
    private Date getNextWeekStart(Date startDate) {
        // 다음 주의 시작일을 가져옴
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, 7); // 7일 후로 이동
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // 주의 시작일로 설정
        return calendar.getTime();
    }

    //오늘 날짜에 색칠하기 위해서 오늘 날짜인지 판별하는 메서드
    private boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH);
    }
}


