import javax.swing.*; // GUI
import java.awt.*;
import java.awt.event.*;

import java.io.*; //STREAMS
import java.net.*;

public class SimpleChatClientA {

    JTextField outgoing;
    PrintWriter writer;
    Socket sock;

    public void go() {
        JFrame frame = new JFrame("Simple Chat Client, ludicrously simple");
        JPanel mainPanel = new JPanel();
        outgoing = new JTextField(15);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        setUpNetworking();
        frame.setSize(400, 100);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void setUpNetworking() {
        try {
            sock = new Socket("127.0.0.1", 5000); //using localhost for testing
            writer = new PrintWriter(sock.getOutputStream()); //This is where we make the socket
            System.out.println("networking succesfully established");//and the PrintWriter (it's called from the go() method right
        } catch (IOException ex) {ex.printStackTrace();} // before displaying the app GUI
    }

    public class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try{
                writer.println(outgoing.getText());
                writer.flush();

                //Now we actually do the writing. Remember, the writer
                //is chained to the input stream from the socket, so whenever we do a println().
                // it goes over the network to the server!

            }catch (Exception ex){ex.printStackTrace();}

            outgoing.setText("message sent");
            outgoing.requestFocus();
        }
    }

    public static void main (String[] args){
        new SimpleChatClientA().go();
    }
}

