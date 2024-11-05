import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

public class TimeTrackerApp {
    private static Timer timer;
    private static JTextField taskField;
    private static JLabel statusLabel;
    private static int timeInSeconds;
    private static int remainingTime;
    private static boolean isPaused = false;
    private static JButton startButton;
    private static JButton pauseButton;
    private static JButton stopButton;
    private static JTextField hoursField;
    private static JTextField minutesField;
    private static JTextField secondsField;

    public static void main(String[] args) {
        // Create frame
        JFrame frame = new JFrame("Time Tracking Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout(10, 10));

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(mainPanel, BorderLayout.CENTER);

        // Create input panel for task and time input
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Task input
        JLabel taskLabel = new JLabel("Task:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        inputPanel.add(taskLabel, gbc);

        taskField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        inputPanel.add(taskField, gbc);

        // Time input fields for hours, minutes, and seconds
        JLabel timeLabel = new JLabel("Time (h:m:s):");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.1;
        inputPanel.add(timeLabel, gbc);

        hoursField = new JTextField("0");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        inputPanel.add(hoursField, gbc);

        minutesField = new JTextField("45");
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        inputPanel.add(minutesField, gbc);

        secondsField = new JTextField("0");
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        inputPanel.add(secondsField, gbc);

        // Add input panel to main panel
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startButton = new JButton("Start Timer");
        buttonPanel.add(startButton);

        pauseButton = new JButton("Pause");
        pauseButton.setEnabled(false);
        buttonPanel.add(pauseButton);

        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        buttonPanel.add(stopButton);

        JButton viewButton = new JButton("View Saved Data");
        buttonPanel.add(viewButton);

        // Add button View Daily time spent
        JButton dailyViewButton = new JButton("View Daily Time Spent");
        buttonPanel.add(dailyViewButton);

        // Add button panel to main panel
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Status label for the timer display
        statusLabel = new JLabel("Status: Waiting to start");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Panel for status label
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        // Add status panel under button panel
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        // Start button action
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String taskName = taskField.getText();
                if (taskName.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a task name.");
                    return;
                }

                try {
                    int hours = Integer.parseInt(hoursField.getText());
                    int minutes = Integer.parseInt(minutesField.getText());
                    int seconds = Integer.parseInt(secondsField.getText());

                    if (hours < 0 || minutes < 0 || minutes >= 60 || seconds < 0 || seconds >= 60) {
                        JOptionPane.showMessageDialog(frame, "Please enter valid time values.");
                        return;
                    }

                    // Convert total time to seconds
                    timeInSeconds = (hours * 3600) + (minutes * 60) + seconds;

                    if (timeInSeconds == 0) {
                        JOptionPane.showMessageDialog(frame, "Total time cannot be zero.");
                        return;
                    }

                    remainingTime = timeInSeconds;
                    startTimer(taskName);

                    // Disable time input fields
                    hoursField.setEnabled(false);
                    minutesField.setEnabled(false);
                    secondsField.setEnabled(false);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid numbers for time.");
                }
            }
        });

        // Pause button action
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPaused) {
                    // Resume the timer
                    startTimer(taskField.getText());
                    pauseButton.setText("Pause");
                    isPaused = false;
                } else {
                    // Pause the timer
                    timer.stop();
                    pauseButton.setText("Resume");
                    isPaused = true;
                    statusLabel.setText("Timer paused at: " + formatTime(remainingTime));
                }
            }
        });

        // Stop button action
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer != null) {
                    timer.stop();
                }
                statusLabel.setText("Timer stopped.");
                saveTask(taskField.getText(), timeInSeconds - remainingTime); // Save with elapsed time
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                stopButton.setEnabled(false);

                // Re-enable time input fields
                hoursField.setEnabled(true);
                minutesField.setEnabled(true);
                secondsField.setEnabled(true);
            }
        });

        // View button action
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSavedData();
            }
        });

        // View Daily action
        dailyViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDailyTimeTasks();
            }
        });

        frame.setVisible(true);
    }

    private static void startTimer(String taskName) {
        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (remainingTime > 0) {
                    statusLabel.setText("Time left: " + formatTime(remainingTime));
                    remainingTime--;
                } else {
                    timer.stop();
                    statusLabel.setText("Time's up for task: " + taskName);
                    playSound();
                    saveTask(taskName, timeInSeconds); // Save full time if completed
                    startButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    stopButton.setEnabled(false);

                    // Re-enable time input fields
                    hoursField.setEnabled(true);
                    minutesField.setEnabled(true);
                    secondsField.setEnabled(true);
                }
            }
        });
        timer.start();

        // Update button states
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        stopButton.setEnabled(true);
    }

    private static String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }

    private static void playSound() {
        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                TimeTrackerApp.class.getResource("sound.wav"));
            clip.open(inputStream);
            clip.start();
        } catch (Exception e) {
            System.out.println("Failed to play sound: " + e.getMessage());
        }
    }

    private static void saveTask(String taskName, int elapsedTime) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String formattedTime = formatTime(elapsedTime);
        String entry = dtf.format(now) + " - Task: " + taskName + " - Duration: " + formattedTime + "\n";

        try (FileWriter writer = new FileWriter("time_tracking_log.txt", true)) {
            writer.write(entry);
            JOptionPane.showMessageDialog(null, "Task saved successfully!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Failed to save the task.");
        }
    }

    private static void showSavedData() {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("time_tracking_log.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            // Display the content in a scrollable text area
            JTextArea textArea = new JTextArea(content.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 200));
            JOptionPane.showMessageDialog(null, scrollPane, "Saved Time Logs", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "No data found or unable to read the file.");
        }
    }

    private static void showDailyTimeTasks() {
        StringBuilder content = new StringBuilder();
        Map<String, Integer> dailyTasks = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        try (BufferedReader reader = new BufferedReader(new FileReader("time_tracking_log.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" - ");
                if (parts.length >= 3) {
                    String datePart = parts[0];
                    String taskPart = parts[1].replace("Task: ", "");
                    String durationPart = parts[2].replace("Duration: ", "");

                    String taskDate = datePart.split(" ")[0];
                    if (taskDate.equals(dtf.format(now))) {
                        int durationInSeconds = convertDurationToSeconds(durationPart);
                        dailyTasks.put(taskPart, dailyTasks.getOrDefault(taskPart, 0) + durationInSeconds);
                    }
                }
            }

            if (dailyTasks.isEmpty()) {
                content.append("No tasks recorded for today.");
            } else {
                for (Map.Entry<String, Integer> entry : dailyTasks.entrySet()) {
                    content.append(entry.getKey()).append(": ").append(formatTime(entry.getValue())).append("\n");
                }
            }

            JTextArea textArea = new JTextArea(content.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 200));
            JOptionPane.showMessageDialog(null, scrollPane, "Daily Time Spent", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "No data found or unable to read the file.");
        }
    }

    private static int convertDurationToSeconds(String duration) {
        String[] durationParts = duration.split(", ");
        int totalSeconds = 0;
        for (String part : durationParts) {
            part = part.trim();
            if (part.endsWith("hours")) {
                totalSeconds += Integer.parseInt(part.split(" ")[0]) * 3600;
            } else if (part.endsWith("minutes")) {
                totalSeconds += Integer.parseInt(part.split(" ")[0]) * 60;
            } else if (part.endsWith("seconds")) {
                totalSeconds += Integer.parseInt(part.split(" ")[0]);
            }
        }
        return totalSeconds;
    }
}
