package ChatMulticasting;


import java.io.*;
import java.net.*;
import java.util.*;


public class NameServer
{
  private static final String GROUP_HOST = "228.5.6.7";
      // the multicast group address sent to new members

  private static final int PORT = 1234;      // for this server
  private static final int BUFSIZE = 1024;   // max size of a message

  private DatagramSocket serverSock;

  private ArrayList groupMembers;  
    // holds the names of the current members of the multicast group

  
  public NameServer()
  {
    try {  // try to create a socket for the server
      serverSock = new DatagramSocket(PORT);
    }
    catch(SocketException se)
    {  System.out.println(se);
       System.exit(1);
    }

    groupMembers = new ArrayList();
    waitForPackets();
  } // end of NameServer()


  private void waitForPackets()
  // wait for a packet, process it, repeat
  {
    DatagramPacket receivePacket;
    byte data[];

    System.out.println("Ready for client messages");
    try {
      while (true) {
        data = new byte[BUFSIZE];  // set up an empty packet
        receivePacket = new DatagramPacket(data, data.length);
        serverSock.receive( receivePacket );  // wait for a packet

        // extract client address, port, message
        InetAddress clientAddr = receivePacket.getAddress();
        int clientPort = receivePacket.getPort();

        String clientMsg = new String( receivePacket.getData(), 0,
                           receivePacket.getLength() ).trim();
        System.out.println("Received: " + clientMsg);

        processClient(clientMsg, clientAddr, clientPort);
      }
    }
    catch(IOException ioe)
    {  System.out.println(ioe);  }
  }  // end of waitForPackets()


  private void processClient(String msg, InetAddress addr, int port)
  /* There are three possible types of message:
       hi <name>   // add the name to the group list (if name is unique)
                      and tell them the multicast address

       bye <name>  // delete this name from the group list

       who         // send back a list of all the current members
  */
  {
    if (msg.startsWith("hi")) {
      String name = msg.substring(2).trim();
      if (name != null && isUniqueName(name)) {
          groupMembers.add(name);
          sendMessage(GROUP_HOST, addr, port);  // send multicast addr
      }
      else
        sendMessage("no", addr, port);
    }
    else if (msg.startsWith("bye")) {
      String name = msg.substring(3).trim();
      if (name != null)
          removeName(name);  // removes name from list
      // note: nothing is done to check the actual multicast group
      // or to verify who sent this message   
    }
    else if (msg.equals("who"))
      sendMessage( listNames(), addr, port);
    else
      System.out.println("Do not understand the message");

  }  // end of processClient()


  private void sendMessage(String msg, InetAddress addr, int port)
  // send message back to the client
  {
    try {
      DatagramPacket sendPacket =
          new DatagramPacket( msg.getBytes(), msg.length(), 
   						addr, port);
      serverSock.send( sendPacket );
    }
    catch(IOException ioe)
    {  System.out.println(ioe);  }
  } // end of sendMessage()


  // ------------------- names processing -------------------


  private boolean isUniqueName(String nm)
  // return true if nm is not already in the group members list
  {
    String name;
    for(int i=0; i < groupMembers.size(); i++) {
      name = (String) groupMembers.get(i);
      if (name.equals(nm))
        return false;
    }
    return true;
  } // end of isUniqueName()


  private void removeName(String nm)
  // remove nm from the group members list
  {
    String name;
    for(int i=0; i < groupMembers.size(); i++) {
      name = (String) groupMembers.get(i);
      if (name.equals(nm)) {
        groupMembers.remove(i);
        break;
      }
    }
  }  // end of removeName()

  
  private String listNames()
  /* Return the group members list as a string of the form
        no. name \n no1. name1 \n ...
  */
  { String list = new String();
    String name;
    for(int i=0; i < groupMembers.size(); i++) {
      name = (String) groupMembers.get(i);
      list += "" + (i+1) + " " + name + "\n";
    }
    return list;
  } // end of listNames()


  // ------------------------------------

  public static void main(String args[]) 
  {  new NameServer();  }


} // end of NameServer class

