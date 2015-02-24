package user;

import packetLib.PacketWriter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Program extends JFrame
{
    public static Program prog;
    public static Client c;
    private JPanel mainPanel;
    private JTextField chatInput;
    private JList contactList;
    private DefaultListModel contactModel;
    private JButton sendButton;
    private JTextArea chatBox;

    public Program()
    {
        super("Exclaim Client (Work in Progress)");

        setContentPane(mainPanel);
        setSize(500, 400);

        //Need for adding to list
        contactModel = new DefaultListModel();
        contactList.setModel(contactModel);

        sendButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                PacketWriter pw = new PacketWriter(1);
                pw.writeString(chatInput.getText());
                for (String s : c.cList.keySet())
                    c.cList.get(s).sendPacket(pw);
                writeChat("Me", chatInput.getText());
                chatInput.setText("");
            }
        });
        //pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }

    public static void main(String[] args) throws IOException
    {
        prog = new Program();

        c = new Client();
        c.run();
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
}
