import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimpleChatClient {

    JTextArea entrantes;
    JTextField salientes;
    BufferedReader lector;
    PrintWriter escritor;
    Socket sock;

    public static void main (String[] args){
        SimpleChatClient cliente = new SimpleChatClient();
        cliente.go();
    }

    public void go(){

        JFrame ventana = new JFrame("Chat simple");
        JPanel panelPrincipal= new JPanel();
        entrantes = new JTextArea(15,50);
        entrantes.setLineWrap(true);
        entrantes.setWrapStyleWord(true);
        entrantes.setEditable(false);
        JScrollPane qScroller = new JScrollPane(entrantes);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        salientes = new JTextField(20);
        JButton botonEnviar = new JButton("Enviar");
        botonEnviar.addActionListener (new SendButtonListener());
        panelPrincipal.add(qScroller);
        panelPrincipal.add(salientes);
        panelPrincipal.add(botonEnviar);

        armarConexiones();

        Thread hiloLector = new Thread(new LectorEntrantes());
        hiloLector.start();

        ventana.getContentPane().add(BorderLayout.CENTER, panelPrincipal);
        ventana.setSize (400, 500);
        ventana.setVisible(true);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void armarConexiones(){

        try{
            sock = new Socket("127.0.0.1",5000);
            InputStreamReader lectorStream = new InputStreamReader(sock.getInputStream());
            lector = new BufferedReader(lectorStream);
            escritor = new PrintWriter(sock.getOutputStream());
            System.out.println("Conexiones creadas correctamente");
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try{
                escritor.println(salientes.getText());
                escritor.flush();
            } catch (Exception ex){
                ex.printStackTrace();
            }

            salientes.setText("");
            salientes.requestFocus();
        }

    }

    public class LectorEntrantes implements Runnable {
        public void run () {
            String mensaje;
            try {
                while ((mensaje = lector.readLine()) != null) {
                    System.out.println("leído " + mensaje);
                    entrantes.append(mensaje + "/n");
                }
            } catch (IOException e) {e.printStackTrace();}
        }
    }
}
