import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Priority {

    private static class PriorityOutput {
        Process[] processes;
        String[] gantt;
        int ganttSize;
        int totalDuration;
    }

    public JPanel getUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        Color headerBg = new Color(245, 228, 215);
        Color accent = new Color(64, 51, 79);

        JLabel title = new JLabel("Priority (Non-Preemptive)", SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(headerBg);
        title.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel inputArea = new JPanel();
        inputArea.setLayout(new BoxLayout(inputArea, BoxLayout.Y_AXIS));
        inputArea.setBorder(BorderFactory.createTitledBorder("Input"));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Color.WHITE);
        top.add(new JLabel("Number of processes:"));
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 50, 1);
        JSpinner numSpinner = new JSpinner(model);
        top.add(numSpinner);
        JButton createBtn = new JButton("Create");
        createBtn.setBackground(accent);
        createBtn.setForeground(Color.WHITE);
        top.add(createBtn);
        inputArea.add(top);

        JPanel rowsContainer = new JPanel(new BorderLayout());
        rowsContainer.setPreferredSize(new Dimension(360, 260));
        inputArea.add(Box.createRigidArea(new Dimension(0,8)));
        inputArea.add(new JScrollPane(rowsContainer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton runBtn = new JButton("Run Priority");
        runBtn.setEnabled(false);
        runBtn.setBackground(accent);
        runBtn.setForeground(Color.WHITE);
        actionRow.add(runBtn);
        inputArea.add(Box.createRigidArea(new Dimension(0,8)));
        inputArea.add(actionRow);


        JPanel resultArea = new JPanel(new BorderLayout(8,8));
        resultArea.setBorder(BorderFactory.createTitledBorder("Result"));
        JLabel hint = new JLabel("<html>Fill inputs on the left and click <b>Run Priority</b>.</html>");
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        resultArea.add(hint, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputArea, resultArea);
        split.setResizeWeight(0.40);
        mainPanel.add(split, BorderLayout.CENTER);

        final JTextField[][] fieldsRef = new JTextField[1][];

        createBtn.addActionListener(e -> {
            int n = (Integer) numSpinner.getValue();
            JPanel rows = new JPanel();
            rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
            rows.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

            JTextField[][] inputs = new JTextField[n][3];

            for (int i = 0; i < n; i++) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.setBackground(Color.WHITE);
                JLabel lbl = new JLabel("P" + (i + 1));
                lbl.setPreferredSize(new Dimension(40,20));
                row.add(lbl);

                row.add(new JLabel("AT:"));
                JTextField at = new JTextField(6);
                inputs[i][0] = at;
                row.add(at);

                row.add(Box.createRigidArea(new Dimension(8,0)));
                row.add(new JLabel("BT:"));
                JTextField bt = new JTextField(6);
                inputs[i][1] = bt;
                row.add(bt);

                row.add(Box.createRigidArea(new Dimension(8,0)));
                row.add(new JLabel("PRIO:"));
                JTextField pr = new JTextField(5);
                inputs[i][2] = pr;
                row.add(pr);

                rows.add(row);
            }

            rowsContainer.removeAll();
            rowsContainer.add(rows, BorderLayout.NORTH);
            rowsContainer.revalidate();
            rowsContainer.repaint();

            fieldsRef[0] = new JTextField[n * 3];
            for (int i = 0; i < n; i++) {
                fieldsRef[0][i * 3] = inputs[i][0];
                fieldsRef[0][i * 3 + 1] = inputs[i][1];
                fieldsRef[0][i * 3 + 2] = inputs[i][2];
            }

            runBtn.setEnabled(true);
        });

        runBtn.addActionListener(e -> {
            int n = (Integer) numSpinner.getValue();
            JTextField[] fields = fieldsRef[0];
            if (fields == null || fields.length < n * 3) {
                JOptionPane.showMessageDialog(mainPanel, "Please click Create and fill all fields.");
                return;
            }

            String[] pid = new String[n];
            int[] arrivalTime = new int[n];
            int[] burstTime = new int[n];
            int[] prio = new int[n];

            try {
                for (int i = 0; i < n; i++) {
                    pid[i] = "P" + (i + 1);
                    String atText = fields[i * 3].getText().trim();
                    String btText = fields[i * 3 + 1].getText().trim();
                    String prText = fields[i * 3 + 2].getText().trim();
                    arrivalTime[i] = Integer.parseInt(atText);
                    burstTime[i] = Integer.parseInt(btText);
                    prio[i] = Integer.parseInt(prText);
                    if (arrivalTime[i] < 0 || burstTime[i] < 0) {
                        throw new NumberFormatException();
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Please enter valid non-negative integers for all fields.");
                return;
            }

            Process[] p = new Process[n];
            for (int i = 0; i < n; i++) {
                p[i] = new Process(pid[i], arrivalTime[i], burstTime[i], prio[i]);
            }

            PriorityOutput out = computePriority(p, n);

            resultArea.removeAll();

            String[] cols = {"PID", "AT", "BT", "PRIO", "CT", "TAT", "WT"};
            String[][] data = new String[out.processes.length][7];
            double totalTAT = 0, totalWT = 0;
            for (int i = 0; i < out.processes.length; i++) {
                Process prc = out.processes[i];
                data[i][0] = prc.pid;
                data[i][1] = String.valueOf(prc.arrivalTime);
                data[i][2] = String.valueOf(prc.burstTime);
                data[i][3] = String.valueOf(prc.priority);
                data[i][4] = String.valueOf(prc.completionTime);
                data[i][5] = String.valueOf(prc.turnaroundTime());
                data[i][6] = String.valueOf(prc.waitingTime());
                totalTAT += prc.turnaroundTime();
                totalWT += prc.waitingTime();
            }

            JTable table = new JTable(data, cols);
            table.setFillsViewportHeight(true);
            table.setPreferredScrollableViewportSize(new Dimension(520, 120));
            JScrollPane tableScroll = new JScrollPane(table);
            resultArea.add(tableScroll, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setBackground(Color.WHITE);

            JPanel ganttOuter = new JPanel(new BorderLayout());
            ganttOuter.setBorder(BorderFactory.createTitledBorder("Gantt Chart"));

            int totalUnits = 0;
            for (int i = 0; i < out.ganttSize; i++) {
                String block = out.gantt[i];
                int len = Integer.parseInt(block.substring(block.indexOf("(") + 1, block.indexOf(")")));
                totalUnits += len;
            }
            final int totalUnitsFinal = Math.max(1, totalUnits);

            JPanel ganttPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    int paddingLeft = 25;
                    int y = 30;
                    int height = 40;
                    int x = paddingLeft;

                    Color[] pastel = {
                        new Color(255, 179, 186),
                        new Color(255, 223, 186),
                        new Color(255, 255, 186),
                        new Color(186, 255, 201),
                        new Color(186, 225, 255)
                    };

                    int unitPx = 28;
                    int fullWidth = paddingLeft + totalUnitsFinal * unitPx + 40;
                    g.setColor(Color.WHITE);
                    g.fillRect(0,0,getWidth(), getHeight());

                    for (int i = 0; i < out.ganttSize; i++) {
                        String block = out.gantt[i];
                        String pid = block.substring(0, block.indexOf("("));
                        int len = Integer.parseInt(block.substring(block.indexOf("(") + 1, block.indexOf(")")));

                        g.setColor(pastel[i % pastel.length]);
                        int blockWidth = len * unitPx;
                        g.fillRect(x, y, blockWidth, height);

                        g.setColor(Color.DARK_GRAY);
                        g.drawRect(x, y, blockWidth, height);

                        g.setColor(Color.BLACK);
                        g.drawString(pid, x + 6, y + 25);

                        x += blockWidth;
                    }

                    int tickX = paddingLeft;
                    g.setColor(Color.BLACK);
                    g.setFont(g.getFont().deriveFont(Font.PLAIN, 12f));

                    int currentTime = 0;
                    g.drawString(String.valueOf(0), tickX - 6, y + height + 20);

                    for (int i = 0; i < out.ganttSize; i++) {
                        String block = out.gantt[i];
                        int len = Integer.parseInt(block.substring(block.indexOf("(") + 1, block.indexOf(")")));
                        tickX += len * unitPx;
                        currentTime += len;
                        g.drawString(String.valueOf(currentTime), tickX - 6, y + height + 20);
                    }

                    setPreferredSize(new Dimension(Math.max(getWidth(), paddingLeft + totalUnitsFinal * unitPx + 80), 120));
                }
            };

            int unitPx = 28;
            int prefWidth = Math.max(520, 40 + totalUnitsFinal * unitPx);
            ganttPanel.setPreferredSize(new Dimension(prefWidth, 140));

            JScrollPane ganttScroll = new JScrollPane(ganttPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            ganttOuter.add(ganttScroll, BorderLayout.CENTER);

            centerPanel.add(ganttOuter, BorderLayout.CENTER);

            JPanel statsPanel = new JPanel();
            statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
            statsPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

            JLabel totalTATLabel = new JLabel("Total Turnaround Time: " + (int) totalTAT);
            totalTATLabel.setFont(totalTATLabel.getFont().deriveFont(Font.PLAIN, 12f));
            JLabel totalWTLabel = new JLabel("Total Waiting Time: " + (int) totalWT);
            totalWTLabel.setFont(totalWTLabel.getFont().deriveFont(Font.PLAIN, 12f));

            double avgTAT = totalTAT / out.processes.length;
            double avgWT = totalWT / out.processes.length;

            JLabel avgTATLabel = new JLabel(String.format("Average Turnaround Time: %.2f ms", avgTAT));
            avgTATLabel.setFont(avgTATLabel.getFont().deriveFont(Font.PLAIN, 12f));
            JLabel avgWTLabel = new JLabel(String.format("Average Waiting Time: %.2f ms", avgWT));
            avgWTLabel.setFont(avgWTLabel.getFont().deriveFont(Font.PLAIN, 12f));

            statsPanel.add(totalTATLabel);
            statsPanel.add(Box.createRigidArea(new Dimension(0,6)));
            statsPanel.add(totalWTLabel);
            statsPanel.add(Box.createRigidArea(new Dimension(0,12)));
            statsPanel.add(avgTATLabel);
            statsPanel.add(Box.createRigidArea(new Dimension(0,6)));
            statsPanel.add(avgWTLabel);

            centerPanel.add(statsPanel, BorderLayout.SOUTH);

            resultArea.add(centerPanel, BorderLayout.CENTER);

            resultArea.revalidate();
            resultArea.repaint();
        });

        return mainPanel;
    }

    private PriorityOutput computePriority(Process[] p, int n) {

        //same lang to kay sir sanidad glenn pwede na di mo na palitan muehehe
        //pero kung may gawa ka g lang di pa naman optimize to
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (p[j].arrivalTime > p[j + 1].arrivalTime) {
                    Process temp = p[j];
                    p[j] = p[j + 1];
                    p[j + 1] = temp;
                }
            }
        }

        int time = 0;
        int completed = 0;

        String[] gantt = new String[n];
        int gIndex = 0;

        boolean done[] = new boolean[n];

        while (completed < n) {

            int chosen = -1;

            for (int i = 0; i < n; i++) {

                if (!done[i] && p[i].arrivalTime <= time) {

                    if (chosen == -1) {
                        chosen = i;
                    } else if (p[i].priority < p[chosen].priority) {
                        chosen = i;
                    } else if (p[i].priority == p[chosen].priority) {
                        if (p[i].burstTime < p[chosen].burstTime)
                            chosen = i;
                        else if (p[i].burstTime == p[chosen].burstTime) {
                            if (p[i].arrivalTime < p[chosen].arrivalTime)
                                chosen = i;
                            else if (p[i].arrivalTime == p[chosen].arrivalTime) {
                                if (p[i].pid.compareTo(p[chosen].pid) < 0)
                                    chosen = i;
                            }
                        }
                    }
                }
            }

            if (chosen == -1) {
                time++;
                continue;
            }

            if (p[chosen].start == -1)
                p[chosen].start = time;

            time += p[chosen].burstTime;
            p[chosen].completionTime = time;
            done[chosen] = true;
            completed++;

            gantt[gIndex++] = p[chosen].pid + "(" + p[chosen].burstTime + ")";
        }

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (p[j].pid.compareTo(p[j + 1].pid) > 0) {
                    Process temp = p[j];
                    p[j] = p[j + 1];
                    p[j + 1] = temp;
                }
            }
        }

        PriorityOutput out = new PriorityOutput();
        out.gantt = new String[gIndex];
        for (int i = 0; i < gIndex; i++) out.gantt[i] = gantt[i];
        out.ganttSize = gIndex;
        out.processes = p;

        int total = 0;
        for (int i = 0; i < n; i++) total += p[i].completionTime;
        out.totalDuration = total;

        return out;
    }
}
