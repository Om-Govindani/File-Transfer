import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

class Server extends JFrame
{
    public Server()
    {
        initComponents();
    }

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataInputStream inputStream;
    private FileOutputStream fileOutputStream;

    private void initComponents()
    {
        setTitle("Server");
        setBackground(Color.red);
        setIconImage(new ImageIcon("Server.png").getImage());
        Font titleFont=new Font("Verdana", Font.BOLD,18);
		Font dataFont=new Font("Times New Roman", Font.PLAIN,18);
        Container container;
        JLabel startLabel = new JLabel("Start Server :- ");
        startLabel.setFont(titleFont);
        JButton startButton = new JButton("Start");
        startButton.setFont(dataFont);
        JButton clearButton = new JButton("Clear");
        clearButton.setFont(dataFont);
        JProgressBar progressBar=new JProgressBar();
        JTextArea txt = new JTextArea();
        txt.setFont(dataFont);
        txt.setEditable(false);
        txt.setColumns(20);
        txt.setRows(5);
        JScrollPane jScrollPane1= new JScrollPane();
        jScrollPane1.setViewportView(txt);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        startButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                new Thread(() -> {
                    try {
                        txt.setText("Server Has Started Working !!\n");
                        serverSocket = new ServerSocket(5555); // Port number
                        clientSocket = serverSocket.accept();
                        txt.append("New Client Joined at Port 4568\n");
                        inputStream = new DataInputStream(clientSocket.getInputStream());
                        receiveFile(txt,progressBar);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt.setText("");
            }
        });

        container = getContentPane();
		container.setLayout(null);

		int lm,tm;
		int w=630; 
		int h=600;
		lm=10;
		tm=0;

        startLabel.setBounds(lm+165,tm+10,160,30);
        startButton.setBounds(lm+165+160+5,tm+10,80,30);
        txt.setBounds(lm+10,tm+10+30+10,580,400);
        progressBar.setBounds(lm+10,tm+10+30+10+400+10,580,40);
        clearButton.setBounds(lm+10,tm+10+30+10+450+10,80,30);

        container.add(startLabel);
        container.add(startButton);
        container.add(txt);
        container.add(progressBar);
        container.add(clearButton);

        setSize(w,h);
		Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width/2)-(w/2),(d.height/2)-(h/2));
		setVisible(true);
    }
    
    private void receiveFile(JTextArea txt, JProgressBar progressBar) {
        try {
            String fileName = inputStream.readUTF();
            long fileSize = inputStream.readLong();
            txt.append("Started Receiving the File \n");
            txt.append("File Name : "+fileName+"\nFile Size : "+fileSize+"\n");
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = fileChooser.getSelectedFile();
                File file = new File(selectedDirectory.getAbsolutePath() + File.separator + fileName);
                fileOutputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead<fileSize) {
                    bytesRead=inputStream.read(buffer,0,buffer.length);
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    final int progress = (int) ((double) totalBytesRead / fileSize * 100);
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                }
                SwingUtilities.invokeLater(() -> progressBar.setValue(100));
                txt.append("File received successfully.\n");
            } else {
                System.out.println("File transfer canceled.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String gg[])
    {
        new Server();
    }
}