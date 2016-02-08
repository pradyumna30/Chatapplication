package chat;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * The Server class is the backbone of the chat room.
 * Multiple clients will connect to the server and the 
 * connections will be maintained on individual threads
 */

public class Server 
{
	/* To create unique ID's for each connection */
	private static int _connId;
	/* List of clients connected */
	private ArrayList<ClientThread> clients;
	/* GUI mode flag */
	private ServerGUI sgui;
	/* To display current time */
	private SimpleDateFormat sdf;
	/* Port number for server to listen */
	private int port;
	/* To stop the server */
	private boolean keepOn;
	/* Backup to file*/
	private static File file = new File("backup.txt");
	private static File file2 = new File("cliconn.txt");
	private FileWriter fw, fw2;

	ObjectInputStream ois;
	ObjectOutputStream oos;

	/* Constructor that sets the value of port and GUI object*/
	public Server(int port, ServerGUI sgui)
	{
		this.sgui = sgui;
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		clients = new ArrayList<ClientThread>();
	}
	
	/* 
	 * Starts the server 
	 */
	public void wakeup()
	{
		keepOn = true;
		try
		{
			if(!file.exists()) 
				file.createNewFile();
			if(!file2.exists())
				file2.createNewFile();
		}
		catch(Exception e)
		{
			show("Exception "+e);
		}
		
		try
		{
			/* Create the main server socket */
			ServerSocket servSoc = new ServerSocket(port);

			while(keepOn)
			{
				show("Waiting for clients on port "+ port +".");
				/* Accept incoming connection request from client */
				Socket connSoc = servSoc.accept();

				/* Stop if Server is to be stopped */
				if(!keepOn)
					break;

				/* Set a new thread for the new connection */
				ClientThread clientThread = new ClientThread(connSoc);

				/* Add the connection to the client list */
				clients.add(clientThread);
				clientThread.start();
			}

			/* Stop the server*/
			try
			{
				servSoc.close();
				/* Close all connections to the clients */
				for(int i=0; i< clients.size(); ++i) 
				{
					ClientThread clientThread = clients.get(i);
					try
					{
						clientThread.sInput.close();
						clientThread.sOutput.close();
						clientThread.connSoc.close();
					}
					catch(IOException e)
					{
						/* Do not handle */
					}
				}
			}
			catch(Exception e)
			{
				show("Unable to close the clients ");
			}
			
		}
		catch(IOException e)
		{
			show("Exception on new ServerSocket ");
		}
	}

	/* 
	 * Stops the server 
	 */
	@SuppressWarnings("resource")
	protected void stop()
	{
		keepOn = false;
		/* Try connecting to self as a Client */
		try {
			new Socket("localhost", port);
		}
		catch(Exception e)
		{
			/* Do nothing */
		}
		file.delete();
	}

	/* 
	 * Display an event on the console 
	 * Input
	 * 		-Message to be displayed
	 */
	private void show(String msg)
	{
		String timeMes = sdf.format(new Date()) + " " + msg;
		/* Check if GUI is enabled else output on console */
		sgui.appendEvent(timeMes + "\n");
	}

	/* 
	 * Broadcast a message to all clients
	 * Synchronized is used to make the method thread synchronous. So that
	 * multiple client threads can use the method safely 
	 */
	private synchronized void broadcast(String message)
	{
		String time = sdf.format(new Date());
		String messTime = time + " " + message + "\n";
		BufferedWriter bw;
		PrintWriter pw;

		if(messTime.indexOf('~')!=-1)
		{
			sgui.appendRoom(messTime, ChatObject.PRIO);
		}
		else
			sgui.appendRoom(messTime, ChatObject.MESSAGE);
		try{
			/* Backup data to a file */
			fw = new FileWriter(file.getAbsoluteFile(),true);
			
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.print(messTime);
			pw.close();
		}
		catch(Exception e)
		{
			System.out.println("Unable to backup data\n");
		}

		/* Remove client if write fails and client has disconnected */
		for(int i=clients.size(); --i>=0;)
		{
			ClientThread clientThread = clients.get(i);
			if(!clientThread.blocked)
				if(!clientThread.writeMsg(messTime))
				{
					clients.remove(i);
					show("Disconnected client "+ clientThread.username + " removed from list.");
				}			
		}
	}
	
