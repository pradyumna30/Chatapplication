package chat;

import java.net.*;
import java.io.*;


public class Client{
		
		/* Input Output */ 
		public ObjectInputStream sIn;     		
		/* To read from the Socket */
		public ObjectOutputStream sOut;			
		/* To write into socket */
		private Socket socket;
		private ClientGUI cGUI;     			
		/* It is used for Graphical user interface*/
		private String server,username;
		private int port;
		public volatile boolean awayFlag;
		
		/* calls the common constructor with the GUI set to null */

		Client(String server,int port, String username, ClientGUI cg){
			this.server=server;
			this.port=port;
			this.username=username;
			cGUI = cg;
			awayFlag  = false;
			}
	
		public boolean start() {

		try{	
			socket = new Socket(server,port);
			//System.out.println("In Start Method");
		}

		catch(Exception e){
			display("Error connection to server" );
			return false;
		}
			//System.out.println("Above connection accepted");
			String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
			display(msg);
			//System.out.println("below connection accepted" + msg);

		/* creating data stream */

		try
		{   
			sOut = new ObjectOutputStream(socket.getOutputStream());
			sIn = new ObjectInputStream(socket.getInputStream());
		}
		catch (IOException e) {
			display("Exception in creating new IO stream ");
			return false;
		}
		
		/* Thread creation to listen from server */
		/* username is sent to server as string all other objects are sent as ChatObject object */	
			new ListenFromServer().start(); 
	
		try
		{
			sOut.writeObject(username);
		}
		catch(IOException e)
		{
			display("Exception doing login ");
			disconnect();
			return false;
		}
			/* success and informed the person */
			return true;
		}
		
		private void display(String msg){
	
		 cGUI.append(msg + "\n",ChatObject.MESSAGE);
		/* append to message to clientGUI */
		}
		
		void sendMessage(ChatObject msg){
		String temp=null;
		String temp2 =null;
		msg.setIntended(null);
		/* sending a private message to a user */
		/* private message is preceded by #[Name of user] [Message] */
		if(msg.getType()==ChatObject.PRIVMESS)
		{	
			temp=msg.getMessage();
			temp2=msg.getMessage();
			int loc=temp.indexOf('#')+1;
			int startloc = temp.indexOf(' ',loc);
			if(startloc==-1){
				startloc=temp.length();
			}
			if(startloc!=-1 && loc!=-1){
			String intended = temp.substring(loc, startloc);
			
			msg.setIntended(intended);
			if(startloc!=temp.length())
				temp=temp.substring(startloc+1);
			else temp =" ";
			if(loc-2>0){
			temp2 =temp2.substring(0, loc-2);
			temp=temp2.concat(" "+temp);
			}
			temp=temp.trim();
			msg.setMessage(temp);
			}
		}
		else if(msg.getType()==ChatObject.DIR)
		{
			temp=msg.getMessage();
			temp2=msg.getMessage();
			int loc=temp.indexOf('@')+1;
			int startloc = temp.indexOf(' ',loc);
			if(startloc==-1){
				startloc=temp.length();
			}
			if(startloc!=-1 && loc!=-1){
			String intended = temp.substring(loc, startloc);
			
			msg.setIntended(intended);
			if(startloc!=temp.length())
				temp=temp.substring(startloc+1);
			else temp = " ";
			if(loc-2>0){
			temp2 =temp2.substring(0, loc-2);
			temp=temp2.concat(" "+temp);
			}
			temp=temp.trim();
			msg.setMessage(temp);
			}
		}
		else {
			temp = msg.getMessage();
			temp = temp.trim();
			msg.setMessage(temp);
		}
		if(!temp.isEmpty() || msg.getType()==ChatObject.LOGOUT || msg.getType()==ChatObject.WHO){
		try {
			if(msg.getType()==ChatObject.PRIVMESS || msg.getType()==ChatObject.DIR){
				if(msg.getIntended()!=null)
					sOut.writeObject(msg);
			}
			else	
			{
				sOut.writeObject(msg);
			}
		}
		catch(IOException e){
			display("Exception writing to server");
			}
		  }
		}
		/* If something goes wrong then close all the connection */

		private void disconnect() {
			
		try {
			if(sIn !=null) sIn.close();
		    }
		catch(Exception e) {} 
		/*nothing to be done when sIn has null */
		try {
			if(sOut !=null) sOut.close();
		   }
		catch(Exception e) {} 
		/* nothing to be done */
		try {
			if(socket!= null) socket.close();
		    }
		/* nothing to be done */
		catch(Exception e) {} 
		
			if(cGUI!= null)
			cGUI.connectionFailed();
		}
		
		/* a class that waits for message from the server and append them to JTextPane */
		class ListenFromServer extends Thread {
		 @Override
		public void run() {
			while(true) {
				try {

					String msg = (String)sIn.readObject();
					
					if(awayFlag==true)
					{
						while(awayFlag==true)
						{
							try {
								sleep(2000);
							} catch (InterruptedException e) {
							}
						}
					}
					
					/* checking for a private message */
					if(msg.indexOf('#')!= -1){
						int index=msg.indexOf('#');
						msg=msg.substring(0, index);
						cGUI.append(msg,ChatObject.PRIVMESS);
					}
					/* checking for a broadcast message */
					else if(msg.indexOf('~')!= -1){
						msg=msg.replace('~', ' ');
						cGUI.append(msg,ChatObject.PRIO);
					}
					else if(msg.indexOf('@')!=-1){
						cGUI.append(msg,ChatObject.DIR);
					}
					else if(msg.indexOf('*')!=-1){
						cGUI.append(msg, ChatObject.INFO);
					}
					else	
				/* checking for a normal chatroom message */	
				
				cGUI.append(msg,ChatObject.MESSAGE);
				}
		
		
		catch(IOException e) {
		display("Server has closed the connection ");
		if(cGUI !=null)
			cGUI.connectionFailed();
		break;
		}
		/* can't happen with a string object but need the catch any how */
		catch(ClassNotFoundException e2){
		}
	   }
	  }
	}
}