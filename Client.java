import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

class Upload extends Thread
{
	String ip;
	int port;
	File file;
	JTextArea log;
	JProgressBar progressBar;
	JTable table;
	Upload(String ip , int port , FileTransferData fileData , JTextArea log , JTable table)
	{
		this.table=table;
		this.ip = ip;
		this.port = port;
		this.file = fileData.file;
		this.progressBar = fileData.progressBar;
		this.log = log;
		log.append(" Sending File : \nFile Name= "+file.getName()+"\nFile Size= "+file.length()+"\n");
		start();
	}
	public void run()
	{
		try
		{
			Socket socket = new Socket(this.ip , this.port);
			
			String name = file.getName();
			long size = file.length();

			System.out.println(name+" "+size);

			DataOutputStream os=new DataOutputStream(socket.getOutputStream());
			os.writeUTF(name);
			os.writeLong(size);

			byte[] buffer = new byte[1024];

            FileInputStream fis = new FileInputStream(file);

            long totalBytes=0;
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
                double progress = (double) totalBytes / size;
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue((int)(progress*100));
					progressBar.repaint();
                	table.updateUI();
                });
            }
            os.flush();
            System.out.println("File sent to server: " + name);
            log.append("File Sent!!\n\n");
            os.close();
            fis.close();
            socket.close();
		}catch(Exception e)
		{
			System.out.println(e);
		}
	}
}

class UploadManager extends Thread
{
	private String ip;
	private int port;
	private Vector< FileTransferData > fileTransferList;
	private JTextArea log;
	private JTable table;
	public UploadManager(String ip , int port , Vector< FileTransferData > fileTransferList , JTextArea log , JTable table)
	{
		this.ip = ip;
		this.port = port;
		this.fileTransferList = fileTransferList;
		this.log = log;
		this.table=table;
		start();
	}
	public void run()
	{
		int rows = fileTransferList.size();
		FileTransferData fileData;
		for(int i = 0 ; i < rows ; i++)
		{
			fileData = fileTransferList.elementAt(i);
			new Upload(this.ip , this.port , fileData , this.log , this.table);
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

class FileTransferData 
{
    public File file;
    public JProgressBar progressBar;

    FileTransferData(File file) 
    {
        this.file = file;
        this.progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
    }
}

public class Client extends JFrame
{
	private JLabel ip;
	private JTextField ipTextField;
	private JLabel portLabel;
	private JTextField portNumberTextField;
	private JTextArea log;
	private JTable table;
	private JScrollPane logScrollPane;
	private JScrollPane tableScrollPane;
    private Container container;
    private DefaultTableModel model;
    private JButton open;
    private JButton clear;
    private JButton connect;
    private JLabel statusLabel;
    private JButton send;
	private Vector< FileTransferData > fileTransferList = new Vector< FileTransferData >();

    public void reset()
    {
        connect.setEnabled(true);
        model.setRowCount(0);
        table.updateUI();
        ipTextField.setEnabled(true);
        portNumberTextField.setEnabled(true);
        statusLabel.setText("Status : not connected");
    }

    private Client()
    {
    	super("Client");
    	ip = new JLabel("IP :- ");
    	ipTextField = new JTextField();
    	portLabel = new JLabel("Port :- ");
    	portNumberTextField = new JTextField();
    	log = new JTextArea();
    	log.setEditable(false);
    	log.append("Welcome!!\n\n");
    	connect = new JButton("Connect");
    	clear = new JButton("Clear");
    	statusLabel = new JLabel("Status : not connected");
    	
    	open = new JButton("Open");
    	send = new JButton("Send");	 


    	String[] columnNames = {"File Name", "Progress"};
		model = new DefaultTableModel(columnNames,0);
	    table = new JTable(model);

	    //table.setFont(new Font("SansSerif",Font.BOLD,20));

	    open.addActionListener(new ActionListener()
	    {
	    	public void actionPerformed(ActionEvent ae)
	    	{
	    		JFileChooser fileChooser = new JFileChooser("C:/Users/Lenovo YC/Downloads");
	    		fileChooser.setMultiSelectionEnabled(true);
                int answer = fileChooser.showOpenDialog(null);

                if(answer == JFileChooser.APPROVE_OPTION) addFileToTable(fileChooser.getSelectedFiles());
                table.updateUI();
	    	}
	    });

	    send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	ipTextField.setEnabled(false);
            	portNumberTextField.setEnabled(false);
            	statusLabel.setText("Status : Connected");
            	log.append("Connected to \nIP : "+ipTextField.getText()+"\nPort : "+Integer.parseInt(portNumberTextField.getText())+"\n\n");
            	new UploadManager(ipTextField.getText() , Integer.parseInt(portNumberTextField.getText()) , fileTransferList , log , table);
            }
        });

        clear.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ae)
        	{
        		model.setRowCount(0);
				table.updateUI();
        	}
        });

	    tableScrollPane = new JScrollPane(table , ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		logScrollPane = new JScrollPane(log , ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
	    container = getContentPane();
	    container.setLayout(null);
	    int tm = 10;
	    int lm = 10;
	    ip.setBounds(lm+60 , tm , 50 , 30);
	    ipTextField.setBounds(lm+100 , tm , 100 , 30);
	    portLabel.setBounds(lm+205 , tm , 50 , 30);
        portNumberTextField.setBounds(lm+245 , tm , 60 , 30);
        connect.setBounds(lm+315 , tm , 90 , 30);
        logScrollPane.setBounds(lm+10 , tm+30+10 , 460 , 100);
        tableScrollPane.setBounds(lm+10 , tm+30+10+100+10 , 460 , 250);
        clear.setBounds(lm+380 , tm+30+10+100+10+250+10 , 80 , 30);
        send.setBounds(lm+290 , tm+30+10+100+10+250+10 , 80 , 30);
        open.setBounds(lm+200 , tm+30+10+100+10+250+10 , 80 , 30);
	    
	    container.add(ip);
	    container.add(ipTextField);
	    container.add(portLabel);
	    container.add(portNumberTextField);
	    container.add(connect);
	    container.add(logScrollPane);
	    container.add(tableScrollPane);
	    container.add(clear);
	    container.add(open);
	    container.add(send);

	    int w = 500;
        int h = 500;
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(d.width/2-w/2 , d.height/2-h/2);
        setSize(w , h);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

    }

    private void addFileToTable(File[] files) {
    	for(File file:files)
    	{
        	FileTransferData fileTransferData = new FileTransferData(file);
        	fileTransferList.add(fileTransferData);
        	model.addRow(new Object[]{file.getName() , fileTransferData.progressBar});
        }
	    table.getColumnModel().getColumn(1).setCellRenderer(new ProgressBarRenderer());
    }

	public static void main(String gg[])
	{
		Client client = new Client();
	}
}