	/*
	 * Sends a broadcast message addressed to a user
	 */
	private synchronized void broadcast(String message, String intended, String username)
	{
		int flag = 0, flag2=0;
		String time = sdf.format(new Date());
		String messTime = time +" " + message + " to "+intended+ "@\n";
		BufferedWriter bw;
		PrintWriter pw;

		if(messTime.indexOf('~')!=-1)
		{
			sgui.appendRoom(messTime, ChatObject.PRIO);
		}
		
		else
		sgui.appendRoom( messTime, ChatObject.DIR);
		try{
			/* Backup data to a file */
			fw = new FileWriter(file.getAbsoluteFile(),true);
			
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.print(messTime);
			pw.close();
		}
		catch(Exception e)
		{
			System.out.println("Unable to backup data\n");
		}
		int id=-1;
		try{
			id = Integer.parseInt(intended);
		}
		catch(NumberFormatException e)
		{
			flag2=1;
		}
		show(id+"\n");
		/* Remove client if write fails and client has disconnected */
		for(int i=clients.size(); --i>=0;)
		{
			ClientThread clientThread = clients.get(i);
			if(flag2==1)
			{
				if(clientThread.username.equalsIgnoreCase(intended))
				{
					flag = 1;
					if(clientThread.blocked)
					{
						messTime = "User "+intended+" is blocked\n";
						flag=2;
					}
					break;	
				}
			}
			else
			{
				if(clientThread.id == id)
				{
					flag = 1;
					intended = clientThread.username;
					messTime = time +" " + message + " to "+intended+ "@\n";
					if(clientThread.blocked)
					{
						messTime = "User "+intended+" is blocked\n";
						flag=2;
					}
					break;	
				}
			}
		}
		if(flag==0)
		{
			messTime = "No such user\n";
			flag=2;
		}
		if(flag == 2)
		{
				
				for(int i=clients.size(); --i>=0;)
				{
					ClientThread clientThread = clients.get(i);
					if(clientThread.username.equalsIgnoreCase(username))
					{
						if(!clientThread.blocked)
							if(!clientThread.writeMsg(messTime))
							{
								clients.remove(i);
								show("Disconnected client "+ clientThread.username + " removed from list.");
							}		
					}
				}
		}
		else
		{
			/* Remove client if write fails and client has disconnected */
			for(int i=clients.size(); --i>=0;)
			{
				ClientThread clientThread = clients.get(i);
				if(!clientThread.blocked)
					if(!clientThread.writeMsg(messTime))
					{
						clients.remove(i);
						show("Disconnected client "+ clientThread.username + " removed from list.");
					}
			}
		}
	}
	
	/* 
	 * Private chat
	 * Unicast a message to the intended client 
	 */
	private synchronized void unicast(String message, String intended, String username)
	{
		int flag = 0, flag2=0;
		String time = sdf.format(new Date());
		String messTime = time + " " + message + "\n";
		sgui.appendRoom(messTime, ChatObject.PRIVMESS);

		int id=-1;
		try{
			id = Integer.parseInt(intended);
		}
		catch(NumberFormatException e)
		{
			flag2=1;
		}
		
		/* Remove client if write fails and client has disconnected */
		for(int i=clients.size(); --i>=0;)
		{
			ClientThread clientThread = clients.get(i);
			if(flag2==1)
			{
				if(clientThread.username.equalsIgnoreCase(intended))
				{
					if(!clientThread.blocked)
					{
						if(!clientThread.writeMsg(messTime+'#'))
						{
							clients.remove(i);
							show("Disconnected client "+ clientThread.username + " removed from list.");
						}
					}
					else
						messTime = "User "+intended+" is blocked";
					flag = 1;
					break;
				}
			}
			else
			{
				if(clientThread.id==id)
				{
					if(!clientThread.blocked)
					{
						if(!clientThread.writeMsg(messTime+'#'))
						{
							clients.remove(i);
							show("Disconnected client "+ clientThread.username + " removed from list.");
						}
					}
					else
						messTime = "User "+intended+" is blocked";
					flag = 1;
					break;
				}
			}
		}
		if(flag == 0)
		{
				messTime = "No such user\n";
		}
		/* Send message back to the sender as well */
		for(int i=clients.size(); --i>=0;)
			{
				ClientThread clientThread = clients.get(i);
				if(clientThread.username.equalsIgnoreCase(username))
				{
					if(!clientThread.blocked)
						if(!clientThread.writeMsg(messTime+'#'))
						{
							clients.remove(i);
							show("Disconnected client "+ clientThread.username + " removed from list.");
						}		
				}
			}
		
	}

