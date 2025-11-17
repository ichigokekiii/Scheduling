import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainPage extends JFrame {

    private JPanel mainContent;

    public MainPage() {

        setTitle("CPU & Disk Scheduling");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(new Color(245, 228, 215)); 
        header.setPreferredSize(new Dimension(1400, 65));
        JLabel title = new JLabel("CPU & Disk Scheduling Algorithms");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(60, 50, 50));
        header.add(title);
        add(header, BorderLayout.NORTH);

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 820));
        sidebar.setBackground(new Color(47, 35, 65)); 
        sidebar.setLayout(new GridLayout(6, 1, 0, 10));

        JButton btnSRTF = createSidebarButton("SRTF (Shortest Remaining Time First)");
        JButton btnPriority = createSidebarButton("Priority (Non-Preemptive)");
        JButton btnCLook = createSidebarButton("C-LOOK Disk Scheduling");
        JButton btnScan = createSidebarButton("SCAN Disk Scheduling");
        JButton btnExit = createSidebarButton("Exit");

        sidebar.add(btnSRTF);
        sidebar.add(btnPriority);
        sidebar.add(btnCLook);
        sidebar.add(btnScan);
        sidebar.add(btnExit);

        add(sidebar, BorderLayout.WEST);

        mainContent = new JPanel(new CardLayout());
        mainContent.setBackground(Color.WHITE);

        JPanel homePanel = new JPanel();
        homePanel.setBackground(Color.WHITE);
        homePanel.add(new JLabel("Select an algorithm from the left menu."));
        mainContent.add(homePanel, "HOME");

        add(mainContent, BorderLayout.CENTER);

        btnSRTF.addActionListener(e -> openSRTF());
        btnPriority.addActionListener(e -> openPriority());
        btnCLook.addActionListener(e -> openCLook());
        btnScan.addActionListener(e -> openScan());
        btnExit.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(64, 51, 79));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(90, 70, 110));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(64, 51, 79));
            }
        });

        return btn;
    }

    private void openSRTF() {
        SRTF s = new SRTF();
        JPanel panel = s.getUI();

        mainContent.removeAll();
        mainContent.add(panel, "SRTF");
        ((CardLayout) mainContent.getLayout()).show(mainContent, "SRTF");
        mainContent.revalidate();
        mainContent.repaint();
    }

    private void openPriority() {
        Priority p = new Priority();
        JPanel panel = p.getUI();

        mainContent.removeAll();
        mainContent.add(panel, "PRIORITY");
        ((CardLayout) mainContent.getLayout()).show(mainContent, "PRIORITY");
        mainContent.revalidate();
        mainContent.repaint();
    }

    private void openCLook() {
        CLook c = new CLook();
        JPanel panel = c.getUI();

        mainContent.removeAll();
        mainContent.add(panel, "CLOOK");
        ((CardLayout) mainContent.getLayout()).show(mainContent, "CLOOK");
        mainContent.revalidate();
        mainContent.repaint();
    }

    private void openScan() {
        Skan sc = new Skan();
        JPanel panel = sc.getUI();

        mainContent.removeAll();
        mainContent.add(panel, "SCAN");
        ((CardLayout) mainContent.getLayout()).show(mainContent, "SCAN");
        mainContent.revalidate();
        mainContent.repaint();
    }

    public static void main(String[] args) {
        new MainPage();
    }
}