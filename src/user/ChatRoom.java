package user;

import packetLib.PacketWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChatRoom extends JFrame
{
    private JPanel chatPanel;
    private JTextField chatInput;
    private JList contactList;
    private DefaultListModel contactModel;
    private JButton sendButton;
    private JTextArea chatBox;
    private JButton joinButton;
    private JFormattedTextField joinIPInput;

    public ChatRoom()
    {
        super("Exclaim Chat Room (Work in Progress)");
        chatPanel.setBorder(new EmptyBorder(7, 7, 7, 7));
        setContentPane(chatPanel);
        setSize(600, 450);

        //Need for adding to list
        contactModel = new DefaultListModel();
        contactList.setModel(contactModel);
        contactList.addMouseListener(new ContactMouseListener());

        sendButton.addActionListener(new SendChatListener());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }

    public void addContact(String name)
    {
        contactModel.addElement(name);
    }

    public void removeContact(String name)
    {
        contactModel.removeElement(name);
    }

    public void writeChat(String name, String message)
    {
        chatBox.append(name + ": " + message + "\n");
    }

    public void writeAlert(String message)
    {
        chatBox.append(message + "\n");
    }
        /*PacketWriter pw = new PacketWriter(12);
        pw.writeShort(323);
        pw.writeInt(-123);
        pw.writeLong(992319323123L);
        pw.writeString("hey?");
        pw.writeHexString("AB CD EFFFF");
        System.out.println(pw);
        PacketReader pr = new PacketReader(pw.toByteArray());
        System.out.println(pr.readByte());
        System.out.println(pr.readShort());
        System.out.println(pr.readInt());
        System.out.println(pr.readLong());
        System.out.println(pr.readString());
        System.out.println(pr.readHexString(5));
        pr.close();
        System.exit(0);*/

        /*JFrame prog = new JFrame();
        prog.setSize(300, 400);
        prog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        prog.setVisible(true);
        */
    class SendChatListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            PacketWriter pw = new PacketWriter(1);
            pw.writeString(chatInput.getText());
            for (String s : Program.c.cList.keySet())
                Program.c.cList.get(s).sendPacket(pw);
            writeChat("Me", chatInput.getText());
            chatInput.setText("");
        }
    }

    class ContactMouseListener extends MouseAdapter
    {
        ContactRightMenu contactMenu;

        public ContactMouseListener()
        {
            contactMenu = new ContactRightMenu();
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                contactMenu.removeItem.setEnabled(contactList.getSelectedIndex() >= 0);
                contactMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class ContactRightMenu extends JPopupMenu
    {
        final JMenuItem addItem;
        final JMenuItem removeItem;

        public ContactRightMenu()
        {
            addItem = new JMenuItem("Add Contact");
            removeItem = new JMenuItem("Remove Contact");

            addItem.addActionListener(new AddContactListener());
            removeItem.addActionListener(new RemoveContactListener());

            add(addItem);
            add(removeItem);
        }

        class AddContactListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("Adding not implemented");
            }
        }

        class RemoveContactListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("Removing not implemented");
            }
        }
    }
}
