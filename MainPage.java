import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainPage extends JFrame {

    public MainPage() {

        setTitle("CPU & Disk Scheduling Simulator");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("MAIN MENU", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);

        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton btnSRTF = new JButton("SRTF (Shortest Remaining Time First)");
        JButton btnPriority = new JButton("Priority Scheduling (Non-Preemptive)");
        JButton btnCLook = new JButton("C-LOOK Disk Scheduling");
        JButton btnSCAN = new JButton("SCAN Disk Scheduling");
        JButton btnExit = new JButton("Exit");

        JButton[] buttons = { btnSRTF, btnPriority, btnCLook, btnSCAN, btnExit };

        for (JButton b : buttons) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(350, 45));
            b.setFont(new Font("Arial", Font.PLAIN, 16));
            panel.add(b);
            panel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        btnSRTF.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SRTF s = new SRTF();
                s.srtfResult();
            }
        });

        btnPriority.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Priority p = new Priority();
                p.prioResult();
            }
        });

        btnCLook.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CLook c = new CLook();
                c.cLookResult();
            }
        });

        btnSCAN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Skan s = new Skan();
                s.scanResult();
            }
        });

        btnExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainPage());
    }
}
