package Java_Servlet_HTTP_Client_Server;

import java.io.*;
import java.net.*;

public class TimeClient {
    public static void main(String[] args) {
        try {
            // URL of the servlet (Tomcat running on localhost:8100)
            URL url = new URL("http://localhost:8080/Java_Servlet_HTTP_Client_Server/TimeServlet");

            // open stream to servlet
            BufferedReader br = new BufferedReader(
                new InputStreamReader(url.openStream())
            );

            // read server response line by line
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

