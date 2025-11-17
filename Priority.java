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

    public void prioResult() {

        JFrame inputFrame = new JFrame("Priority Scheduling Input");
        inputFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inputFrame.setSize(460, 540);
        inputFrame.setLocationRelativeTo(null);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Number of processes:"));
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 50, 1);
        JSpinner numSpinner = new JSpinner(model);
        top.add(numSpinner);
        JButton createBtn = new JButton("Create");
        top.add(createBtn);
        main.add(top);

        JPanel rowsContainer = new JPanel();
        rowsContainer.setLayout(new BorderLayout());
        main.add(Box.createRigidArea(new Dimension(0, 8)));
        main.add(new JScrollPane(rowsContainer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton runBtn = new JButton("Run Priority (Non-Preemptive)");
        runBtn.setEnabled(false);
        actionRow.add(runBtn);
        main.add(Box.createRigidArea(new Dimension(0, 8)));
        main.add(actionRow);

        inputFrame.add(main);
        inputFrame.setVisible(true);

        final JTextField[][] fieldsRef = new JTextField[1][];

        createBtn.addActionListener(e -> {
            int n = (Integer) numSpinner.getValue();
            JPanel rows = new JPanel();
            rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
            rows.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            JTextField[][] inputs = new JTextField[n][3];

            for (int i = 0; i < n; i++) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel lbl = new JLabel("P" + (i + 1));
                lbl.setPreferredSize(new Dimension(40, 20));
                row.add(lbl);

                row.add(new JLabel("AT:"));
                JTextField at = new JTextField(6);
                inputs[i][0] = at;
                row.add(at);

                row.add(Box.createRigidArea(new Dimension(8, 0)));
                row.add(new JLabel("BT:"));
                JTextField bt = new JTextField(6);
                inputs[i][1] = bt;
                row.add(bt);

                row.add(Box.createRigidArea(new Dimension(8, 0)));
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
                JOptionPane.showMessageDialog(inputFrame, "Please click Create and fill all fields.");
                return;
            }

            String[] pid = new String[n];
            int[] arrivalTime = new int[n];
            int[] burstTime = new int[n];
            int[] prio = new int[n];

            try {
                for (int i = 0; i < n; i++) {
                    pid[i] = "P" + (i + 1);
                    arrivalTime[i] = Integer.parseInt(fields[i * 3].getText().trim());
                    burstTime[i] = Integer.parseInt(fields[i * 3 + 1].getText().trim());
                    prio[i] = Integer.parseInt(fields[i * 3 + 2].getText().trim());
                    if (arrivalTime[i] < 0 || burstTime[i] < 0) throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(inputFrame, "Please enter valid non-negative integers for all fields.");
                return;
            }

            Process[] p = new Process[n];
            for (int i = 0; i < n; i++) {
                p[i] = new Process(pid[i], arrivalTime[i], burstTime[i], prio[i]);
            }

            PriorityOutput out = computePriority(p, n);
            inputFrame.dispose();
            showUI(out);
        });
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

    private void showUI(PriorityOutput out) {

        JFrame frame = new JFrame("Priority (Non-Preemptive) Result");
        frame.setSize(900, 700);
        frame.setLayout(new BorderLayout(8, 8));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] cols = {"PID", "AT", "BT", "PRIO", "CT", "TAT", "WT"};
        String[][] data = new String[out.processes.length][7];

        for (int i = 0; i < out.processes.length; i++) {
            Process p = out.processes[i];
            data[i][0] = p.pid;
            data[i][1] = String.valueOf(p.arrivalTime);
            data[i][2] = String.valueOf(p.burstTime);
            data[i][3] = String.valueOf(p.priority);
            data[i][4] = String.valueOf(p.completionTime);
            data[i][5] = String.valueOf(p.turnaroundTime());
            data[i][6] = String.valueOf(p.waitingTime());
        }

        JTable table = new JTable(data, cols);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(860, 150));
        JScrollPane tableScroll = new JScrollPane(table);
        frame.add(tableScroll, BorderLayout.NORTH);

        JPanel ganttOuter = new JPanel();
        ganttOuter.setLayout(new BorderLayout());
        ganttOuter.setBorder(BorderFactory.createTitledBorder("Gantt Chart"));

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

                for (int i = 0; i < out.ganttSize; i++) {
                    String block = out.gantt[i];
                    String pid = block.substring(0, block.indexOf("("));
                    int len = Integer.parseInt(block.substring(block.indexOf("(") + 1, block.indexOf(")")));

                    g.setColor(pastel[i % pastel.length]);
                    int blockWidth = len * 28;
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
                    tickX += len * 28;
                    currentTime += len;
                    g.drawString(String.valueOf(currentTime), tickX - 6, y + height + 20);
                }
            }
        };

        ganttPanel.setPreferredSize(new Dimension(860, 350));
        ganttOuter.add(new JScrollPane(ganttPanel), BorderLayout.CENTER);
        frame.add(ganttOuter, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
