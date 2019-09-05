import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Client
{
    public static void main(String[] args) throws IOException
    {
        try
        {
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            Socket serversocket = new Socket(args[0], 5056);
            Socket ClientSocket = new Socket(args[0],5057);
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(serversocket.getInputStream()));
            DataOutputStream ToServer = new DataOutputStream(serversocket.getOutputStream());
            DataOutputStream ToClient = new DataOutputStream(ClientSocket.getOutputStream());
            ToServer.writeBytes(args[1]+"\n");
            ToServer.writeBytes("REGISTER TOSEND " + args[1] + "\n");
            ToServer.writeBytes("REGISTER TORECV " + args[1] + "\n");
            Thread t = new messageFromClient(inFromClient,ToServer);
            t.start();
            Thread t2 = new messageFromServer(inFromServer);
            t2.start();
            Thread t1 = new messageFromMe(inFromUser,ToServer,ToClient);
            t1.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}


class messageFromClient extends Thread
{
    final BufferedReader inFromClient;
    final DataOutputStream ToServer;
    public messageFromClient(BufferedReader inFromClient,DataOutputStream ToServer)
    {
        this.inFromClient = inFromClient;
        this.ToServer = ToServer;
    }
    public void run()
    {
        String received;
        boolean f= false;
        boolean l = false;
        boolean m = false;
        String sender = "";
        String messgae = "";
        int m_l = 0;
        int t1 =0;
        while (true)
        {
            try {
                received = inFromClient.readLine();
                if(received.length()>5){
                    if(received.substring(0,5).equals("SENT ")){
                        System.out.println(received);
                        continue;
                    }
                }
                if(received.length()>5){
                    if(received.substring(0,5).equals("ERROR")){
                        System.out.println(received);
                        continue;
                    }
                }
                if(received.length()>8){
                    if(!f && received.substring(0,8).equals("FORWARD ")){
                        int t = 8;
                        while(t<received.length()){
                            sender = sender + received.charAt(t);
                            t++;
                        }
                        f = true;
                        l = false;
                        m =false;
                    }
                }
                boolean jk = false;
                if(received.length()>16){
                    if(f && !l && received.substring(0,16).equals("Content-length: ")){
                        m_l = Integer.parseInt(received.substring(16,received.length()));
                        l = true;
                        jk = true;
                    }
                }
                
                if(f && l && !m && !jk){
                    int t=0;
                    while(t1<m_l && t<received.length()){
                        messgae = messgae + received.charAt(t);
                        t1++;
                        t++;
                    }
                    if(t1 == m_l){
                        m= true;
                    }
                }
                if(f && l && m){
                    System.out.println(sender + ": " +messgae);
                    f = false;
                    l = false;
                    m = false;
                    t1 =0;
                    sender = "";
                    messgae = "";
                    m_l =0;
                    ToServer.writeBytes("RECEIVED "+ sender + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       /* try
        {
            this.inFromClient.close();
            this.ToServer.close();
            //this.dis.close();
            //this.dos.close();
        }catch(IOException e){
            e.printStackTrace();
        }*/
    }
}

class messageFromServer extends Thread
{
    final BufferedReader inFromServer;
    public messageFromServer(BufferedReader inFromServer)
    {
        this.inFromServer = inFromServer;
    }
    public void run()
    {
        String received;
        while (true)
        {
            try {
                received = inFromServer.readLine();
                System.out.println(received);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*try
         {
         //this.dis.close();
         //this.dos.close();
         }catch(IOException e){
         e.printStackTrace();
         }*/
    }
}


class messageFromMe extends Thread
{
    final DataOutputStream ToServer;
    final DataOutputStream ToClient;
    final BufferedReader inFromUser;
    public messageFromMe(BufferedReader inFromUser, DataOutputStream ToServer, DataOutputStream ToClient)
    {
        this.inFromUser = inFromUser;
        this.ToClient = ToClient;
        this.ToServer = ToServer;
    }
    public void run()
    {
        String received;
        while (true)
        {
            try {
                received = inFromUser.readLine();
                if(received.length()>1){
                    if(received.substring(0,1).equals("@")){
                        char t = received.charAt(1);
                        String opponent = "";
                        int i=1;
                        while(!(received.charAt(i)==' ') && i<received.length()){
                            opponent = opponent + received.charAt(i);
                            i++;
                        }
                        String messgae  = "";

                        i++;

                        while(i<received.length()){
                            messgae = messgae+ received.charAt(i);
                            i++;
                        }
                        if(messgae.length() == 0){
                            System.out.println("No Message Content");
                        }
                        if(opponent.length()==0){
                            System.out.println("No User Name Specified");
                        }
                        ToClient.writeBytes("SEND "+ opponent + "\n" + "Content-length: " + messgae.length() + "\n" + "\n" + messgae + "\n");

                    }
                    else{
                        System.out.println("Incorrect Format");
                    }
                }
                else{
                    System.out.println("Incorrect Format");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*try
         {
         //this.dis.close();
         //this.dos.close();
         }catch(IOException e){
         e.printStackTrace();
         }*/
    }
}
