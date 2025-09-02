package Chat_Threaded;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class ChatWatcher extends Thread
{
  private ChatClient client;   // ref to top-level client
  private BufferedReader in;   // stream coming from the server


  public ChatWatcher(ChatClient c, BufferedReader i)
  {  client = c; in = i;  }


  public void run()
  /* Read a server message, display it, repeat.
     A message can be:
			"WHO$$ n1 & p1 & .... nN & pN & "
     or     "(cliAddr,port): msg"

     "WHO$$" messages are reformatted in showWho(), but 
     other messages are displayed immediately by calling 
     showMsg() in the client.
  */
  { String line;
    try {
      while ((line = in.readLine()) != null) {
        if ((line.length() >= 6) &&     // "WHO$$ "
            (line.substring(0,5).equals("WHO$$")))
          showWho( line.substring(5).trim() );    
		    // remove WHO$$ keyword and surrounding space
        else  // show immediately
          client.showMsg(line + "\n");
      }
    }
    catch(Exception e)    // socket closure will cause termination of while
    { // System.out.println( e );  
      JOptionPane.showMessageDialog( null, 
           "Link to Server Lost!", "Connection Closed", 
			JOptionPane.ERROR_MESSAGE);
      System.exit( 0 );
    }
  } // end of run()


  private void showWho(String line)
  /*  line has the format:
             "cliAddr1 & port1 & ... cliAddrN & portN & "
      Reformat it before calling showMsg() in the client.
  */
  { StringTokenizer st = new StringTokenizer(line, "&");
    String addr;
    int port;
    int i = 1;
    try {
      while (st.hasMoreTokens()) {
        addr = st.nextToken().trim();
        port = Integer.parseInt( st.nextToken().trim() );
        client.showMsg("" + i + ". " + addr + " : " + port + "\n");
        i++;
      }
      // client.showMsg("\n");
    }
    catch(Exception e)
    { client.showMsg("Problem parsing who info.\n");
      System.out.println("Parsing error with who info: \n" + e);  
    }
  }  // end of showWho()


}  // end of ChatWatcher class
