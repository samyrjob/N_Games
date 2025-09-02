package Java_Servlet_HTTP_Client_Server;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class TimeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // set response content type
        response.setContentType("text/plain");

        // format current date/time
        SimpleDateFormat formatter = new SimpleDateFormat("d M yyyy HH:mm:ss");
        Date today = new Date();
        String todayStr = formatter.format(today);

        // log to server console
        System.out.println("Today is: " + todayStr);

        // send response to client
        PrintWriter output = response.getWriter();
        output.println(todayStr);
        output.close();
    }
}
