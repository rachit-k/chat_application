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
		clients=new ClientThread();
		serverSocket= new ServerSocket();
	}

	public void initialise()
	{
		flag=true;
		
		while(flag)
		{
			Socket connectionSocket = serverSocket.accept();
			ClientThread newClient = new ClientThread(connectionSocket);
			clients.add(newClient);
			newClient.start();			
		}

	}

	public void chatting(String sentence)
	{
		String[] str=sentence.split(" ",3); 
		String uname=str[1].substring(1);
		String message=str[0]+str[2];
		boolean flag=flase;
		for(int i=0;i<clients.size(),i++)
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

	void removeClient(username)
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

	class ClientThread implements Runnable
	{
		Socket connectionSocket;
		DataInputStream inFromClient;
		DataOutputStream outToClient;
		String username;
		String sentence;

		ClientThread(Socket connectionSocket)
		{
			this.connectionSocket=connectionSocket;
			inFromClient=new DataInputStream();
			outToClient=new DataOutputStream();
			username=inFromClient.readline();
			//chatting(username);
		}

		void msgToClient(String sentence)
		{
			outToClient.writeBytes("@"+sentence);
		}

		public void run()
		{
			while(true)
			{
				sentence=inFromClient.readLine();
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
			inFromClient.close();
			outToClient.close();
			connectionSocket.close();

		}
	}
}

