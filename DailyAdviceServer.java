import java.io.*;
import java.net.*;

public class DailyAdviceServer {

    String[] adviceList = {"Take smaller bites", "Go for the tight jeans. fNo they do NOT make you look fat.",
    "One word: innappropiate", "Just for today, be honest. Tell your boss what you *really* think",
            "You might want to rethink that haircut"};

    public void go(){

        try {
            ServerSocket serverSock = new ServerSocket(4242);
            //ServerSocket makes this server app "listen" for client
            //requests on port 4242 on the machine this code is running on

            while (true) { //The server goes into a permanent loop, waiting
                //for and servicing client requests
                Socket sock = serverSock.accept(); //the accept method blocks
                // just sits there until a request comes in, and then the method returns
                //a socket (on some anonymous port) for communicating with the client

                PrintWriter writer = new PrintWriter(sock.getOutputStream());
                String advice = getAdvice();
                writer.println(advice); // now we use the socket connection to the
                //client to make a PrintWriter and send it (println()) a String advice
                //message. Then we close the socket beacause we're done with this client.
                writer.close();
                System.out.println(advice);
            }
        } catch (IOException ex) { ex.printStackTrace();}
    }

    private String getAdvice() {
        int random = (int) (Math.random() * adviceList.length);
        return adviceList[random];
    }

    public static void main (String[] args){
        DailyAdviceServer server = new DailyAdviceServer();
        server.go();
    }
}
