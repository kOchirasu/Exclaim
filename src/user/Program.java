package user;

import java.io.IOException;

public class Program
{
    public static Client c;
    public static ChatRoom chatRoom;
    public static MainForm mainProg;

    public static void main(String[] args) throws IOException
    {
        chatRoom = new ChatRoom();
        //mainProg = new MainForm();

        c = new Client();
        c.run();
    }
}
