package user;

import packetLib.PacketWriter;
import tools.Validate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.event.*;

import static tools.Validate.isValidIP;

public class ChatRoom extends JFrame
{
    private JPanel chatPanel;
    private JTextField chatInput;
    private JList contactList;
    private DefaultListModel contactModel;
    private JButton sendButton;
    private JTextArea chatBox;
    private JButton joinLeaveButton;
    private JTextField joinIPInput;
    private JTextField chatNameInput;
    private JButton sendFileButton;
    private JFileChooser fileChooser;

    public ChatRoom()
    {
        super("Exclaim: Chat Room");
        chatPanel.setBorder(new EmptyBorder(7, 7, 7, 7));

        setContentPane(chatPanel);
        setSize(600, 450);

        ((AbstractDocument) chatNameInput.getDocument()).setDocumentFilter(new LimitDocumentFilter(20));
        //Need for adding to list
        contactModel = new DefaultListModel();
        contactList.setModel(contactModel);
        contactList.addMouseListener(new ContactMouseListener());
        sendFileButton.addActionListener(new SendFileListener());

        sendButton.addActionListener(new SendChatListener());
        joinLeaveButton.addActionListener(new JoinLeaveListener());

        joinIPInput.addFocusListener(new JoinIPListener());
        chatNameInput.addFocusListener(new ChatNameListener());

        getRootPane().setDefaultButton(sendButton);

        super.setName(chatNameInput.getText());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowEventListener());

        fileChooser = new JFileChooser();
        fileChooser.setApproveButtonText("Send");
        fileChooser.setDialogTitle("Choose a file to send...");

        setVisible(true);
    }

    public void addContact(String name)
    {
        contactModel.addElement(name);
    }

    public void removeContact(String name)
    {
        contactModel.removeElement(name);
        sendFileButton.setEnabled(contactList.getSelectedIndex() >= 0);
    }

    public void setName(String name)
    {
        super.setName(name);
        chatNameInput.setText(name);
        writeAlert("Chat name set as: " + name);
    }

    public void joined(boolean b)
    {
        joinLeaveButton.setEnabled(!b);
        joinIPInput.setEnabled(!b);
    }

    public void writeChat(String name, String message)
    {
        if(message.length() > 0)
            chatBox.append(name + ": " + message + "\n");
    }

    public void writeAlert(String message)
    {
        chatBox.append(message + "\n");
    }

    class JoinIPListener implements FocusListener
    {
        @Override
        public void focusGained(FocusEvent e)
        { /* do nothing? */ }

        @Override
        public void focusLost(FocusEvent e)
        {
            if (!Validate.isValidIP(joinIPInput.getText().trim()))
                joinIPInput.setText("");
            else
                joinIPInput.setText(joinIPInput.getText().trim());
        }
    }

    class JoinLeaveListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String ip = joinIPInput.getText().trim();
            if (isValidIP(ip) && !ip.substring(0, 3).equals("127"))
            {
                System.out.println("Attempting to connect to " + ip + ":2121");
                Program.c.connectTo(ip, 2121);
            }
            else
            {
                if(ip.length() > 0) //no loopback support
                    writeAlert("You cannot connect to " + ip);
            }
        }
    }

    class ChatNameListener implements FocusListener
    {
        @Override
        public void focusGained(FocusEvent e)
        { /* do nothing? */ }

        @Override
        public void focusLost(FocusEvent e)
        {
            String name = chatNameInput.getText();
            if(!name.equals(getName()))
            {
                PacketWriter pw = new PacketWriter(Header.CHAT_NAME);
                pw.writeString(name);
                Program.c.sendAll(pw);
                setName(name);
            }
        }
    }

    class SendChatListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            PacketWriter pw = new PacketWriter(Header.CHAT);
            pw.writeString(chatInput.getText());
            Program.c.sendAll(pw);
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
            if (e.isPopupTrigger())
            {
                contactMenu.sendFileItem.setEnabled(contactList.getSelectedIndex() >= 0);
                contactMenu.show(e.getComponent(), e.getX(), e.getY());
            }
            if(e.getButton() == 1)
            {
                sendFileButton.setEnabled(contactList.getSelectedIndex() >= 0);
            }
        }
    }

    class ContactRightMenu extends JPopupMenu
    {
        //final JMenuItem addItem;
        //final JMenuItem removeItem;
        final JMenuItem sendFileItem;

        public ContactRightMenu()
        {
            /*addItem = new JMenuItem("Add Contact");
            removeItem = new JMenuItem("Remove Contact");

            addItem.addActionListener(new AddContactListener());
            removeItem.addActionListener(new RemoveContactListener());

            add(addItem);
            add(removeItem);*/
            sendFileItem = new JMenuItem("Send File");
            sendFileItem.addActionListener(new SendFileListener());
            add(sendFileItem);
        }

        /*class AddContactListener implements ActionListener
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
        }*/
    }

    class SendFileListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                System.out.println(fileChooser.getSelectedFile());
            
            PacketWriter pw = new PacketWriter(Header.FILE_OFFER);
            pw.writeString(fileChooser.getSelectedFile().getName());
            pw.writeLong(fileChooser.getSelectedFile().length()); //file size
            Program.c.chatList.get(contactList.getSelectedValue()).sendPacket(pw);
        }
    }

    class LimitDocumentFilter extends DocumentFilter
    {

        private int limit;

        public LimitDocumentFilter(int limit) {
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit can not be <= 0");
            }
            this.limit = limit;
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            int currentLength = fb.getDocument().getLength();
            int overLimit = (currentLength + text.length()) - limit - length;
            if (overLimit > 0) {
                text = text.substring(0, text.length() - overLimit);
            }
            if (text.length() > 0) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

    }
}
