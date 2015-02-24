package user;

import java.io.IOException;
import java.net.ServerSocket;

public class Listener implements Runnable
{
    Client c;
    ServerSocket listenSock;

    public Listener(Client c, int port) throws IOException
    {
        listenSock = new ServerSocket(port);
        this.c = c;
    }

    public void run()
    {
        System.out.println("S> Exclaim Chat Server started...");
        while (true)
        {
            //Wait for some connection
            System.out.println("Server waiting for connection...");
            c.connectTo(listenSock);
        }
    }
}