	/*
	 *  If a client logs off using LOGOUT message 
	 */
	synchronized void remove(int id) 
	{
		for(int i=0; i<clients.size(); ++i)
		{
			ClientThread clientThread = clients.get(i);
			if(clientThread.id == id)
			{
				clients.remove(i);
				return;
			}
		}
	}
	
	synchronized private void fetch(String target, String username) {
		int flag=0;
		String mess="Previous users connected at ";
		String ip;
		for(int i=clients.size(); --i>=0;)
		{
			ClientThread clientThread = clients.get(i);
			if(clientThread.username.equalsIgnoreCase(target))
			{
				if(!clientThread.blocked)
				{
					ip = clientThread.ip;
					mess = mess.concat(ip+" are:\n");
					BufferedReader br = null;
					try {
						br = new BufferedReader(new FileReader(file2.getAbsoluteFile()));
					} catch (FileNotFoundException e1) {}
					String current, ipsrc, usersrc;
					try {
						while((current=br.readLine())!=null)
						{
							ipsrc = current.substring(0, current.indexOf('@'));
							usersrc = current.substring(current.indexOf('@')+1);

							if(ip.equals(ipsrc))
							{
								mess = mess.concat(usersrc+"\n");
							}
						}
					} catch (IOException e) {
					}
					finally {
						try {
							br.close();
						} catch (IOException e) {
						}
					}
					flag=1;
					break;
				}
			}
			
		}
		if(flag==0)
		{
			mess = "No such user\n";
		}

		for(int i=clients.size(); --i>=0;)
		{
			ClientThread clientThread = clients.get(i);
			if(clientThread.username.equalsIgnoreCase(username))
			{
				if(!clientThread.blocked)
					if(!clientThread.writeMsg(mess))
					{
						clients.remove(i);
						show("Disconnected client "+ clientThread.username + " removed from list.");
					}		
			}
		}
	}

	synchronized void adder(String username, String ip)
	{
		String time = sdf.format(new Date());
		try{
			fw2 = new FileWriter(file2.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw2);
			PrintWriter pw = new PrintWriter(bw);
			pw.print(ip+'@'+time+" "+username+"\n");
			pw.close();
		}
		catch(IOException f)
		{
		}
	}
	
	/*
	 * Inner class for connecting to the clients
	 */
	class ClientThread extends Thread 
	{
		/* Socket for communication */
		Socket connSoc;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		/* Connection ID */
		int id;
		/* Client username */
		String username;
		String intended;
		/* Chat message */
		ChatObject chatObject;
		/* Date */
		String date;
		BufferedReader br;
		String ip;
		boolean blocked;
		
		/* Constructor */
		public ClientThread(Socket connSoc) {
			id = ++_connId;
			blocked = false;
			this.connSoc = connSoc;
			ip = connSoc.getInetAddress().getHostAddress();
			
			/* Creation of i/o datastreams */
			try
			{
				sInput = new ObjectInputStream(connSoc.getInputStream());
				sOutput = new ObjectOutputStream(connSoc.getOutputStream());
				/* fetch the username */
				username = (String) sInput.readObject()+id;
				show(username + " just joined the chat room.");
				this.writeMsg("Server assigned you the alias: $"+username+"*\n");
				broadcast(username + " just joined the chat room.*");
				br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
				
				String current;
				while((current = br.readLine()) != null)
				{
					this.writeMsg(current+"\n");
				}
			}

			catch(IOException e)
			{
				show("Unable to create new I/O streams");
				return;
			} 
			catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
			adder(username, ip);
		}
		
