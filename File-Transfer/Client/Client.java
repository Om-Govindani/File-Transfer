//package client;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
public class Client extends JFrame
{
	public Socket socket;
	public OutputStreamWriter out;
	public DataInputStream dis;
	public DataOutputStream dos;
	public Client()
	{
		initComponents();
	}
	private void initComponents()
	{
		setTitle("Client Application");
		Font titleFont=new Font("Verdana", Font.PLAIN,18);
		Font dataFont=new Font("Times New Roman", Font.PLAIN,18);
		JLabel jLabel1 = new JLabel("Enter IP :- ");
		jLabel1.setFont(titleFont);
		JButton connect= new JButton("Connect");
		connect.setFont(dataFont);
		JScrollPane jScrollPane1=new JScrollPane();
		JTextArea txt=new JTextArea();
		txt.setFont(dataFont);
		JButton open=new JButton("Open");
		open.setFont(dataFont);
		//JButton send=new JButton("Send");
		//send.setFont(dataFont);
		JButton clear=new JButton("Clear");
		clear.setFont(dataFont);
		JTextField IP=new JTextField();
		JProgressBar progressBar=new JProgressBar();
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		txt.setEditable(false);
		txt.setColumns(25);
		txt.setRows(10);
		jScrollPane1.setViewportView(txt);

		connect.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent AE){
				try 
				{
					socket = new Socket(IP.getText().trim(), 5555);
					txt.append("Connected successfully ...\n");
				}catch (Exception e) {
					JOptionPane.showMessageDialog(Client.this, e, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		open.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent AE){
				JFileChooser ch = new JFileChooser();
				int c = ch.showOpenDialog(Client.this);
				if (c == JFileChooser.APPROVE_OPTION) 
				{   //new Thread(()->{
					try
					{
						File f = ch.getSelectedFile();
						dos = new DataOutputStream(socket.getOutputStream());
						sendFile(f,txt,progressBar);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(Client.this, e, "Error", JOptionPane.ERROR_MESSAGE);
					}//}).start();
				}
			}
		});

		clear.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent AE)
			{
				txt.setText("");
			}
		});

		Container container = getContentPane();
		container.setLayout(null);

		int lm,tm;
		int w=630; 
		int h=600;
		lm=10;
		tm=0;
		jLabel1.setBounds(lm+150,tm+10,110,30);
		IP.setBounds(lm+265,tm+10,100,30);
		connect.setBounds(lm+365+5,tm+10,110,30);
		txt.setBounds(lm+10,tm+10+30+10,580,400);
		progressBar.setBounds(lm+10,tm+10+30+10+400+10,580,40);
		open.setBounds(lm+10,tm+10+30+10+450+10,80,30);
		clear.setBounds(lm+510,tm+10+30+10+450+10,80,30);

		container.add(jLabel1);
		container.add(IP);
		container.add(connect);
		container.add(txt);
		container.add(open);
		container.add(progressBar);
		container.add(clear);
		
		setSize(w,h);
		Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width/2)-(w/2),(d.height/2)-(h/2));
		setVisible(true);
	}

	private void sendFile(File file , JTextArea txt , JProgressBar progressBar) 
	{
        try 
		{
            String fileName = file.getName();
            long fileSize = file.length();

            SwingUtilities.invokeLater(() -> {
                txt.setText("Sending file: " + fileName);
            });

            dos.writeUTF(fileName);
            dos.writeLong(fileSize);

            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytesRead = 0;

            while (totalBytesRead<fileSize) {
				bytesRead = fileInputStream.read(buffer);
                dos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
				System.out.println(totalBytesRead+" "+bytesRead);
				final int progress = (int) ((double) totalBytesRead / fileSize * 100);
                SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
            }
			SwingUtilities.invokeLater(() -> progressBar.setValue(100));
			txt.append("File Sent SuccessFully!!");
            fileInputStream.close();
        } catch (IOException e) 
		{
            e.printStackTrace();
        }
    }

	public static void main(String gg[])
	{
		new Client();
	}
}

