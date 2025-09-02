package Chat_Threaded;

import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    public final int PORT = 1234;

    // create chatgroup object here
    private ChatGroup cg;

    public ChatServer(){
        // wait for a client connection, spawn a thread and repeat

        cg = new ChatGroup();
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            Socket clientSocket;

            while (true){
                System.out.println("Waiting for a client");
                clientSocket = serverSocket.accept();
                // spawn a new chat server handler thread
                new ChatServerHandler(clientSocker, cg).start();
            }
        }
        catch(Exception e)
        {  System.out.println(e);  }
    }

      public static void main(String args[]) 
    {  new ChatServer();  }

    
}
