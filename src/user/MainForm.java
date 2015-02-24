package user;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Created by Computer on 2/24/2015.
 */
public class MainForm extends JFrame
{
    private JList blackList;
    private JList whiteList;
    private JList requestList;
    private JPanel mainPanel;

    public MainForm()
    {
        super("Exclaim Chat Client (Work in Progress)");
        mainPanel.setBorder(new EmptyBorder(7, 7, 7, 7));
        setContentPane(mainPanel);
        setSize(500,300);
        setResizable(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }
}
