import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class Skan {

    public void scanResult() {

        JFrame inputFrame = new JFrame("SCAN Input");
        inputFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inputFrame.setSize(460, 460);
        inputFrame.setLocationRelativeTo(null);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Number of requests:"));
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

        JPanel headRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headRow.add(new JLabel("Enter head position:"));
        JTextField headField = new JTextField(6);
        headRow.add(headField);
        main.add(Box.createRigidArea(new Dimension(0,8)));
        main.add(headRow);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton runBtn = new JButton("Run SCAN");
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

            JTextField[] inputs = new JTextField[n];

            for (int i = 0; i < n; i++) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
                JOptionPane.showMessageDialog(inputFrame, "Please click Create and fill all fields.");
                return;
            }

            int head;
            try {
                head = Integer.parseInt(headField.getText().trim());
                if (head < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(inputFrame, "Please enter a valid non-negative integer for head.");
                return;
            }

            DiskRequest[] req = new DiskRequest[n];
            try {
                for (int i = 0; i < n; i++) {
                    int tr = Integer.parseInt(fields[i].getText().trim());
                    req[i] = new DiskRequest(tr);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(inputFrame, "Please enter valid integer track numbers for all requests.");
                return;
            }

            inputFrame.dispose();

            DiskRequest[] reqCopy = new DiskRequest[n];
            for (int i = 0; i < n; i++) reqCopy[i] = new DiskRequest(req[i].track);

            computeSCAN(reqCopy, n, head);

            SCANResultData result = buildResultFromCompute(reqCopy, n, head);
            showUI(result);
        });
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

    private void showUI(SCANResultData r) {

        JFrame frame = new JFrame("SCAN Result");
        frame.setSize(940, 700);
        frame.setLayout(new BorderLayout(8,8));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] cols = {"Track", "Movement", "Served"};
        String[][] data = new String[r.req.length][3];
        for (int i = 0; i < r.req.length; i++) {
            data[i][0] = String.valueOf(r.req[i].track);
            data[i][1] = String.valueOf(r.req[i].movement);
            data[i][2] = String.valueOf(r.req[i].served);
        }

        JTable table = new JTable(data, cols);
        JScrollPane tableScroll = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(420, 140));
        frame.add(tableScroll, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        JPanel graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                int left = 60;
                int right = getWidth() - 160;
                int top = 40;
                int bottom = getHeight() - 80;

                g.setColor(new Color(230,230,230));
                g.fillRect(left, top, right - left, bottom - top);

                g.setColor(Color.DARK_GRAY);
                g.drawRect(left, top, right - left, bottom - top);

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

                int pts = r.plottedCount;
                if (pts < 2) return;

                int[] xs = new int[pts];
                int[] ys = new int[pts];
                for (int i = 0; i < pts; i++) {
                    xs[i] = left + (int)((double)i / (pts - 1) * (right - left));
                    int track = r.plottedOrder[i];
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
                    g2.drawString(String.valueOf(r.plottedOrder[i]), xs[i] - 10, ys[i] - 12);
                    g2.setColor(new Color(255,223,186));
                }

                g2.setColor(Color.BLACK);
                g2.drawString("Order →", right - 60, bottom + 30);
                g2.drawString("Track (0-199)", left - 44, top - 12);
            }
        };

        graphPanel.setPreferredSize(new Dimension(640, 420));
        center.add(graphPanel, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        JLabel orderLabel = new JLabel("Order of Service:");
        right.add(orderLabel);
        StringBuilder sbOrder = new StringBuilder();
        for (int i = 0; i < r.plottedCount; i++) {
            sbOrder.append(r.plottedOrder[i]);
            if (i < r.plottedCount - 1) sbOrder.append(" → ");
        }
        JTextArea orderArea = new JTextArea(sbOrder.toString());
        orderArea.setEditable(false);
        orderArea.setLineWrap(true);
        orderArea.setWrapStyleWord(true);
        orderArea.setPreferredSize(new Dimension(220, 120));
        right.add(new JScrollPane(orderArea));
        right.add(Box.createRigidArea(new Dimension(0,10)));
        JLabel breakdownLabel = new JLabel("Movement Breakdown:");
        right.add(breakdownLabel);
        JTextArea breakdownArea = new JTextArea(r.breakdownText);
        breakdownArea.setEditable(false);
        breakdownArea.setLineWrap(true);
        breakdownArea.setWrapStyleWord(true);
        breakdownArea.setPreferredSize(new Dimension(220, 220));
        right.add(new JScrollPane(breakdownArea));
        right.add(Box.createRigidArea(new Dimension(0,10)));
        JLabel totalLabel = new JLabel("Total Track Movement: " + r.totalMovement + " cylinders");
        right.add(totalLabel);

        center.add(right, BorderLayout.EAST);

        frame.add(center, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
