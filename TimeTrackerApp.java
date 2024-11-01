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

    public static void main(String[] args) {
        // Create frame
        JFrame frame = new JFrame("Time Tracking Tool");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        // Task input
        JLabel taskLabel = new JLabel("Task:");
        taskLabel.setBounds(10, 10, 100, 25);
        frame.add(taskLabel);

        taskField = new JTextField();
        taskField.setBounds(100, 10, 200, 25);
        frame.add(taskField);

        // Timer input (in seconds)
        JLabel timeLabel = new JLabel("Time (sec):");
        timeLabel.setBounds(10, 40, 100, 25);
        frame.add(timeLabel);

        JTextField timeField = new JTextField();
        timeField.setBounds(100, 40, 200, 25);
        frame.add(timeField);

        // Start button
        JButton startButton = new JButton("Start Timer");
        startButton.setBounds(10, 70, 120, 25);
        frame.add(startButton);

        // View Data button
        JButton viewButton = new JButton("View Saved Data");
        viewButton.setBounds(150, 70, 150, 25);
        frame.add(viewButton);

        // Status label
        statusLabel = new JLabel("Status: Waiting to start");
        statusLabel.setBounds(10, 100, 300, 25);
        frame.add(statusLabel);

        // Action on start button click
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String taskName = taskField.getText();
                String timeText = timeField.getText();
                if (taskName.isEmpty() || timeText.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter both task and time.");
                    return;
                }
                try {
                    timeInSeconds = Integer.parseInt(timeText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid number for time.");
                    return;
                }
                startTimer(taskName);
            }
        });

        // Action on view button click
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
            int countdown = timeInSeconds;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (countdown > 0) {
                    statusLabel.setText("Time left: " + countdown + " seconds");
                    countdown--;
                } else {
                    timer.stop();
                    statusLabel.setText("Time's up for task: " + taskName);
                    playSound();
                    saveTask(taskName);
                }
            }
        });
        timer.start();
    }

    private static void playSound() {
        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                TimeTrackerApp.class.getResource("/sound/alert.wav"));
            clip.open(inputStream);
            clip.start();
        } catch (Exception e) {
            System.out.println("Failed to play sound: " + e.getMessage());
        }
    }

    private static void saveTask(String taskName) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String entry = dtf.format(now) + " - Task: " + taskName + " - Duration: " + timeInSeconds + " seconds\n";

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
            scrollPane.setPreferredSize(new java.awt.Dimension(400, 200));
            JOptionPane.showMessageDialog(null, scrollPane, "Saved Time Logs", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "No data found or unable to read the file.");
        }
    }
}
