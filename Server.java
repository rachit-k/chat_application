import java.util.ArrayList;
import java.io.*;
import java.net.*; 

class Server
{
	private ArrayList<ClientThread> clients;
	ServerSocket serverSocket;

	boolean flag;

	Server()
	{
		clients=new ArrayList<ClientThread>();
		try
		{
			serverSocket= new ServerSocket(6789);
		}
		catch(IOException e)
		{
			System.out.println("Exception in Server");
		}
	}

	public void initialise()
	{
		flag=true;
		
		while(flag)
		{
			try
			{
				Socket connectionSocket = serverSocket.accept();			
				ClientThread newClient = new ClientThread(connectionSocket);
				clients.add(newClient);
				newClient.start();	
			}
			catch(IOException e)
			{
				System.out.println("Exception in Server");
			}		
		}

	}

	public void chatting(String sentence)
	{
		String[] str=sentence.split(" ",3); 
		String uname=str[1].substring(1);
		String message=str[0]+str[2];
		boolean flag=false;
		for(int i=0;i<clients.size();i++)
		{
			ClientThread temp=clients.get(i);
			if(temp.username.equals(uname))
			{
				temp.msgToClient(message);
				flag=true;
				break;
			}
		}
		if(!flag)
		{	
			System.out.println("Client not found");
		} 

	}

	public static void main(String args[])
	{
		Server serv=new Server();
		// while(true)
		// {
		// 	Socket socket= serv.serverSocket.accept();

		// }
		serv.initialise();
	}

	void removeClient(String username)
	{
		for(int i=0;i<clients.size();i++)
		{
			ClientThread temp=clients.get(i);
			if(temp.username.equals(username))
			{
				clients.remove(i);
				break;
			}
		}

	}

	class ClientThread extends Thread
	{
		Socket connectionSocket;
		DataInputStream inFromClient;
		DataOutputStream outToClient;
		String username;
		String sentence;

		ClientThread(Socket connectionSocket)
		{
			this.connectionSocket=connectionSocket;
			try
			{
				inFromClient=new DataInputStream(connectionSocket.getInputStream());
				outToClient=new DataOutputStream(connectionSocket.getOutputStream());
				username=inFromClient.readLine();
			}
			catch(IOException e)
			{
				System.out.println("Exception in ClientThread");
			}
			//chatting(username);
		}

		void msgToClient(String sentence)
		{
			try
			{
				outToClient.writeBytes("@"+sentence);
			}
			catch(IOException e)
			{
				System.out.println("Exception in ClientThread");
			}
		}

		public void run()
		{
			while(true)
			{
				try
				{
					sentence=inFromClient.readLine();
				}
				catch(IOException e)
				{
					System.out.println("Exception in ClientThread");
				}
				if(sentence.equalsIgnoreCase("unregister"))
				{
					System.out.println("unregistered");
					flag=false;
					break;
				}
				else
				{
					chatting(username+ " "+ sentence);
				}
			}
			removeClient(username);
			try
			{
				inFromClient.close();
				outToClient.close();
				connectionSocket.close();
			}
			catch(IOException e)
			{
				System.out.println("Exception in ClientThread");
			}

		}
	}
}

