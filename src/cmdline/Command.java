package cmdline;

import user.Client;

public interface Command
{
    public void handle(Client c, String[] cmd);

    public String usage();

    public void setName(String name);
}
