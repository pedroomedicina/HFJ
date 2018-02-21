// if you want to chat, you must compile and run MusicServer.java
// To compile properly use a cmd terminal -> type cd + file path to navigate to the folder -> type javac BeatBox.java -> type java BeatBox "username" (without quotation marks)
// you can have more than one BeatBox running. One per cmd terminal.

import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.event.*;

public class BeatBox {

    JFrame laVentana;
    JPanel panelPrincipal;
    JList listaEntrantes;
    JTextField mensajeUsuario;
    ArrayList<JCheckBox> checkboxList;
    int numSiguiente;
    Vector<String> listaVectores = new Vector<String>();
    String nombreUsuario;
    ObjectOutputStream sale;
    ObjectInputStream entra;
    HashMap<String, boolean[]> otroMapaSecuencia = new HashMap<String, boolean[]>();

    Sequencer secuenciador;
    Sequence secuencia;
    Sequence miSecuencia = null;
    Track track;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
            "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
            "Open Hi Conga"};
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        new BeatBox().startUp(args[0]); // args[0] es el ID de usuario / nombre para mostrar
    }

    public void startUp(String nombre) {
        nombreUsuario = nombre;
        setUpMidi();
        buildGUI();
        //abrir la conexion al servidor
        try {
            Socket sock = new Socket("127.0.0.1", 4242);
            sale = new ObjectOutputStream(sock.getOutputStream());
            entra = new ObjectInputStream(sock.getInputStream());
            Thread remoto = new Thread(new LectorRemoto());
            remoto.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("no se pudo conectar, tendras que jugar solo");
        }

    }

    public void buildGUI() {
        laVentana = new JFrame("Pedro's Cyber Beatbox");
        laVentana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton sendIt = new JButton("sendIt");
        sendIt.addActionListener(new MySendListener());
        buttonBox.add(sendIt);

        mensajeUsuario = new JTextField();
        buttonBox.add(mensajeUsuario);

        listaEntrantes = new JList();
        listaEntrantes.addListSelectionListener(new MyListSelectionListener());
        listaEntrantes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane laLista = new JScrollPane(listaEntrantes);
        buttonBox.add(laLista);
        listaEntrantes.setListData(listaVectores); // sin datos para iniciar.

        Box nameBox = new Box(BoxLayout.Y_AXIS);

        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        laVentana.getContentPane().add(background);

        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        panelPrincipal = new JPanel(grid);
        background.add(BorderLayout.CENTER, panelPrincipal);

        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            panelPrincipal.add(c);
        }


        laVentana.setBounds(50, 50, 300, 300);
        laVentana.pack();
        laVentana.setVisible(true);
    }

    public void setUpMidi() {
        try {
            secuenciador = MidiSystem.getSequencer();
            secuenciador.open();
            secuencia = new Sequence(Sequence.PPQ, 4);
            track = secuencia.createTrack();
            secuenciador.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buildTrackAndStart() {
        ArrayList<Integer> tracklist = null; //Esto tendra los valores de los instrumentos

        secuencia.deleteTrack(track);
        track = secuencia.createTrack();

        for (int i = 0; i < 16; i++) {
            tracklist = new ArrayList<Integer>();

            for (int j = 0; j < 16; j++) {

                JCheckBox jc = (JCheckBox) checkboxList.get(j + (16 * i));
                if (jc.isSelected()) {
                    int key = instruments[i];
                    tracklist.add(new Integer(key));
                } else {
                    tracklist.add(null); // porque este box no deberia estar marcado
                }
            }
            makeTracks(tracklist);
        }
        track.add(makeEvent(192, 9, 1, 0, 15));
        try {
            secuenciador.setSequence(secuencia);
            secuenciador.setLoopCount(secuenciador.LOOP_CONTINUOUSLY);
            secuenciador.start();
            secuenciador.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            buildTrackAndStart();
        }
    }

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            secuenciador.stop();
        }
    }

    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = secuenciador.getTempoFactor();
            secuenciador.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = secuenciador.getTempoFactor();
            secuenciador.setTempoFactor((float) (tempoFactor * 0.97));
        }
    }

    public class MySendListener implements ActionListener {

        public void actionPerformed(ActionEvent a) {
            // hacer un arraylist con el estado de las checkboxes
            boolean[] checkboxState = new boolean[256];
            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if (check.isSelected()) {
                    checkboxState[i] = true;
                }
            }

            String mensajePorEnviar = null;
            try {
                sale.writeObject(nombreUsuario + numSiguiente++ + ": " + mensajeUsuario.getText());
                sale.writeObject(checkboxState);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Lo siento, no se pudo enviar al servidor");
            }
            mensajeUsuario.setText("");
        }
    }

    public class MyListSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent le) {
            if (!le.getValueIsAdjusting()) {
                String seleccionado = (String) listaEntrantes.getSelectedValue();
                if (seleccionado != null) {
                    //Ve al mapa y cambia la secuencia
                    boolean[] estadoSeleccionado = (boolean[]) otroMapaSecuencia.get(seleccionado);
                    changeSequence(estadoSeleccionado);
                    secuenciador.stop();
                    buildTrackAndStart();
                }
            }
        }
    }

    public class LectorRemoto implements Runnable {
        boolean[] checkboxState = null;
        String nombreParaMostrar = null;
        Object obj = null;

        public void run() {
            try {
                while ((obj = entra.readObject()) != null) {
                    System.out.println("got an object from server");
                    System.out.println(obj.getClass());
                    nombreParaMostrar = (String) obj;
                    checkboxState = (boolean[]) entra.readObject();
                    otroMapaSecuencia.put(nombreParaMostrar, checkboxState);
                    listaVectores.add(nombreParaMostrar);
                    listaEntrantes.setListData(listaVectores);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class MyPlayMineListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            if (miSecuencia != null) {
                secuencia = miSecuencia; //restaurar al original
            }
        }
    }

    public void changeSequence(boolean[] checkboxState) {
        for (int i = 0; i < 256; i++) {
            JCheckBox check = (JCheckBox) checkboxList.get(i);
            if (checkboxState[i]) {
                check.setSelected(true);
            } else {
                check.setSelected(false);
            }
        }
    }

    public void makeTracks(ArrayList list) {

        Iterator it = list.iterator();
        for (int i = 0; i < 16; i++) {
            Integer num = (Integer) it.next();
            if (num != null) {
                int numKey = num.intValue();
                track.add(makeEvent(144, 9, numKey, 100, i));
                track.add(makeEvent(128, 9, numKey, 100, i + 1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

}
