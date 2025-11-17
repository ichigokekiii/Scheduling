import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class Skan {

    public JPanel getUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JLabel title = new JLabel("SCAN Disk Scheduling", SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(new Color(245, 228, 215));
        title.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel inputArea = new JPanel();
        inputArea.setLayout(new BoxLayout(inputArea, BoxLayout.Y_AXIS));
        inputArea.setBorder(BorderFactory.createTitledBorder("Input"));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Color.WHITE);
        top.add(new JLabel("Number of requests:"));
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 50, 1);
        JSpinner numSpinner = new JSpinner(model);
        top.add(numSpinner);
        JButton createBtn = new JButton("Create");
        createBtn.setBackground(new Color(64,51,79));
        createBtn.setForeground(Color.WHITE);
        top.add(createBtn);
        inputArea.add(top);

        JPanel rowsContainer = new JPanel(new BorderLayout());
        rowsContainer.setPreferredSize(new Dimension(320, 240));
        inputArea.add(Box.createRigidArea(new Dimension(0,8)));
        inputArea.add(new JScrollPane(rowsContainer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        JPanel headRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headRow.setBackground(Color.WHITE);
        headRow.add(new JLabel("Enter head position:"));
        JTextField headField = new JTextField(6);
        headRow.add(headField);
        inputArea.add(Box.createRigidArea(new Dimension(0,8)));
        inputArea.add(headRow);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton runBtn = new JButton("Run SCAN");
        runBtn.setEnabled(false);
        runBtn.setBackground(new Color(64,51,79));
        runBtn.setForeground(Color.WHITE);
        actionRow.add(runBtn);
        inputArea.add(Box.createRigidArea(new Dimension(0,8)));
        inputArea.add(actionRow);

        JPanel resultArea = new JPanel(new BorderLayout(8,8));
        resultArea.setBorder(BorderFactory.createTitledBorder("Result"));
        JLabel hint = new JLabel("<html>Fill inputs on the left and click <b>Run SCAN</b>.</html>");
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

            JTextField[] inputs = new JTextField[n];

            for (int i = 0; i < n; i++) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.setBackground(Color.WHITE);
                JLabel lbl = new JLabel("Request " + (i + 1) + ":");
                lbl.setPreferredSize(new Dimension(90, 20));
                row.add(lbl);

                JTextField track = new JTextField(8);
                inputs[i] = track;
                row.add(track);

                rows.add(row);
            }

            rowsContainer.removeAll();
            rowsContainer.add(rows, BorderLayout.NORTH);
            rowsContainer.revalidate();
            rowsContainer.repaint();

            fieldsRef[0] = new JTextField[n];
            for (int i = 0; i < n; i++) fieldsRef[0][i] = inputs[i];

            runBtn.setEnabled(true);
        });

        runBtn.addActionListener(e -> {
            int n = (Integer) numSpinner.getValue();
            JTextField[] fields = fieldsRef[0];
            if (fields == null || fields.length < n) {
                JOptionPane.showMessageDialog(mainPanel, "Please click Create and fill all fields.");
                return;
            }

            int head;
            try {
                head = Integer.parseInt(headField.getText().trim());
                if (head < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Please enter a valid non-negative integer for head.");
                return;
            }

            DiskRequest[] req = new DiskRequest[n];
            try {
                for (int i = 0; i < n; i++) {
                    int tr = Integer.parseInt(fields[i].getText().trim());
                    req[i] = new DiskRequest(tr);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Please enter valid integer track numbers for all requests.");
                return;
            }

            DiskRequest[] reqCopy = new DiskRequest[n];
            for (int i = 0; i < n; i++) reqCopy[i] = new DiskRequest(req[i].track);

            computeSCAN(reqCopy, n, head);

            SCANResultData result = buildResultFromCompute(reqCopy, n, head);

            resultArea.removeAll();

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(Color.WHITE);

            String[] cols = {"Track", "Movement", "Served"};
            String[][] data = new String[result.req.length][3];
            for (int i = 0; i < result.req.length; i++) {
                data[i][0] = String.valueOf(result.req[i].track);
                data[i][1] = String.valueOf(result.req[i].movement);
                data[i][2] = String.valueOf(result.req[i].served);
            }

            JTable table = new JTable(data, cols);
            table.setFillsViewportHeight(false);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            JScrollPane tableScroll = new JScrollPane(table,
                    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            int tableHeight = (result.req.length + 1) * table.getRowHeight() + 5;
            tableScroll.setPreferredSize(new Dimension(520, tableHeight));
            tableScroll.setMinimumSize(new Dimension(520, tableHeight));
            tableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, tableHeight));
            contentPanel.add(tableScroll);
            contentPanel.add(Box.createRigidArea(new Dimension(0,5)));

            JPanel graphPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    int left = 60;
                    int right = getWidth() - 60;
                    int top = 40;
                    int bottom = getHeight() - 40;

                    g.setColor(new Color(230,230,230));
                    g.fillRect(left, top, Math.max(1, right - left), Math.max(1, bottom - top));

                    g.setColor(new Color(200, 200, 200));
                    g.drawRect(left, top, Math.max(1, right - left), Math.max(1, bottom - top));

                    g.setFont(getFont().deriveFont(12f));
                    g.setColor(Color.BLACK);

                    int yMax = 199;
                    int yMin = 0;
                    for (int t = 0; t <= 4; t++) {
                        int yy = top + (int)((double)(4 - t) / 4.0 * (bottom - top));
                        int tickVal = yMin + t * (yMax - yMin) / 4;
                        g.drawLine(left - 6, yy, left, yy);
                        g.drawString(String.valueOf(tickVal), left - 44, yy + 4);
                    }

                    int pts = result.plottedCount;
                    if (pts < 2) return;

                    int[] xs = new int[pts];
                    int[] ys = new int[pts];

                    int drawWidth = Math.max(1, getWidth() - (left + 60));
                    int drawLeft = left;
                    for (int i = 0; i < pts; i++) {
                        xs[i] = drawLeft + (int)((double)i / (pts - 1) * drawWidth);
                        int track = result.plottedOrder[i];
                        double frac = (double)(track - yMin) / (double)(yMax - yMin);
                        ys[i] = bottom - (int)(frac * (bottom - top));
                    }

                    Graphics2D g2 = (Graphics2D) g;
                    g2.setStroke(new BasicStroke(2));
                    g2.setColor(new Color(255, 179, 186));
                    for (int i = 0; i < pts - 1; i++) {
                        g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
                    }

                    g2.setColor(new Color(255,223,186));
                    for (int i = 0; i < pts; i++) {
                        int rsz = 10;
                        g2.fillOval(xs[i] - rsz/2, ys[i] - rsz/2, rsz, rsz);
                        g2.setColor(Color.BLACK);
                        g2.drawString(String.valueOf(result.plottedOrder[i]), xs[i] - 10, ys[i] - 12);
                        g2.setColor(new Color(255,223,186));
                    }

                    g2.setColor(Color.BLACK);
                    g2.drawString("Order →", Math.max(drawLeft + 10, drawLeft + drawWidth - 60), bottom + 30);
                    g2.drawString("Track (0-199)", left - 44, top - 12);
                }
            };

            int graphWidth = Math.max(700, 120 * Math.max(1, result.plottedCount));
            int graphHeight = 300;
            graphPanel.setPreferredSize(new Dimension(graphWidth, graphHeight));
            graphPanel.setBackground(Color.WHITE);

            JScrollPane graphScroll = new JScrollPane(graphPanel,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            graphScroll.setPreferredSize(new Dimension(520, 200));
            graphScroll.setMinimumSize(new Dimension(520, 200));
            graphScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
            contentPanel.add(graphScroll);
            contentPanel.add(Box.createRigidArea(new Dimension(0,5)));

            JTextArea orderArea = new JTextArea();
            orderArea.setEditable(false);
            orderArea.setLineWrap(true);
            orderArea.setWrapStyleWord(true);
            orderArea.setBorder(BorderFactory.createTitledBorder("Order of Service"));
            StringBuilder sbOrder = new StringBuilder();
            for (int i = 0; i < result.plottedCount; i++) {
                sbOrder.append(result.plottedOrder[i]);
                if (i < result.plottedCount - 1) sbOrder.append(" → ");
            }
            orderArea.setText(sbOrder.toString());
            orderArea.setPreferredSize(new Dimension(520, 45));
            orderArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            JScrollPane orderScroll = new JScrollPane(orderArea);
            orderScroll.setPreferredSize(new Dimension(520, 45));
            orderScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            contentPanel.add(orderScroll);
            contentPanel.add(Box.createRigidArea(new Dimension(0,5)));

            JTextArea breakdownArea = new JTextArea(result.breakdownText);
            breakdownArea.setEditable(false);
            breakdownArea.setLineWrap(true);
            breakdownArea.setWrapStyleWord(true);
            breakdownArea.setBorder(BorderFactory.createTitledBorder("Movement Breakdown"));
            
            int rows = result.breakdownText.split("\n").length;
            int breakdownHeight = Math.max(80, rows * 18 + 30);
            
            breakdownArea.setPreferredSize(new Dimension(520, breakdownHeight));
            breakdownArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, breakdownHeight));
            JScrollPane breakdownScroll = new JScrollPane(breakdownArea,
                    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            breakdownScroll.setPreferredSize(new Dimension(520, breakdownHeight));
            breakdownScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, breakdownHeight));
            contentPanel.add(breakdownScroll);

            JScrollPane mainScroll = new JScrollPane(contentPanel,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            mainScroll.getVerticalScrollBar().setUnitIncrement(16);

            resultArea.add(mainScroll, BorderLayout.CENTER);

            JLabel totalLabel = new JLabel("Total Track Movement: " + result.totalMovement + " cylinders");
            totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 14f));
            totalLabel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
            resultArea.add(totalLabel, BorderLayout.SOUTH);

            resultArea.revalidate();
            resultArea.repaint();
        });

        return mainPanel;
    }

    private static class SCANResultData {
        DiskRequest[] req;
        int[] plottedOrder;
        int plottedCount;
        int totalMovement;
        String breakdownText;
    }

    private SCANResultData buildResultFromCompute(DiskRequest[] reqInput, int n, int head) {

        DiskRequest[] req = new DiskRequest[n];
        for (int i = 0; i < n; i++) req[i] = new DiskRequest(reqInput[i].track);

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (req[j].track > req[j + 1].track) {
                    DiskRequest tmp = req[j];
                    req[j] = req[j + 1];
                    req[j + 1] = tmp;
                }
            }
        }

        int startIndex = n;
        for (int i = 0; i < n; i++) {
            if (req[i].track >= head) {
                startIndex = i;
                break;
            }
        }

        int[] order = new int[n + 3];
        int idx = 0;
        int current = head;

        for (int i = startIndex; i < n; i++) {
            order[idx++] = req[i].track;
            current = req[i].track;
        }

        order[idx++] = 199;

        for (int i = startIndex - 1; i >= 0; i--) {
            order[idx++] = req[i].track;
            current = req[i].track;
        }

        if (current != 0) {
            order[idx++] = 0;
        }

        int[] plotted = new int[idx + 1];
        plotted[0] = head;
        for (int i = 0; i < idx; i++) plotted[i + 1] = order[i];

        int total = 0;
        for (int i = 0; i < n; i++) total += reqInput[i].movement;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < plotted.length - 1; i++) {
            int a = plotted[i];
            int b = plotted[i + 1];
            int mv = Math.abs(b - a);
            sb.append(a).append(" → ").append(b).append(" = ").append(mv).append("\n");
        }
        sb.append("\nTotal = ");
        for (int i = 0; i < plotted.length - 1; i++) {
            int a = plotted[i];
            int b = plotted[i + 1];
            int mv = Math.abs(b - a);
            sb.append(mv);
            if (i < plotted.length - 2) sb.append(" + ");
        }
        sb.append(" = ").append(total);

        SCANResultData out = new SCANResultData();
        out.req = reqInput;
        out.plottedOrder = plotted;
        out.plottedCount = plotted.length;
        out.totalMovement = total;
        out.breakdownText = sb.toString();
        return out;
    }

    private void computeSCAN(DiskRequest[] req, int n, int head) {

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (req[j].track > req[j + 1].track) {
                    DiskRequest t = req[j];
                    req[j] = req[j + 1];
                    req[j + 1] = t;
                }
            }
        }

        int startIndex = n;
        for (int i = 0; i < n; i++) {
            if (req[i].track >= head) {
                startIndex = i;
                break;
            }
        }

        int gantt[] = new int[n + 2];
        int gIndex = 0;

        int totalMovement = 0;
        int current = head;

        System.out.println("\nSCAN SERVICE ORDER:");
        System.out.print(current);

        for (int i = startIndex; i < n; i++) {
            req[i].movement = Math.abs(req[i].track - current);
            totalMovement += req[i].movement;
            current = req[i].track;
            req[i].served = true;

            gantt[gIndex++] = current;
            System.out.print(" -> " + current);
        }

        int movementTo199 = Math.abs(199 - current);
        totalMovement += movementTo199;

        current = 199;

        gantt[gIndex++] = 199;
        System.out.print(" -> " + 199 + " (boundary)");

        for (int i = startIndex - 1; i >= 0; i--) {

            req[i].movement = Math.abs(req[i].track - current);
            totalMovement += req[i].movement;

            current = req[i].track;
            req[i].served = true;

            gantt[gIndex++] = current;
            System.out.print(" -> " + current);
        }

        if (current != 0) {
            int movementTo0 = current;
            totalMovement += movementTo0;

            current = 0;
            gantt[gIndex++] = 0;
            System.out.print(" -> 0 (boundary)");
        }

        System.out.println("\n\nTotal Track Movement: " + totalMovement + " cylinders");

        System.out.println("\nDETAILS PER REQUEST:");
        System.out.println("Track\tMovement\tServed");
        for (int i = 0; i < n; i++) {
            System.out.println(req[i].track + "\t" + req[i].movement + "\t\t" + req[i].served);
        }
    }
}