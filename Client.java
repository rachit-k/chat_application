import java.util.*;
import java.io.*;
import java.net.*; 

class Client
{
	private Socket socket;
	private DataInputStream inFromServer;
	private DataOutputStream outToServer;

	private String server, username;
	//private int port;		

	public static Boolean isAlphaNum(String s) 
	{
		for (int i = 0; i < s.length(); i++) 
		{
			char c = s.charAt(i);
			if (!(c >= 'A' && c <= 'Z') && !(c >= 'a' && c <= 'z') &&!(c >= '0' && c <= '9')) 
			{
				return false;
			}
		}
		return true;
	}

	Client(String username, String server) //throws Wrongusername
	{
		// if(!isAlphaNum(s))
		// {
		// 	throw new Wrongusername();
		// }
		this.server = server;
		this.username = username;
	}	

	public void initialise()
	{
		try
		{
			socket = new Socket(server, 6789);
			inFromServer  = new DataInputStream(socket.getInputStream());
			outToServer = new DataOutputStream(socket.getOutputStream());

			ServerThread s =new ServerThread();
			s.start();

			outToServer.writeBytes(username + '\n');
		}
		catch(Exception e)
		{			
			System.out.println("Exception in ClientStart");			
		}

	}

	public void msgToServer(String sentence)
	{
		try
		{
			outToServer.writeBytes(sentence);
		}
		catch(IOException e)
		{
			System.out.println("Error in sending message to server");
		}
	}

	public void end()
	{
		try
		{
			inFromServer.close();
			outToServer.close();
			socket.close();
		}
		catch(Exception e)
		{			
			System.out.println("Exception in ClientEnd");			
		}
	}


	public static void main(String args[])
	{
		Scanner scanner = new Scanner(System.in);
		String uname;
		String serv="localhost";
		System.out.println("Enter the username and server: ");
		uname= args[0];
		while(!((isAlphaNum(uname)) && (args.length==2)))
		{
			System.out.println("Enter the username and server: ");
			uname= args[0];
			serv=args[1];
		}

		Client client=new Client(uname,serv);
		client.initialise();

		while(true)
		{
			System.out.print(": ");
			String sentence= scanner.nextLine();
			if(sentence.equalsIgnoreCase("unregister"))
			{
				client.msgToServer(sentence);
				break;
			}
			else if(!(sentence.charAt(0)=='@' && (sentence.length() - sentence.replaceAll(" ", "").length())==1))
			{
				System.out.println("Invalid request ");
				continue;
			}
			client.msgToServer(sentence);

		}
		client.end();

	}


	class ServerThread extends Thread
	{
		public void run()
		{
			while(true)
			{
				try
				{
					String sentence=inFromServer.readLine();
					System.out.println(sentence);
				}
				catch(IOException e)
				{
					System.out.println("Connection closed from server side");
					break;
				}
			}
		}
	}

}
