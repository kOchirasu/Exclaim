package user;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;

public class MainForm extends JFrame
{
    private DefaultListModel blackModel;
    private DefaultListModel whiteModel;
    private DefaultListModel requestModel;
    private JList blackList;
    private JList whiteList;
    private JList requestList;
    private JPanel mainPanel;
    private JButton whitelistButton;
    private JButton blacklistButton;
    private JButton acceptButton;
    private JButton rejectButton;
    private JButton removeWlButton;
    private JButton removeBlButton;

    public MainForm()
    {
        super("Exclaim: Connection Manager");
        mainPanel.setBorder(new EmptyBorder(7, 7, 7, 7));
        setContentPane(mainPanel);
        setSize(500, 300);
        setResizable(false);

        blackModel = new DefaultListModel();
        blackList.setModel(blackModel);
        whiteModel = new DefaultListModel();
        whiteList.setModel(whiteModel);
        requestModel = new DefaultListModel();
        requestList.setModel(requestModel);

        blackList.addMouseListener(new BlackMouseListener());
        whiteList.addMouseListener(new WhiteMouseListener());
        requestList.addMouseListener(new RequestMouseListener());

        whitelistButton.addActionListener(new WhiteListListener());
        blacklistButton.addActionListener(new BlackListListener());
        acceptButton.addActionListener(new AcceptListener());
        rejectButton.addActionListener(new RejectListener());
        removeWlButton.addActionListener(new WhiteListener());
        removeBlButton.addActionListener(new BlackListener());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowEventListener());

        setVisible(true);
    }

    public void addBlack(Object name)
    {
        blackModel.addElement(name);
        Program.autoList.put((String)name, false);
    }

    public void addWhite(Object name)
    {
        whiteModel.addElement(name);
        Program.autoList.put((String)name, true);
    }

    public void addRequest(Object name)
    {
        requestModel.addElement(name);
    }

    public void removeBlack(Object name)
    {
        blackModel.removeElement(name);
        Program.autoList.remove(name);
    }

    public void removeWhite(Object name)
    {
        whiteModel.removeElement(name);
        Program.autoList.remove(name);
    }

    public void removeRequest(Object name)
    {
        requestModel.removeElement(name);
    }

    class BlackMouseListener extends MouseAdapter
    {
        BlackRightMenu blackMenu;

        public BlackMouseListener()
        {
            blackMenu = new BlackRightMenu();
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                blackMenu.removeItem.setEnabled(blackList.getSelectedIndex() >= 0);
                blackMenu.show(e.getComponent(), e.getX(), e.getY());
            }
            if(e.getButton() == 1)
            {
                removeBlButton.setEnabled(blackList.getSelectedIndex() >= 0);
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
    }

    class BlackListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            removeBlack(blackList.getSelectedValue());
            removeBlButton.setEnabled(blackList.getSelectedIndex() >= 0);
        }
    }

    class WhiteMouseListener extends MouseAdapter
    {
        WhiteRightMenu whiteMenu;

        public WhiteMouseListener()
        {
            whiteMenu = new WhiteRightMenu();
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                whiteMenu.removeItem.setEnabled(whiteList.getSelectedIndex() >= 0);
                whiteMenu.show(e.getComponent(), e.getX(), e.getY());
            }
            if(e.getButton() == 1)
            {
                removeWlButton.setEnabled(whiteList.getSelectedIndex() >= 0);
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
    }

    class WhiteListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            removeWhite(whiteList.getSelectedValue());
            removeWlButton.setEnabled(whiteList.getSelectedIndex() >= 0);
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
            if (e.isPopupTrigger())
            {
                requestMenu.acceptItem.setEnabled(requestList.getSelectedIndex() >= 0);
                requestMenu.rejectItem.setEnabled(requestList.getSelectedIndex() >= 0);
                requestMenu.whiteListItem.setEnabled(requestList.getSelectedIndex() >= 0);
                requestMenu.blackListItem.setEnabled(requestList.getSelectedIndex() >= 0);
                requestMenu.show(e.getComponent(), e.getX(), e.getY());
            }
            if(e.getButton() == 1)
            {
                acceptButton.setEnabled(requestList.getSelectedIndex() >= 0);
                rejectButton.setEnabled(requestList.getSelectedIndex() >= 0);
                whitelistButton.setEnabled(requestList.getSelectedIndex() >= 0);
                blacklistButton.setEnabled(requestList.getSelectedIndex() >= 0);
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
    }

    class AcceptListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String s = (String) requestList.getSelectedValue();
            removeRequest(s);
            Program.c.chatAccept(s);
            acceptButton.setEnabled(requestList.getSelectedIndex() >= 0);
            rejectButton.setEnabled(requestList.getSelectedIndex() >= 0);
            whitelistButton.setEnabled(requestList.getSelectedIndex() >= 0);
            blacklistButton.setEnabled(requestList.getSelectedIndex() >= 0);
        }
    }

    class RejectListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String s = (String) requestList.getSelectedValue();
            removeRequest(s);
            Program.c.chatReject(s);
            acceptButton.setEnabled(requestList.getSelectedIndex() >= 0);
            rejectButton.setEnabled(requestList.getSelectedIndex() >= 0);
            whitelistButton.setEnabled(requestList.getSelectedIndex() >= 0);
            blacklistButton.setEnabled(requestList.getSelectedIndex() >= 0);
        }
    }

    class WhiteListListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Object o = requestList.getSelectedValue();
            addWhite(o);
            removeRequest(o);
            Program.c.chatAccept((String)o);
            acceptButton.setEnabled(requestList.getSelectedIndex() >= 0);
            rejectButton.setEnabled(requestList.getSelectedIndex() >= 0);
            whitelistButton.setEnabled(requestList.getSelectedIndex() >= 0);
            blacklistButton.setEnabled(requestList.getSelectedIndex() >= 0);
        }
    }

    class BlackListListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Object o = requestList.getSelectedValue();
            addBlack(o);
            removeRequest(o);
            Program.c.chatReject((String) o);
            acceptButton.setEnabled(requestList.getSelectedIndex() >= 0);
            rejectButton.setEnabled(requestList.getSelectedIndex() >= 0);
            whitelistButton.setEnabled(requestList.getSelectedIndex() >= 0);
            blacklistButton.setEnabled(requestList.getSelectedIndex() >= 0);
        }
    }
}
