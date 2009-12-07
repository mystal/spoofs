package cs5204.fs.master;

public class MasterLauncher
{
    public static void main(String[] args)
    {
        boolean flag = false;

        //Parse arguments
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("--backup") && (i+3) < args.length)
            {
                //Start MasterBackup
                flag = true;
                MasterBackup.initialize(args[i+1], Integer.parseInt(args[i+2]), Integer.parseInt(args[i+3]));
            }
        }

        if (!flag)
            MasterServer.initialize();
    }
}
