package user;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainForm extends JFrame
{
    private DefaultListModel blackModel;
    private DefaultListModel whiteModel;
    private DefaultListModel requestModel;
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

        blackModel = new DefaultListModel();
        blackList.setModel(blackModel);
        whiteModel = new DefaultListModel();
        whiteList.setModel(whiteModel);
        requestModel = new DefaultListModel();
        requestList.setModel(requestModel);

        blackList.addMouseListener(new BlackMouseListener());
        whiteList.addMouseListener(new WhiteMouseListener());
        requestList.addMouseListener(new RequestMouseListener());

        setVisible(true);
    }

    public void addBlack(String name) { blackModel.addElement(name); }
    public void addWhite(String name) { whiteModel.addElement(name); }
    public void addRequest(String name) { requestModel.addElement(name); }

    public void removeBlack(String name) { blackModel.removeElement(name); }
    public void removeWhite(String name) { whiteModel.removeElement(name); }
    public void removeRequest(String name) { requestModel.removeElement(name); }

    class BlackMouseListener extends MouseAdapter
    {
        BlackRightMenu blackMenu;

        public BlackMouseListener() { blackMenu = new BlackRightMenu(); }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                blackMenu.removeItem.setEnabled(requestList.getSelectedIndex() >= 0);
                blackMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class BlackRightMenu extends JPopupMenu
    {
        final JMenuItem removeItem;

        public BlackRightMenu()
        {
            removeItem = new JMenuItem("Remove");

            removeItem.addActionListener(new BlackListener());

            add(removeItem);
        }

        class BlackListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                blackModel.removeElement(blackList.getSelectedValue());
            }
        }
    }

    class WhiteMouseListener extends MouseAdapter
    {
        WhiteRightMenu whiteMenu;

        public WhiteMouseListener() { whiteMenu = new WhiteRightMenu(); }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                whiteMenu.removeItem.setEnabled(requestList.getSelectedIndex() >= 0);
                whiteMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class WhiteRightMenu extends JPopupMenu
    {
        final JMenuItem removeItem;

        public WhiteRightMenu()
        {
            removeItem = new JMenuItem("Remove");

            removeItem.addActionListener(new WhiteListener());

            add(removeItem);
        }

        class WhiteListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                whiteModel.removeElement(whiteList.getSelectedValue());
            }
        }
    }

    class RequestMouseListener extends MouseAdapter
    {
        RequestRightMenu requestMenu;

        public RequestMouseListener()
        {
            requestMenu = new RequestRightMenu();
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                requestMenu.acceptItem.setEnabled(requestList.getSelectedIndex() >= 0);
                requestMenu.rejectItem.setEnabled(requestList.getSelectedIndex() >= 0);
                requestMenu.whiteListItem.setEnabled(requestList.getSelectedIndex() >= 0);
                requestMenu.blackListItem.setEnabled(requestList.getSelectedIndex() >= 0);
                requestMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class RequestRightMenu extends JPopupMenu
    {
        final JMenuItem acceptItem;
        final JMenuItem rejectItem;
        final JMenuItem whiteListItem;
        final JMenuItem blackListItem;

        public RequestRightMenu()
        {
            acceptItem = new JMenuItem("Accept");
            rejectItem = new JMenuItem("Reject");
            whiteListItem = new JMenuItem("Whitelist");
            blackListItem = new JMenuItem("Blacklist");

            acceptItem.addActionListener(new AcceptListener());
            rejectItem.addActionListener(new RejectListener());
            whiteListItem.addActionListener(new WhiteListListener());
            blackListItem.addActionListener(new BlackListListener());

            add(acceptItem);
            add(rejectItem);
            add(whiteListItem);
            add(blackListItem);
        }

        class AcceptListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String s = (String) requestList.getSelectedValue();
                requestModel.removeElement(s);
                Program.c.chatAccept(s);
            }
        }

        class RejectListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String s = (String) requestList.getSelectedValue();
                requestModel.removeElement(s);
                Program.c.chatReject(s);
            }
        }

        class WhiteListListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                whiteModel.addElement(requestList.getSelectedValue());
                requestModel.removeElement(requestList.getSelectedValue());
            }
        }

        class BlackListListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                blackModel.addElement(requestList.getSelectedValue());
                requestModel.removeElement(requestList.getSelectedValue());
            }
        }
    }
}
