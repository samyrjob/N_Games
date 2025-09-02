package Chat_Threaded;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;


public class ChatClient extends JFrame implements ActionListener
{
  private static final int PORT = 1234;     // server details
  private static final String HOST = "localhost";

  private Socket sock;
  private PrintWriter out;  // output to the server

  private JTextArea jtaMesgs;  // output text area
  private JTextField jtfMsg;   // for sending messages
  private JButton jbWho;       // for sending a "who" message


  public ChatClient()
  {
     super("Chat Client");
     initializeGUI();
     // makeContact();

     addWindowListener( new WindowAdapter() {
       public void windowClosing(WindowEvent e)
       { closeLink(); }
     });

     setSize(300,450);
     setVisible(true);

     makeContact();    // change: moved so window visible before contact
  } // end of ChatClient();


  private void initializeGUI()
  /* Text area in center, and controls below.
     Controls:
         - textfield for entering messages
         - a "Who" button
  */
  {
    Container c = getContentPane();
    c.setLayout( new BorderLayout() );

    jtaMesgs = new JTextArea(7, 7);
    jtaMesgs.setEditable(false);
    JScrollPane jsp = new JScrollPane( jtaMesgs);
    c.add( jsp, "Center");

    JLabel jlMsg = new JLabel("Message: ");
    jtfMsg = new JTextField(15);
    jtfMsg.addActionListener(this);    // pressing enter triggers sending of name/score

    jbWho = new JButton("Who");
    jbWho.addActionListener(this);

    JPanel p1 = new JPanel( new FlowLayout() );
    p1.add(jlMsg); p1.add(jtfMsg);

    JPanel p2 = new JPanel( new FlowLayout() );
    p2.add(jbWho);

    JPanel p = new JPanel();
    p.setLayout( new BoxLayout(p, BoxLayout.Y_AXIS));
    p.add(p1); p.add(p2);

    c.add(p, "South");

  }  // end of initializeGUI()


  private void closeLink()
  {
    try {
      out.println("bye");    // tell server that client is disconnecting
      sock.close();
    }
    catch(Exception e)
    {  System.out.println( e );  }

    System.exit( 0 ); 
  } // end of closeLink()

    
  private void makeContact()
  // contact the server and start a ChatWatcher thread
  {
    try {
      sock = new Socket(HOST, PORT);
      BufferedReader in  = new BufferedReader( 
		  		new InputStreamReader( sock.getInputStream() ) );
      out = new PrintWriter( sock.getOutputStream(), true );  // autoflush

      new ChatWatcher(this, in).start();    // start watching for server msgs
    }
    catch(Exception e)
    {  System.out.println(e);  }
  }  // end of makeContact()



   public void actionPerformed(ActionEvent e)
   /* Either a message is to be sent or the "Who"
      button has been pressed. */
   {
     if (e.getSource() == jbWho)
       out.println("who");   // the response is read by ChatWatcher
     else if (e.getSource() == jtfMsg)
       sendMessage();
   }


  private void sendMessage()
  /* Check if the user has supplied a message, then
     send it to the server. */
  {
    String msg = jtfMsg.getText().trim();
    // System.out.println("'"+msg+"'");

    if (msg.equals(""))
      JOptionPane.showMessageDialog( null, 
           "No message entered", "Send Message Error", 
			JOptionPane.ERROR_MESSAGE);
    else {
      out.println(msg);
      // showMsg("Sent: " + msg + "\n");
    }
  }  // end of sendMessage()


/*
  synchronized public void showMsgFoo(String msg)
  // Synchronized since this method can be called by this
  //   object and the URLChatWatcher thread.
  {  jtaMesgs.append(msg);  }
*/


  public void showMsg(final String msg)
  /* We're updating the messages text area, so the code should
     be carried out by Swing's event dispatching thread, which is 
     achieved by calling invokeLater(). 

     msg must be final to be used inside the inner class for Runnable.

     showMsg() may be called by this object and the ChatWatcher 
     thread, but the updates are serialised by being placed in the
     queue associated with the event dispatcher. This means that
     there's no need to synchronize this method.

     Thanks to Rachel Struthers (rmstruthers@mn.rr.com)
  */
  { 
    // System.out.println("showMsg(): " + msg);
    Runnable updateMsgsText = new Runnable() {
      public void run() 
      { jtaMesgs.append(msg);  // append message to text area
        jtaMesgs.setCaretPosition( jtaMesgs.getText().length() );
            // move insertion point to the end of the text
      }
    };
    SwingUtilities.invokeLater( updateMsgsText );
  } // end of showMsg()


  // ------------------------------------

  public static void main(String args[]) 
  {  new ChatClient();  }

} // end of ChatClient class


