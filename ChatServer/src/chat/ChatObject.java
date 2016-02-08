package chat;

import java.io.Serializable;

public class ChatObject implements Serializable{	

	/**
	 * The ChatObject will be exchanged between the server and the client
	 */
	private static final long serialVersionUID = 1L;
	/* Static values for each type of message */
	static final int WHO = 0, MESSAGE =1, LOGOUT =2, PRIVMESS =3, PRIO =4, DIR =5, INFO = 6, BLOCK=7, UNBLOCK = 8, FETCH=9;   
	private int type;
	private String message;
	private String intended;
	
	/*
	 * Constructor method
	 * Input:
	 * 		-type of the message
	 * 		-actual message text
	 */
	ChatObject(int type,String message)
	{
	 this.type=type;
	 this.message=message;
	}
 
	/*
	 * Getter method for fetching value of intended client
	 */
	public String getIntended() {
		return intended;
	}

	/*
	 * Setter method for setting value of intended client
	 */
	public void setIntended(String intended) {
		this.intended = intended;
	}

	/*
	 * Getter method for fetching the type of message
	 */
	int getType() {
		return type;
	}
	
	/*
	 * Setter method for setting value of message
	 */
	
	public void setMessage(String message) {
		this.message = message;
	}

	/*
	 * Getter method for fetching the actual message content
	 */
	String getMessage() {
		return message;
	}


}