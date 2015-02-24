package cmdline;

import user.Client;
import user.Program;

import java.util.HashMap;

public class Parser
{
    private HashMap<String, Command> list;
    private Command invalid;
    private String cmdList = "";

    public Parser()
    {
        list = new HashMap<>();
        invalid = new InvalidCommand(); //doesnt need a name
    }

    public void add(String s, Command c) //Supports multiple commands
    {
        s = s.toLowerCase();
        String[] sArr = s.split(",");
        cmdList += sArr[0] + " ";
        c.setName(sArr[0]);
        for (int i = 0; i < sArr.length; i++)
        {
            list.put(sArr[i], c);
        }
    }

    public void handle(Client c, String cmd)
    {
        if (cmd.length() > 0 && cmd.charAt(0) == '/')
        {
            cmd = cmd.substring(1); //remove '/'
            String[] cmdArr = cmd.split(" +");
            cmdArr[0] = cmdArr[0].toLowerCase(); //commands not case sensitive
            Command result = list.get(cmdArr[0]);
            if (result != null)
                result.handle(c, cmdArr);
            else
                invalid.handle(c, cmdArr);
        }
        else
        {
            c.println("Chat >> " + cmd);
            Program.chatRoom.writeChat("MYNAME", cmd);
        }

    }

    public String toString()
    {
        return cmdList;
    }
}
