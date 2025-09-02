package Chat_Threaded;


import java.net.*;
import java.io.*;


public class ChatServerHandler extends Thread
{
  private Socket clientSock;    // client details
  private String cliAddr;
  private int port;

  private ChatGroup cg;    // shared by all threads


  public ChatServerHandler(Socket s, ChatGroup cg)
  {
    this.cg = cg;
    clientSock = s;
    cliAddr = clientSock.getInetAddress().getHostAddress();
    port = clientSock.getPort();
    System.out.println("Client connection from (" + 
						cliAddr + ", " + port + ")");
  } // end of ChatServerHandler()


  public void run()
  {
    try {
      // Get I/O streams from the socket
      BufferedReader in  = new BufferedReader( 
	  		new InputStreamReader( clientSock.getInputStream() ) );
      PrintWriter out = 
			new PrintWriter( clientSock.getOutputStream(), true );  // autoflush

      cg.addPerson(cliAddr, port, out);  // add client details to ChatGroup

      processClient(in, out);            // interact with client
 
      // the client has finished when execution reaches here
      cg.delPerson(cliAddr, port);       // remove client details
      clientSock.close();
      System.out.println("Client (" + cliAddr + ", " + 
								port + ") connection closed\n");
    }
    catch(Exception e)
    {  System.out.println(e);  }
  }  // end of run()


   private void processClient(BufferedReader in, PrintWriter out)
   /* Stop when the input stream closes (is null) or "bye" is sent
      Otherwise pass the input to doRequest(). */
   {
     String line;
     boolean done = false;
     try {
       while (!done) {
         if((line = in.readLine()) == null)
           done = true;
         else {
           System.out.println("Client (" + cliAddr + ", " + 
								port + "): " + line);
           if (line.trim().equals("bye"))
             done = true;
           else 
             doRequest(line, out);
         }
       }
     }
     catch(IOException e)
     {  System.out.println(e);  }
   }  // end of processClient()


  private void doRequest(String line, PrintWriter out)
  /*  The input line (client message) can be :
			who				-- a list of users is returned
      or	any text		-- which is broadcast with
							   (cliAddr,port) at its front 
  */
  { if (line.trim().toLowerCase().equals("who")) {
      System.out.println("Processing 'who'");
      out.println( cg.who() );
    }
    else  // use ChatGroup object to broadcast the message
      cg.broadcast( "("+cliAddr+", "+port+"): " + line);
  }  // end of doRequest()


}  // end of ChatServerHandler class