		/* runner */
		public void run() {
			boolean keepOn = true;
			int counter=0;
			long time = 0;
			while(keepOn)
			{
				/* Read input as object */
				try {
					chatObject = (ChatObject) sInput.readObject();
				} 
				catch (IOException e) {
					show(username+" left the chatroom");
					broadcast(username+" left the chatroom*");
					break;
				}
				catch (ClassNotFoundException e)
				{
					break;
				}
				
				if(!blocked)
				{
					/* Message handling */
					String message = chatObject.getMessage();
					if(counter == 0)
					{
						time = System.currentTimeMillis();
					}
					counter++;
					if(counter==5)
					{
						time = System.currentTimeMillis()-time;
						if(time<2000)
						{
							this.writeMsg("Please slow down! You have been blocked for 10 sec due to spamming\n");
							this.blocked = true;
							message = ""; 
							try {
								sleep(10000);
							} catch (InterruptedException e) {
								show("Exception in wait");
							}
							try {
								sInput.reset();
							} catch (IOException e) {
							}
							this.blocked = false;
							this.writeMsg("You have been unblocked\n");
						}
						counter=0;
					}
					
					String intended = chatObject.getIntended();
					switch (chatObject.getType()) {
					case ChatObject.MESSAGE:
						broadcast(username + ", " + message);
						break;
						
					case ChatObject.LOGOUT:
						show(username + " disconnected with a LOGOUT message.");
						broadcast(username+" left the chatroom*");
						keepOn = false;
						break;
						
					case ChatObject.WHO:
						writeMsg("List of the users connected at "+ sdf.format(new Date())+ "\n");
						/* Scan all connected users */
						for(int i=0; i<clients.size(); ++i)
						{	
							ClientThread clientThread = clients.get(i);
							writeMsg((i+1) + ") " + clientThread.username + " since " + clientThread.date);
						}
						break;
						
					case ChatObject.PRIVMESS:
						/* Call unicast method */
						unicast(username + " says " + message, intended, this.username);
						break;
						
					case ChatObject.PRIO:
						/* Call broadcast */
						broadcast(username + ", " + message);
						break;
						
					case ChatObject.DIR:
						/*Call broadcast with directed */
						broadcast(username + " says " + message, intended, username);
						break;
						
					case ChatObject.BLOCK:
						block(message);
						break;
					
					case ChatObject.UNBLOCK:
						unblock(message);
						break;
					
					case ChatObject.FETCH:
						fetch(message, username);
						break;
					}
				}
			}
			remove(id);
			close();
		}

		private void block(String target) {
			int flag=0;
			String mess=null;
			for(int i=clients.size(); --i>=0;)
			{
				ClientThread clientThread = clients.get(i);
				if(clientThread.username.equalsIgnoreCase(target))
				{
					if(!clientThread.blocked)
					{
						mess = "User "+target+" has been blocked by admin";
						broadcast(mess+'*');
						clientThread.blocked = true;
						flag=1;
						break;
					}
				}
			}
			if(flag==0)
			{
				mess = "No such user\n";
				writeMsg(mess);
			}
		}
		
		private void unblock(String target) {
			int flag=0;
			String mess=null;
			for(int i=clients.size(); --i>=0;)
			{
				ClientThread clientThread = clients.get(i);
				if(clientThread.username.equalsIgnoreCase(target))
				{
					if(clientThread.blocked)
					{
						clientThread.blocked = false;
						mess = "User "+target+" has been unblocked by admin";
						broadcast(mess+'*');
						flag=1;
						
						break;
					}
				}
			}
			if(flag==0)
			{
				mess = "No such user\n";
				writeMsg(mess);
			}	
		}

		/*
		 * Writes a message to the output stream
		 */
		private boolean writeMsg(String msg) {
			/* if Client is still connected send the message to it */
			if(!connSoc.isConnected()) {
				close();
				return false;
			}
			/* write the message to the stream */
			try {
					sOutput.writeObject(msg);
			}
			/* if an error occurs, do not abort just inform the user */
			catch(IOException e) {
				show("Error sending message to " + username);
			}
			return true;
		}

		/*
		 * Close all the streams and socket connection
		 */
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(connSoc != null) connSoc.close();
			}
			catch (Exception e) {}
		}
	}
		
}