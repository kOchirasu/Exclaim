package user;

import java.io.IOException;
import java.net.ServerSocket;

public class Listener implements Runnable
{
    Client c;
    ServerSocket listenSock;

    public Listener(Client c, int port)
    {
        try
        {
            listenSock = new ServerSocket(port);
            this.c = c;
        }
        catch(IOException ex)
        {
            Program.chatRoom.writeAlert("Unable to start listener.  You will not receive connection requests.");
            ex.printStackTrace();
        }
    }

    public void run()
    {
        System.out.println("Exclaim Chat Server started...");
        while (true)
        {
            //Wait for some connection
            System.out.println("Server waiting for connection...");
            c.connectTo(listenSock);
        }
    }
}
