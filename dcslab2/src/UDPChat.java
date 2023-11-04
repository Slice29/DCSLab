import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPChat {

    private static JTextField userInput1, userInput2;
    private static JTextArea chatArea1, chatArea2;

    public static void main(String[] args) {
        // Setup first chat window
        createChatWindow("User 1", 5000, 5001);

        // Setup second chat window
        createChatWindow("User 2", 5001, 5000);
    }

    private static void createChatWindow(String title, int sendPort, int receivePort) {
        JFrame frame = new JFrame(title + " - Sending to port " + sendPort);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        frame.getContentPane().add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        JTextField userInput = new JTextField(30);
        JButton sendButton = new JButton("Send");
        panel.add(userInput);
        panel.add(sendButton);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = userInput.getText();
                sendMessage(message, sendPort);
                chatArea.append("Me: " + message + "\n");
                userInput.setText("");
            }
        });

        Thread receiveThread = new Thread(() -> {
            try {
                receiveMessages(receivePort, chatArea);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });

        receiveThread.start();

        frame.setVisible(true);
    }

    private static void sendMessage(String message, int sendPort) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, sendPort);
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receiveMessages(int port, JTextArea chatArea) throws Exception {
        DatagramSocket socket = new DatagramSocket(port);
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (true) {
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            SwingUtilities.invokeLater(() -> {
                chatArea.append("Them: " + message + "\n");
            });
        }
    }
}
