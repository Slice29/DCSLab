import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;


class MessageSender implements Runnable {
    public final static int PORT = 4445;
    private DatagramSocket sock;
    private String hostname;

    MessageSender(DatagramSocket s, String h) {
        sock = s;
        hostname = h;
    }
    public void sendMessage(String s) throws Exception {
        byte buf[] = s.getBytes();
        InetAddress address = InetAddress.getByName(hostname);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
        sock.send(packet);
    }

    public void run() {
        boolean connected = false;
        do {
            try {
                sendMessage("CONNECTED");
                connected = true;
            } catch (Exception e) {

            }
        } while (!connected);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                while (!in.ready()) {
                    Thread.sleep(100);
                }
                sendMessage(in.readLine());
            } catch(Exception e) {
                System.err.println(e);
            }
        }
    }
}
class MessageReceiver implements Runnable {
    DatagramSocket sock;
    byte buf[];
    MessageReceiver(DatagramSocket s) {
        sock = s;
        buf = new byte[1024];
    }
    public void run() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                sock.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println(received);
            } catch(Exception e) {
                System.err.println(e);
            }
        }
    }
}

public class ChatClient {
    static JTextArea ta = new JTextArea();

    public static void main(String args[]) throws Exception {
        String host = "localhost";
        DatagramSocket socket = new DatagramSocket();
        MessageReceiver r = new MessageReceiver(socket);
        MessageSender s = new MessageSender(socket, host);
        Thread rt = new Thread(r);
        Thread st = new Thread(s);
        rt.start(); st.start();

        JFrame frame = new JFrame("Chat App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,400);

        frame.getContentPane().add(BorderLayout.CENTER, ta);
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter a message: ");
        JTextField tf = new JTextField(10);

        JButton send = new JButton("Send");
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MessageSender s = new MessageSender(socket, host);
                try {
                    s.sendMessage(tf.getText());
                    tf.setText("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton reset = new JButton("Clear");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tf.setText("");
            }
        });

        panel.add(label); // Components Added using Flow Layout
        panel.add(tf);
        panel.add(send);
        panel.add(reset);
        frame.getContentPane().add(BorderLayout.SOUTH, panel);

        frame.setVisible(true);

        final PrintStream sysOut = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) throws
                    IOException {
                ta.append(String.valueOf((char)b));
                sysOut.write(b);
            }
        }));
    }
}