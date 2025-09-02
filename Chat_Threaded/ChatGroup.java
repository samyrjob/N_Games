package Chat_Threaded;

import java.io.PrintWriter;
import java.util.ArrayList;

public class ChatGroup {

    private ArrayList chatPeople;

    public ChatGroup(){
        this.chatPeople = new ArrayList();
    }

    synchronized public void addPerson(String cliAddr, int port, PrintWriter out){
        // add a neW Chatter object to the list 
        chatPeople.add( new Chatter(cliAddr, port, out));  
        broadcast("Welcome a new chatter ("+cliAddr+", "+port+")");

    }

    synchronized public void delPerson(String cliAddr, int port){
        Chatter c;
        for(int i=0; i < chatPeople.size(); i++) {
        c = (Chatter) chatPeople.get(i);
        if (c.matches(cliAddr, port)) {
            chatPeople.remove(i);
            broadcast("("+cliAddr+", "+port+") has departed");
            break;
        }
        }

    }

    synchronized public void broadcast(String msg)
    /* Send msg to all the clients, including back to the
        original sender. */
    {
        Chatter c;
        for(int i=0; i < chatPeople.size(); i++) {
        c = (Chatter) chatPeople.get(i);
        c.sendMessage(msg);
        }
    }  // end of broadcast()


    synchronized public String who()
    /*  Returns a string of who is currently logged on, in the form
            "WHO$$ cliAddr1 & port1 & ... cliAddrN & portN & "
    */
    { Chatter c;
        String whoList = "WHO$$ ";
        for(int i=0; i < chatPeople.size(); i++) {
        c = (Chatter) chatPeople.get(i);
        whoList += c.toString();
        }
        return whoList;
    }  // end of who()


    



    


    
}
