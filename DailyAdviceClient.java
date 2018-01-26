import java.io.*;
import java.net.*; //class socket is in java.net

public class DailyAdviceClient {

    public void go() {
        try { //a lot can go wrong here
            Socket s = new Socket("127.0.0.1", 4242);

            InputStreamReader streamReader = new InputStreamReader(s.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader); //chain a
            //BufferedReader to an InputStreamReader to the input stream from the socket.

            String advice = reader.readLine(); // this readLine() is EXACTLY
            //the same as if you were using a BufferedReader chained to a FILE.
            //In other owrds, by the time you call a BufferedWriter method, the writer
            //doesn't know or care where the characters came from.

            System.out.println("Today you should: " + advice);
            reader.close(); //this closes ALL the streams.
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    public static void main(String[] args ){

        DailyAdviceClient client = new DailyAdviceClient();
        client.go();
    }
}
