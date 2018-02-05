import java.io.*;
import java.net.*;
import java.util.*;

public class MusicServer {

    ArrayList<ObjectOutputStream> clientOutputStreams;

    public static void main (String[] args) {
        new MusicServer().go();
    }

    public class ClientHandler implements Runnable {
        ObjectInputStream entra;
        Socket clientSocket;

        public ClientHandler (Socket socket) {
            try{
                clientSocket = socket;
                entra = new ObjectInputStream(clientSocket.getInputStream());
            }catch(Exception ex){ex.printStackTrace();}
        }

        public void run() {
            Object o2 = null;
            Object o1 = null;
            try{
                while ((o1 = entra.readObject())!= null) {

                    o2 = entra.readObject();

                    System.out.println("Leídos 2 objetos");
                    tellEveryone(o1, o2);
                }
            }catch(Exception ex){ex.printStackTrace();}
        }
    }

    public void go() {
        clientOutputStreams = new ArrayList<ObjectOutputStream>();

        try{
            ServerSocket serverSock = new ServerSocket(4242);

            while (true) {
                Socket clientSocket = serverSock.accept();
                ObjectOutputStream sale = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.add(sale);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();

                System.out.println("got a connection");
            }
        }catch(Exception ex){ex.printStackTrace();}
    }

    public void tellEveryone (Object one, Object two){
        Iterator it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            try {
                ObjectOutputStream sale = (ObjectOutputStream) it.next();
                sale.writeObject(one);
                sale.writeObject(two);
            } catch (Exception ex) {ex.printStackTrace();}
            }
        }

}
