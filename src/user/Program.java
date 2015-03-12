package user;

import java.io.*;
import java.util.HashMap;

public class Program
{
    public static Client c;
    public static ChatRoom chatRoom;
    public static MainForm mainProg;
    public static HashMap<String, Boolean> autoList;
    //aList: Allow List - IP
    //List of IPs that will be white listed for 1 connect

    public static void main(String[] args) throws IOException
    {
        chatRoom = new ChatRoom();
        mainProg = new MainForm();
        autoList = new HashMap<>();
        loadAutoList();

        c = new Client();
        c.run();
    }

    public static void loadAutoList()
    {
        try
        {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("autolist.ser"));
            HashMap<String, Boolean> tempList = (HashMap<String, Boolean>) in.readObject();
            in.close();

            for(String s : tempList.keySet())
            {
                if(tempList.get(s))
                {
                    //System.out.println("Added " + s);
                    mainProg.addWhite(s);
                }
                else
                {
                    mainProg.addBlack(s);
                }
            }
            System.out.println("Data loaded from autolist.ser");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static void saveAutoList()
    {
        try
        {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("autolist.ser"));
            out.writeObject(autoList);
            out.close();
            System.out.println("Data saved to autolist.ser");
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
