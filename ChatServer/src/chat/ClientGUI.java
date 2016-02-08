package chat;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.awt.event.*;

/* client is able to run on Graphical interface using console */
/*
* 	 To start the Client using console mode use following command
* 	 > java chat.Client
*	 Note -chat is the package name where the class file is present
*/

public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	/* will first hold username , later on enter message */
	private JLabel label, label2;	
	/* to hold the username latter on the message */
	private JTextField tf;
	private JPasswordField tf2;
	/* to hold server address and port number */
	private JTextField tfServer, tfPort;
	/* to logout or to get the list of user */
	private JButton login , logout, whoIsIn, block, unblock, fetch, away;
	/* for chat room */
	private JTextPane chatrum;
	/* it is for connection */
	private boolean connected;
	/* client object */
	private Client client;
	/* defaut port number */
	private int defaultPort;
	private String defaultHost;
	private StyledDocument doc;
	private SimpleAttributeSet aset, aset2;
	private JPanel moodPanel, southPanel, northPanel;
	
	// constructor connection receiving socket number
	
	ClientGUI(String host,int port){
		
		super("Quack - Client");
		
		defaultPort = port;
		defaultHost=host;
		/* The north panel */
		
		northPanel = new JPanel(new GridLayout(13,1));
		/* the server name and port number */
		JPanel serverAndPort = new JPanel(new GridLayout(1,5,1,3));
		/* the two JTextField with default value for server address and port number */
		tfServer = new JTextField(host);
		tfServer.setEditable(true);
		tfPort = new JTextField("" + port);
		tfPort.setEditable(true);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
		
		serverAndPort.add(new JLabel("Server Address :     "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		/* adds the server and port field to the GUI */
		
		northPanel.add(serverAndPort);
		/* Label and Textfield */
		
		label= new JLabel("Enter Your Username Below",SwingConstants.CENTER);
		northPanel.add(label);
		
		tf=new JTextField("anon");
		tf.setForeground(Color.black);
		tf.setBackground(Color.white);
		northPanel.add(tf);
		

		label2= new JLabel("Password",SwingConstants.CENTER);
		northPanel.add(label2);
		
		tf2=new JPasswordField();
		tf2.setForeground(Color.black);
		tf2.setBackground(Color.white);
		northPanel.add(tf2);

		northPanel.add(new JLabel("Private Message : #[Name of user] [Message]"), BorderLayout.NORTH);
		northPanel.add(new JLabel("Priority Message : ~[Message]"), BorderLayout.NORTH);
		northPanel.add(new JLabel("Broadcast Message : [Message]"), BorderLayout.NORTH);
		northPanel.add(new JLabel("Broadcast Message addressed to user : @[Name of user] [Message]"), BorderLayout.NORTH);
		northPanel.add(new JLabel("Info Message: [Message]*"), BorderLayout.NORTH);
		northPanel.add(new JLabel("Spammers will be blocked for 10 sec"), BorderLayout.NORTH);
		northPanel.add(new JLabel("Set mood: ;) Happy ;P Mischievous ;( Sad ;| Angry"), BorderLayout.NORTH);
		add(northPanel, BorderLayout.NORTH);
		
		/* the centerPanel which is the chat room */
		chatrum = new JTextPane();
	
		JPanel centerPanel = new JPanel(new GridLayout(2, 1));
		centerPanel.add(new JScrollPane(chatrum));
		chatrum.setEditable(false);
		/*for setting text style */
		doc=chatrum.getStyledDocument();
		aset=new SimpleAttributeSet();
		aset2=new SimpleAttributeSet();
		
		moodPanel = new JPanel(new BorderLayout());
		centerPanel.add(new JScrollPane(moodPanel));
		add(centerPanel, BorderLayout.CENTER);
		
		/* 3 button */
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);
		/* login is required before logout */
		whoIsIn = new JButton("Who is in");
		whoIsIn.addActionListener(this);
		whoIsIn.setEnabled(false);
		away = new JButton("Away");
		away.addActionListener(this);
		away.setEnabled(false);
		
		/* user have to login before being able to see who is in */
		southPanel = new JPanel();
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(whoIsIn);
		southPanel.add(away);
		add(southPanel, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(750,600);
		setVisible(true);
		tf.requestFocus();
		append("Welcome to Chat Room \n",ChatObject.MESSAGE);
	}
	
	/* called by the client to append text in the textpane */
	void append(String str,int Type){
		String str2="", str3="", str4="";
		Font first;
		String arr[];
		first=new Font("Comic Sans MS",Font.PLAIN,13);
		StyleConstants.setFontFamily(aset2, first.getFamily());
		StyleConstants.setFontSize(aset2, first.getSize());
		StyleConstants.setAlignment(aset2, StyleConstants.ALIGN_LEFT);
		StyleConstants.setBold(aset2, false);
		StyleConstants.setItalic(aset2,false);
		StyleConstants.setForeground(aset2, Color.BLACK);
		switch(Type){
		/* checking for broadcast message */
		case ChatObject.MESSAGE:
			first=new Font("Comic Sans MS",Font.PLAIN,15);
			StyleConstants.setFontFamily(aset, first.getFamily());
			StyleConstants.setFontSize(aset, first.getSize());
			StyleConstants.setAlignment(aset, StyleConstants.ALIGN_LEFT);
			StyleConstants.setBold(aset, true);
			StyleConstants.setItalic(aset,false);
			StyleConstants.setForeground(aset, Color.blue);
			arr = str.split(",");
			if(arr.length>1)
			{
				str2= arr[1];
				str = arr[0];
			}
			else
			{
				str2=str;
				str="";
			}
			break;
		/* checking for the private message */
		case ChatObject.PRIVMESS:
			first=new Font("Comic Sans MS",Font.PLAIN,15);
			StyleConstants.setFontFamily(aset, first.getFamily());
			StyleConstants.setFontSize(aset, first.getSize());
			StyleConstants.setAlignment(aset, StyleConstants.ALIGN_LEFT);
			StyleConstants.setBold(aset, false);
			StyleConstants.setItalic(aset,true);
			StyleConstants.setForeground(aset, Color.green);
			arr = str.split("says");
			if(arr.length>1)
			{
				str=arr[0]+" says (privately)";
				str2=arr[1];
			}
			else
			{
				str2=str;
				str="";
			}
			break;
		/* checking for the High Priority message */
		case ChatObject.PRIO:
			first=new Font("Comic Sans MS",Font.PLAIN,20);
			StyleConstants.setFontFamily(aset, first.getFamily());
			StyleConstants.setFontSize(aset, first.getSize());
			StyleConstants.setItalic(aset,false);
			StyleConstants.setBold(aset, true);
			StyleConstants.setForeground(aset, Color.red);
			arr = str.split(",");
			if(arr.length>1)
			{
				str2= arr[1];
				str = arr[0];
			}
			else
			{
				str2=str;
				str="";
			}
			break;
		case ChatObject.DIR:
			first=new Font("Comic Sans MS",Font.PLAIN,20);
			StyleConstants.setFontFamily(aset, first.getFamily());
			StyleConstants.setFontSize(aset, first.getSize());
			StyleConstants.setItalic(aset,false);
			StyleConstants.setBold(aset, true);
			StyleConstants.setForeground(aset, Color.DARK_GRAY);
			str=str.replace('@', ' ');
			arr = str.split("says");
			if(arr.length>1)
			{
			str=arr[0]+" says ";
			arr = arr[1].split("to");
			str3 = arr[0];
			str4 = " to "+arr[1];
			}
			else
			{
				str2=str;
				str="";
			}
			break;
		case ChatObject.INFO:
			first=new Font("Comic Sans MS",Font.PLAIN,12);
			StyleConstants.setFontFamily(aset, first.getFamily());
			StyleConstants.setFontSize(aset, first.getSize());
			StyleConstants.setItalic(aset,false);
			StyleConstants.setBold(aset, true);
			StyleConstants.setForeground(aset, Color.CYAN);
			if(str.indexOf('$')!=-1)
			{
				String temp = str.substring(str.indexOf('$')+1);
				str = str.replace('$', ' ');
				temp = temp.replace('*', ' ');
				label.setText("Hi "+temp+ "enter your message below");
			}
			str2=str;
			str="";
			break;
		}
		
		doc.setParagraphAttributes(0, 0, aset, false);
		try{
			doc.insertString(doc.getLength(), str, aset2);
			doc.insertString(doc.getLength(), str2, aset);
			doc.insertString(doc.getLength(), str3, aset);
			doc.insertString(doc.getLength(), str4, aset2);
			int ind;
			if(str2.indexOf(';')!=-1)
			{
				
				ind = str2.indexOf(';');
				switch(str2.charAt(ind+1))
				{
				case 'P':
					moodPanel.setBackground(Color.yellow);
					moodPanel.removeAll();
					moodPanel.add(new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(";P.png")).getScaledInstance(100, 100, Image.SCALE_DEFAULT))),BorderLayout.CENTER);
					moodPanel.revalidate();
					moodPanel.repaint();
					break;
				case '(':
					moodPanel.removeAll();
					moodPanel.setBackground(Color.blue);
					moodPanel.add(new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(";(.png")).getScaledInstance(100, 100, Image.SCALE_DEFAULT))),BorderLayout.CENTER);
					moodPanel.revalidate();
					moodPanel.repaint();
					break;
				case '|':
					moodPanel.removeAll();
					moodPanel.setBackground(Color.red);
					moodPanel.add(new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(";|.png")).getScaledInstance(100, 100, Image.SCALE_DEFAULT))),BorderLayout.CENTER);
					moodPanel.revalidate();
					moodPanel.repaint();
					break;
				case ')':
					moodPanel.removeAll();
					moodPanel.setBackground(Color.green);
					moodPanel.add(new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(";).png")).getScaledInstance(100, 100, Image.SCALE_DEFAULT))),BorderLayout.CENTER);
					moodPanel.revalidate();
					moodPanel.repaint();
					break;
				}
			}
		}
		catch(BadLocationException e){
			System.out.println("unable to write to chatrum");
			}
		chatrum.setCaretPosition(doc.getLength()-1);
		}
	
	
	/* called by the GUI is the connection failed */
	/* we reset our buttons label textfield */
	void connectionFailed(){
		login.setEnabled(true);
		logout.setEnabled(false);
		whoIsIn.setEnabled(false);
		away.setEnabled(false);
		label.setText("Enter Your Username Below");
		tf.setText("anon");
		/* reset port number and host name */
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		/* let the user change them */
		tfServer.setEditable(true);
		tfPort.setEditable(true);
		/* don't react to a change request after the username */
		tf.removeActionListener(this);
		connected = false;
		}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		/* if it is the Logout button */
		if(o == logout) {
			client.sendMessage(new ChatObject(ChatObject.LOGOUT, ""));
			tfServer.setEditable(true);
			tfPort.setEditable(true);
			new ClientGUI("localhost", 1500);
			this.dispose();
			return;
		}
		/* if it the who is in button */
		if(o == whoIsIn) {
			client.sendMessage(new ChatObject(ChatObject.WHO, ""));				
			return;
		}
		if(o==block)
		{
			String target = tf.getText();
			tf.setText("");
			tf2.setText("");
			if(target.length()==0)
				return;
			client.sendMessage(new ChatObject(ChatObject.BLOCK, target));
			return;
		}
		if(o==unblock)
		{
			String target = tf.getText();
			tf.setText("");
			tf2.setText("");
			if(target.length()==0)
				return;
			client.sendMessage(new ChatObject(ChatObject.UNBLOCK, target));
			return;
		}
		if(o==fetch)
		{
			String target = tf.getText();
			tf.setText("");
			tf2.setText("");
			if(target.length()==0)
				return;
			client.sendMessage(new ChatObject(ChatObject.FETCH, target));
			return;
		}
		if(o==away)
		{
			if(client.awayFlag==true)
			{
				away.setText("Away");
				tf.setEditable(true);
				client.sendMessage(new ChatObject(ChatObject.MESSAGE, "Back online"));
				client.awayFlag =false;
			}
			else
			{
				away.setText("Available");
				tf.setEditable(false);
				client.sendMessage(new ChatObject(ChatObject.MESSAGE, "Be right back"));
				client.awayFlag =true;
			}
		}
		/* ok it is coming from the JTextField */
		if(connected) {
			/* just have to send the message */
			String msg=tf.getText();
			/* checking for private message */
			if(!msg.isEmpty())
			{
				msg = msg.trim();
			if(msg.indexOf('#')!=-1){
				client.sendMessage(new ChatObject(ChatObject.PRIVMESS,msg));
				}
			/* checking for High Priority messagse */
			else if(msg.indexOf('~')!=-1){
				client.sendMessage(new ChatObject(ChatObject.PRIO, msg));
				}
			else if(msg.indexOf('@')!=-1){
				client.sendMessage(new ChatObject(ChatObject.DIR, msg));
			}
		else{	
			client.sendMessage(new ChatObject(ChatObject.MESSAGE, tf.getText().trim()));				
		}
			}
			tf.setText("");
			return;
		}
		if(o == login) {
			/* ok it is a connection request */
			String username = tf.getText().trim();
			char[] pass = tf2.getPassword();
			String password = new String(pass);
			/* empty user name ignore it */
			if(username.length() == 0)
				return;
			if(username.equals("admin"))
			{
				if(password.equals("quack"))
				{
					tf2.setEditable(false);
					block = new JButton("Block");
					block.addActionListener(this);
					unblock = new JButton("Unblock");
					unblock.addActionListener(this);
					fetch = new JButton("Fetch Terminal History");
					fetch.addActionListener(this);
					southPanel.add(block);
					southPanel.add(unblock);
					southPanel.add(fetch);
					southPanel.setBackground(new Color(224,255,255));
					southPanel.revalidate();
					southPanel.repaint();
					northPanel.add(new JLabel("Block users: [username] and click on \"Block\" button"));
					northPanel.add(new JLabel("Unlock users: [username] and click on \"Unblock\" button"));
					northPanel.add(new JLabel("Terminal history: [username] and click on \"Fetch Terminal History\" button"));
					northPanel.setBackground(new Color(224,255,255));
				}
				else
				{
					append("Incorrect password\n", ChatObject.INFO);
					return;
				}
			}
			/* empty serverAddress ignore it */
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			/* empty or invalid port number, ignore it */
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;  
			/* nothing I can do if port number is not valid */
			}

			/* try creating a new Client with GUI */
			client = new Client(server, port, username, this);
			/* test if we can start the Client */
			if(!client.start()) 
				{ 
					System.out.println("Client not started");
					return;
				}
			tf.setText("");
			tf2.setText("");
			label.setText("Hi "+username+" enter your message below");
			connected = true;
			
			/* disable login button */
			login.setEnabled(false);
			/* enable the 2 buttons */
			logout.setEnabled(true);
			whoIsIn.setEnabled(true);
			away.setEnabled(true);
			/* disable the Server and Port JTextField */
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			/* Action listener for when the user enter a message */
			tf.addActionListener(this);
			northPanel.remove(label2);
			northPanel.remove(tf2);
			revalidate();
			repaint();
		}

		

	}

	/* to start the whole thing the server */
	public static void main(String[] args) {
		new ClientGUI("localhost", 1500);
	}

}


