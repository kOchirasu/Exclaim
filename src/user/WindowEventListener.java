package user;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class WindowEventListener extends WindowAdapter
{
    public void windowClosing(WindowEvent e)
    {
        Program.saveAutoList();
    }
}
