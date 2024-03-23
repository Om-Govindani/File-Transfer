import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

class Download extends Thread
{
    private Socket socket;
    private Server server;
    public Download(Socket socket , Server server)
    {
        this.socket=socket;
        this.server=server;
        start();
    }
    public void run()
    {
        try
        {
            DataInputStream is = new DataInputStream(socket.getInputStream());
            
            String name = is.readUTF();
            System.out.println(name);
            long size = is.readLong();

            this.server.log.append("Recieveing File : \nFile Name = "+name+"\nsize = "+size+"\n");

            File file = new File(name);
            if(file.exists()) file.delete();

            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];

            int bytesRead;
            long totalBytes = 0;   
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setValue(0);
            server.model.addRow(new Object[]{name, progressBar});

            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
                double progress = (double) totalBytes / size;
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue((int) (progress * 100));
                    progressBar.repaint();
                    this.server.table.updateUI();
                });
            }
            progressBar.setValue(100);
            fos.close();
            is.close();
            socket.close();
            this.server.log.append("Recieved Succesfully\n\n");
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
}
class DownloadManager extends Thread
{
    private int port;
    private Server server;

    public DownloadManager(int port , Server server)
    {
        try
        {
            this.port = port;
            this.server = server;
            this.server.serverSocket = new ServerSocket(this.port);
            this.server.log.append("Server Started!!\n");
            start();
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public void run()
    {
        try
        {
            while(this.server.isStarted)
            {    
                new Download(this.server.serverSocket.accept() , this.server);
            }
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
}

class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {

    ProgressBarRenderer() {
        setStringPainted(true); // Show progress as text
    }
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Check if the value is a JProgressBar instance
        if (value instanceof JProgressBar) {
            JProgressBar progressBar = (JProgressBar) value;
            setValue(progressBar.getValue());
            setString(progressBar.getString());
        }
        return this;
    }
}

public class Server extends JFrame
{
	public Boolean isStarted;
	private JLabel portLabel;
	private JTextField portNumberTextField;
	private JButton startStopButton;
	public JTextArea log;
	public JTable table;
	private JScrollPane logScrollPane;
	private JScrollPane tableScrollPane;
    private Container container;
    public ServerSocket serverSocket;
    public DefaultTableModel model;
    private JButton clear;

	public Server()
	{
		super("Server App");
		portLabel = new JLabel("Port :- ");
		portNumberTextField = new JTextField();
		log = new JTextArea();
		log.setEditable(false);
		log.append("Welcome!!\n");
		startStopButton = new JButton("Start");
		clear = new JButton("Clear");
		isStarted = false;
		String[] columnNames = {"File Name", "Progress"};
		model = new DefaultTableModel(columnNames, 0);
	    table = new JTable(model);
        table.getColumnModel().getColumn(1).setCellRenderer(new ProgressBarRenderer());

		startStopButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                if(!isStarted)
                {
                    portNumberTextField.setEnabled(false);
                    isStarted = true;
                    startStopButton.setText("Stop");
                    log.append("Server recieving at Port : "+Integer.parseInt(portNumberTextField.getText())+"\n\n");
                    new DownloadManager(Integer.parseInt(portNumberTextField.getText()) , Server.this);
                }
                else
                {
                    try
        			{
            			serverSocket.close();
        			}catch(Exception exception){System.out.println(exception);}
                    isStarted = false;
                    startStopButton.setText("Start");
                    portNumberTextField.setEnabled(true);
                    portNumberTextField.setText("");
                    log.append("Server stopped!!!!!");
                }
            }
        });

		clear.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				model.setRowCount(0);
				table.updateUI();
			}
		});

        tableScrollPane=new JScrollPane( table , ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		logScrollPane=new JScrollPane( log , ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        int lm = 10;
        int tm = 10;
        container=getContentPane();
        portLabel.setBounds(lm+250 , tm , 50 , 30);
        portNumberTextField.setBounds(lm+290 , tm , 80 , 30);
        startStopButton.setBounds(lm+380 , tm , 80 , 30);
        logScrollPane.setBounds(lm+10 , tm+30+10 , 460 , 100);
        tableScrollPane.setBounds(lm+10 , tm+30+10+100+10 , 460 , 250);
        clear.setBounds(lm+380 , tm+30+10+100+10+250+10 , 80 , 30);

        container=getContentPane();
        container.setLayout(null);
        container.add(portLabel);
        container.add(portNumberTextField);
        container.add(startStopButton);
        container.add(logScrollPane);
        container.add(tableScrollPane);
        container.add(clear);

        int w = 500;
        int h = 500;
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(d.width/2-w/2 , d.height/2-h/2);
        setSize(w , h);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

	}

	public static void main(String gg[])
	{
		Server server=new Server();
	}
}