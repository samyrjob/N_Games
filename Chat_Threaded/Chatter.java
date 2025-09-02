package Chat_Threaded;

import java.io.*;


public class Chatter
{
  private String cliAddr;
  private int port;
  private PrintWriter out;

  public Chatter(String cliAddr, int port, PrintWriter out)
  { this.cliAddr = cliAddr;
    this.port = port; this.out = out;
  }


  public boolean matches(String ca, int p)
  // the address and port of a client are used to uniquely identify it
  { if (cliAddr.equals(ca) && (port == p))
      return true;
    return false;
  } // end of matches()
 

  public void sendMessage(String msg)
  {  out.println(msg);  }


  public String toString()
  {  return cliAddr + " & " + port + " & ";  }


}  // end of Chatter class
