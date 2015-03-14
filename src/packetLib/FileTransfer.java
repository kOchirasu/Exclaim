package packetLib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;

public class FileTransfer extends Connection implements Runnable
{
    private static final String AES_KEY = "rurt5Sg7PvhF8gIf";
    private static final int MAX_BUF = 4000;
    private File file;

    public FileTransfer(String ip, int port)
    {
        super(ip, port, AES_KEY);
    }

    public FileTransfer(ServerSocket ss)
    {
        super(ss, AES_KEY);
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    @Override
    public void run()
    {
        if(encrypted)
        {
            try
            {
                //long size = in.readLong();
                FileOutputStream outFile = new FileOutputStream(file);
                //System.out.println("Writing " + size + " bytes");
                int length;
                byte[] recvP;// = new byte[MAX_BUF];
                while ((length = in.readInt()) > 0)
                {
                    recvP = new byte[length];
                    //System.out.println("reading " + length);
                    in.readFully(recvP);
                    //System.out.println("Read: " + Arrays.toString(recvP));
                    recvP = dCiph.doFinal(recvP);
                    //System.out.println("Decrypt: " + Arrays.toString(recvP));
                    outFile.write(recvP);
                    //System.out.println("Wrote");
                }
                outFile.close();
                System.out.println("finished writing file");
            }
            catch (Exception ex) //File transfer failed
            {
                System.out.println("File transfer failed...");
                ex.printStackTrace();
            }
        }
        else
        {
            super.init();
            recvFile();
        }
    }

    public void sendFile(File f)
    {
        try
        {
            long size = f.length();
            //out.writeLong(size);
            FileInputStream inFile = new FileInputStream(f);
            byte[] sendP;
            int length = MAX_BUF;
            while(size > 0)
            {
                sendP = size >= length ? new byte[MAX_BUF] : new byte[(int) size];

                inFile.read(sendP);
                size -= sendP.length;
                sendP = eCiph.doFinal(sendP);
                out.writeInt(sendP.length);
                out.write(sendP);
            }
            out.writeInt(0); //finished
            System.out.println("File transfer complete");
        }
        catch(Exception ex)
        {
            System.out.println("File transfer failed...");
            ex.printStackTrace();
        }
    }

    public void recvFile()
    {
        if(!request) //If this is server, it is downloading
            new Thread(this).start(); //Start thread for receiving packets
    }
}
