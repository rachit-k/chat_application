import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;


import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


import javax.crypto.Cipher;

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
    String publicKey;
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
    public static int type = 0;
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
                Server.type = Integer.parseInt(args[0]);
                For_other = ss2.accept();
                BufferedReader inFromClientForMe = new BufferedReader(new InputStreamReader(For_me.getInputStream()));
                BufferedReader inFromClientForOther = new BufferedReader(new InputStreamReader(For_other.getInputStream()));
                DataOutputStream MsgFromMe = new DataOutputStream(For_me.getOutputStream());
                DataOutputStream MsgFromOther = new DataOutputStream(For_other.getOutputStream());
                MsgFromMe.writeBytes(args[0]+ "\n");
                String userName = inFromClientForMe.readLine();
                boolean isValidName = true;
                for(int i=0;i<Clients.size();i++){
                    if(Clients.get(i).userName.equals(userName)){
                        isValidName = false;
                    }
                }
                for(int i=0;i<userName.length();i++){
                    if(!(userName.charAt(i)>='A' && userName.charAt(i)<='Z') && !(userName.charAt(i)>='a' && userName.charAt(i) <= 'z')&& !(userName.charAt(i)>='0' && userName.charAt(i) <= '9')){
                        isValidName = false;
                    }
                }
                System.out.println("Hllo1");
                if(!isValidName){
                    MsgFromMe.writeBytes("INVALID" + "\n");
                }
                else{
                    MsgFromMe.writeBytes("sdfds"+"\n");
                }
                while(!isValidName){
                    System.out.println("Hll3o");

                    userName = inFromClientForMe.readLine();
                    System.out.println("Hll4o");

                    isValidName = true;
                    for(int i=0;i<Clients.size();i++){
                        if(Clients.get(i).userName.equals(userName)){
                            isValidName = false;
                        }
                    }
                    for(int i=0;i<userName.length();i++){
                        if(!(userName.charAt(i)>='A' && userName.charAt(i)<='Z') && !(userName.charAt(i)>='a' && userName.charAt(i) <= 'z')&& !(userName.charAt(i)>='0' && userName.charAt(i) <= '9')){
                            isValidName = false;
                        }
                    }
                    if(!isValidName){
                        MsgFromMe.writeBytes("INVALID" + "\n");
                    }
                    else{
                        MsgFromMe.writeBytes("sdfds"+"\n");
                    }
                }
                System.out.println("Hll5o");

                String publicKey = inFromClientForMe.readLine();
                System.out.println("sdsdvds");
                byte[] sx = Base64.getDecoder().decode(publicKey);
                ServerData newClient = new ServerData(For_me,For_other,inFromClientForMe,inFromClientForOther,MsgFromMe,MsgFromOther,false,false,userName);
                newClient.userName = userName;
                newClient.userName = userName;
                MsgFromMe.writeBytes("REGISTERED TOSEND " + userName + "\n");
                MsgFromMe.writeBytes("REGISTERED TORECV " + userName + "\n");
                String userName1 = inFromClientForMe.readLine();
                newClient.publicKey = publicKey;
                userName1 = inFromClientForMe.readLine();
                newClient.canSend = true;
                newClient.canReceive = true;
                Clients.add(newClient);
                Thread t = new OtherClientHandler(inFromClientForOther,userName,MsgFromOther);
                t.start();
                Thread t1 = new FetchKey(inFromClientForMe,MsgFromOther,MsgFromMe,newClient.userName);
                t1.start();
            }
            catch (Exception e){
                For_other.close();
                For_me.close();
                //System.out.println("Rahul");
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
        String signature = "";
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
                int k = Server.type;
                if(k==2){
                    if(received.length()>10){
                        if(received.substring(0,10).equals("SIGNATURE ") && se && len && !mess){
                            signature = received;
                            continue;
                        }
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
                if(k!=2){
                    if( se && len && mess){
                        boolean xd = false;
                        for(int i=0;i<Server.Clients.size();i++){
                            if(Server.Clients.get(i).userName.equals(recipent)){
                                xd = true;
                                //System.out.println(messgae);
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
                }
                else{
                    if( se && len && mess){
                        boolean xd = false;
                        for(int i=0;i<Server.Clients.size();i++){
                            if(Server.Clients.get(i).userName.equals(recipent)){
                                xd = true;
                                //System.out.println(messgae);
                                Server.Clients.get(i).MsgFromOther.writeBytes("FORWARD " + ME + "\n" + "Content-length: " + leng + "\n" + signature +"\n" + messgae + "\n");
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


class FetchKey extends Thread{
    final DataOutputStream MsgFromMe;
    final String user;
    final DataOutputStream MsgFromOther;
    final BufferedReader inFromClientForMe;
    public FetchKey(BufferedReader inFromClientForMe, DataOutputStream MsgFromOther, DataOutputStream MsgFromMe, String user){
        this.MsgFromOther = MsgFromOther;
        this.MsgFromMe = MsgFromMe;
        this.inFromClientForMe = inFromClientForMe;
        this.user = user;
    }
    public void run(){
        String received;
        String[] splited;
        while(true){
            try{
                received = inFromClientForMe.readLine();
                //System.out.println("Rahuldsds");
                //System.out.println(received);
                if(received.equals("UNREGISTER")){
                    //System.out.println("On Server");
                    for(int i=0;i<Server.Clients.size();i++){
                        if(Server.Clients.get(i).userName.equals(user)){
                            Server.Clients.get(i).inFromClientForMe.close();
                            Server.Clients.get(i).inFromClientForOther.close();
                            Server.Clients.get(i).MsgFromMe.close();
                            Server.Clients.get(i).MsgFromOther.close();
                            Server.Clients.get(i).For_me.close();
                            Server.Clients.get(i).For_other.close();
                            Server.Clients.remove(i);
                            MsgFromMe.writeBytes("UNREGISTERED");
                            break;
                        }
                    }
                    for(int j=0;j<Server.Clients.size();j++){
                        System.out.println(Server.Clients.get(j).userName);
                    }
                }
                
                if(received.length()>9){
                    if(received.substring(0,9).equals("FETCHKEY ")){
                        //System.out.println("Rahul");
                        splited = received.split("\\s+",2);
                        boolean sd = true;
                        for(int i=0;i<Server.Clients.size();i++){
                            if(Server.Clients.get(i).userName.equals(splited[1])){
                                MsgFromMe.writeBytes("FETCHKEY " + splited[1] + " " + Server.Clients.get(i).publicKey + "\n");
                                sd = false;
                                //System.out.println("Rahuldsds");
                                //System.out.println("FETCHKEY " + splited[1] + " " + Server.Clients.get(i).publicKey + "\n");


                            }
                        }
                        if(sd){
                            MsgFromOther.writeBytes("ERROR 102 Unable to send" + "\n" + "\n");
                        }
                    }
                }
                if(received.length()>10){
                    if(received.substring(0,10).equals("FETCHKEYA ")){
                        //System.out.println("Rahul");
                        splited = received.split("\\s+",2);
                        boolean sd = true;
                        for(int i=0;i<Server.Clients.size();i++){
                            if(Server.Clients.get(i).userName.equals(splited[1])){
                                MsgFromMe.writeBytes("FETCHKEYA " + splited[1] + " " + Server.Clients.get(i).publicKey + "\n");
                                //System.out.println("Make_m_true");
                                sd = false;
                                //System.out.println("Rahuldsds");
                                //System.out.println("FETCHKEY " + splited[1] + " " + Server.Clients.get(i).publicKey + "\n");
                                
                                
                            }
                        }
                    }
                }
            }
            catch(IOException e){
                for(int i=0;i<Server.Clients.size();i++){
                    if(Server.Clients.get(i).userName.equals(user)){
                        Server.Clients.remove(i);
                        break;
                    }
                }
            }
        }
    }
}
