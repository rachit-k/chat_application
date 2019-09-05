import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

class ServerData
{
    boolean canSend;
    boolean canReceive;
    Socket For_me;
    Socket For_other;
    DataOutputStream MsgFromMe;
    DataOutputStream MsgFromOther;
    BufferedReader inFromClientForMe;
    BufferedReader inFromClientForOther;
    String userName;
    public ServerData(Socket For_me,Socket For_other,BufferedReader inFromClientForMe, BufferedReader inFromClientForOther, DataOutputStream MsgFromMe, DataOutputStream MsgFromOther, boolean canSend, boolean canReceive, String userName){
        this.canReceive = canReceive;
        this.canSend = canSend;
        this.inFromClientForOther = inFromClientForOther;
        this.inFromClientForMe = inFromClientForMe;
        this.For_other = For_other;
        this.For_me = For_me;
        this.MsgFromOther = MsgFromOther;
        this.MsgFromMe = MsgFromMe;
    }
}
public class Server
{
    public static ArrayList<ServerData> Clients = new ArrayList<ServerData>();
    public static void main(String[] args) throws IOException
    {
        ServerSocket ss1 = new ServerSocket(5056);
        ServerSocket ss2 = new ServerSocket(5057);
        while (true)
        {
            Socket For_me = null;
            Socket For_other = null;
            
            try
            {
                For_me = ss1.accept();
                For_other = ss2.accept();
                BufferedReader inFromClientForMe = new BufferedReader(new InputStreamReader(For_me.getInputStream()));
                BufferedReader inFromClientForOther = new BufferedReader(new InputStreamReader(For_other.getInputStream()));
                DataOutputStream MsgFromMe = new DataOutputStream(For_me.getOutputStream());
                DataOutputStream MsgFromOther = new DataOutputStream(For_other.getOutputStream());
                String userName = inFromClientForMe.readLine();
                ServerData newClient = new ServerData(For_me,For_other,inFromClientForMe,inFromClientForOther,MsgFromMe,MsgFromOther,false,false,userName);
                newClient.userName = userName;
                newClient.userName = userName;
                MsgFromMe.writeBytes("REGISTERD TOSEND " + userName + "\n");
                MsgFromMe.writeBytes("REGISTERD TORECV " + userName + "\n");
                String userName1 = inFromClientForMe.readLine();
                userName1 = inFromClientForMe.readLine();
                newClient.canSend = true;
                newClient.canReceive = true;
                Clients.add(newClient);
                Thread t = new OtherClientHandler(inFromClientForOther,userName,MsgFromOther);
                t.start();
            }
            catch (Exception e){
                For_other.close();
                For_me.close();
                e.printStackTrace();
            }
        }
    }
}

class OtherClientHandler extends Thread
{
    final BufferedReader inFromClientForOther;
    final String ME;
    final DataOutputStream MsgFromOther ;
    public OtherClientHandler(BufferedReader inFromClientForOther, String ME,DataOutputStream MsgFromOther)
    {
        this.inFromClientForOther = inFromClientForOther;
        this.ME = ME;
        this.MsgFromOther = MsgFromOther;
    }
    public void run()
    {
        String received;
        String toreturn= "" ;
        boolean se = false;
        boolean len = false;
        boolean mess = false;
        int leng =0;
        int t1 =0;
        String recipent = "";
        String messgae = "";
        while (true)
        {
            try {
                received = inFromClientForOther.readLine();
                if(received.length()>5){
                    if(received.substring(0,5).equals("SEND ") && !se){
                        recipent = received.substring(5,received.length());
                        se = true;
                    }
                }
                boolean fg = false;
                if(received.length()>16){
                    if(received.substring(0,16).equals("Content-length: ") && se){
                        leng = Integer.parseInt(received.substring(16,received.length()));
                        len  = true;
                        fg = true;
                    }
                }
                if(se && len && !mess && !fg){
                    int t=0;
                    while(t1<leng && t<received.length()){
                        messgae = messgae + received.charAt(t);
                        t1++;
                        t++;
                    }
                    if(t1 == leng){
                        mess = true;
                    }
                }
                if( se && len && mess){
                    boolean xd = false;
                    for(int i=0;i<Server.Clients.size();i++){
                        if(Server.Clients.get(i).userName.equals(recipent)){
                            xd = true;
                            Server.Clients.get(i).MsgFromOther.writeBytes("FORWARD " + ME + "\n" + "Content-length: " + leng + "\n" + "\n" + messgae + "\n");
                            MsgFromOther.writeBytes("SENT "+recipent+"\n");
                        }
                    }
                    if(!xd){
                        MsgFromOther.writeBytes("ERROR 102 Unable to send" + "\n" + "\n");
                    }
                    se = false ;
                    len = false;
                    mess = false;
                    t1 = 0;
                    toreturn = "";
                    recipent = "";
                    leng = 0;
                    messgae = "";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       /* try
        {
            this.inFromClientForOther.close();
            this.inFromClientForMe.close();
            this.MsgFromOther.close();
            this.MsgFromMe.close();
        }catch(IOException e){
            e.printStackTrace();
        }*/
    }
}
