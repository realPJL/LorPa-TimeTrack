import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        frame.setSize(600, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        // Task input
        JLabel taskLabel = new JLabel("Task:");
        taskLabel.setBounds(10, 10, 100, 25);
        frame.add(taskLabel);

        taskField = new JTextField();
        taskField.setBounds(100, 10, 200, 25);
        frame.add(taskField);

        // Time input fields for hours, minutes, and seconds
        JLabel timeLabel = new JLabel("Time (h:m:s):");
        timeLabel.setBounds(10, 40, 100, 25);
        frame.add(timeLabel);

        hoursField = new JTextField("0");
        hoursField.setBounds(100, 40, 50, 25);
        frame.add(hoursField);

        minutesField = new JTextField("45");
        minutesField.setBounds(160, 40, 50, 25);
        frame.add(minutesField);

        secondsField = new JTextField("0");
        secondsField.setBounds(220, 40, 50, 25);
        frame.add(secondsField);

        // Start button
        startButton = new JButton("Start Timer");
        startButton.setBounds(10, 70, 120, 25);
        frame.add(startButton);

        // Pause button
        pauseButton = new JButton("Pause");
        pauseButton.setBounds(140, 70, 120, 25);
        pauseButton.setEnabled(false);
        frame.add(pauseButton);

        // Stop button
        stopButton = new JButton("Stop");
        stopButton.setBounds(270, 70, 120, 25);
        stopButton.setEnabled(false);
        frame.add(stopButton);

        // View Data button
        JButton viewButton = new JButton("View Saved Data");
        viewButton.setBounds(10, 110, 150, 25);
        frame.add(viewButton);

        // Status label
        statusLabel = new JLabel("Status: Waiting to start");
        statusLabel.setBounds(10, 150, 400, 25);
        frame.add(statusLabel);

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
            scrollPane.setPreferredSize(new java.awt.Dimension(600, 200));
            JOptionPane.showMessageDialog(null, scrollPane, "Saved Time Logs", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "No data found or unable to read the file.");
        }
    }
}
