import java.io.*;

import java.text.*;
import java.util.*;

import java.net.*;
import java.util.Scanner;
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
import java.security.MessageDigest;

import javax.crypto.Cipher;

class recipent_data{
    String publicKey;
    String user;
    public recipent_data(){
        publicKey = "";
        user = "";
    }
}

public class Client
{
    public static int type = 0;
    public static recipent_data recipent = new recipent_data();
    public static boolean data_came = true;
    public static recipent_data sender = new recipent_data();
    public static boolean data_came_from_sender = false;
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
            KeyPair generateKeyPair = CryptographyExample.generateKeyPair();
            byte[] publicKey = generateKeyPair.getPublic().getEncoded();
            byte[] privateKey = generateKeyPair.getPrivate().getEncoded();
            String hdfh = inFromServer.readLine();
            Client.type = Integer.parseInt(hdfh);
            //System.out.println(hdfh);
            ToServer.writeBytes(args[1]+"\n");
            String pk = Base64.getEncoder().encodeToString(publicKey);
            //System.out.println(pk);
            //System.out.println(publicKey);
            ToServer.writeBytes(pk+"\n");
            ToServer.writeBytes("REGISTER TOSEND " + args[1] + "\n");
            ToServer.writeBytes("REGISTER TORECV " + args[1] + "\n");
            Thread t = new messageFromClient(inFromClient,ToServer,privateKey,publicKey);
            t.start();
            Thread t2 = new messageFromServer(inFromServer);
            t2.start();
            Thread t1 = new messageFromMe(inFromUser,ToServer,ToClient, privateKey);
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
    final byte[] privateKey;
    final byte[] publicKey;
    public messageFromClient(BufferedReader inFromClient,DataOutputStream ToServer, byte[] privateKey, byte[] publicKey)
    {
        this.inFromClient = inFromClient;
        this.ToServer = ToServer;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }
    public void run()
    {
        String received;
        boolean f= false;
        boolean l = false;
        boolean m = false;
        boolean s = false;
        String sender = "";
        String messgae = "";
        String signature = "";
        int m_l = 0;
        int t1 =0;
        while (true)
        {
            try {
                received = inFromClient.readLine();
                //System.out.println("p");
                //System.out.println(received);
                if(received.length()>5){
                    if(received.substring(0,5).equals("SENT ")){
                        System.out.println(received);
                        continue;
                    }
                }
                if(received.length()>5){
                    if(received.substring(0,5).equals("ERROR")){
                        Client.data_came = true;
                        Client.recipent.user = "";
                        Client.recipent.publicKey = "";
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
                        ToServer.writeBytes("FETCHKEYA "+ sender+ "\n");
                        f = true;
                        l = false;
                        m =false;
                    }
                }
                int k=Client.type;
                if(k==2){
                    if(received.length()>10){
                        if(received.substring(0,10).equals("SIGNATURE ") && f && l && !m){
                            signature = received.split("\\s+",2)[1];
                            //System.out.println("dfsd");
                            //System.out.println(signature);
                            continue;
                        }
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
                        //System.out.println("Make_m_true");
                        //System.out.println(messgae);
                    }
                }
                if(f && l && m){
                    //System.out.println("Make_m_truedf");
                    if(k==0){
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
                    if(k==1){
                        byte[] xew = Base64.getDecoder().decode(messgae);
                        // System.out.println(messgae);
                        byte[] decryptedData = CryptographyExample.decrypt(privateKey, xew);
                        messgae = new String(decryptedData);
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
                    if(k==2){
                        //System.out.println("Make_m_truesgdgdg");
                        boolean happen = false;
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        byte[] fg = Base64.getDecoder().decode(messgae);
                        byte[] kf = md.digest(fg);
                        byte[] si = Base64.getDecoder().decode(signature);
                        boolean dfg = true;
                        while(dfg){
                            if(Client.data_came_from_sender){
                                dfg = false;
                            }
                        }
                        //System.out.println(Client.sender.publicKey);
                        //System.out.println("sgsdgsdg");
                        byte[] jklh = Base64.getDecoder().decode(Client.sender.publicKey);
                        //System.out.println(jklh);
                        byte[] side = CryptographyExample.decrypt1(jklh,si);
                        if((new String(kf)).equals(new String(side))){
                            happen = true;
                        }

                        if(happen){
                            byte[] xew = Base64.getDecoder().decode(messgae);
                            // System.out.println(messgae);
                            byte[] decryptedData = CryptographyExample.decrypt(privateKey, xew);
                            messgae = new String(decryptedData);
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
                        else{
                            System.out.println(sender + ": corrupted Message!");
                        }
                    }
                    Client.data_came_from_sender = false;
                }
            } catch (Exception e) {
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
                String[] splited;
                //System.out.println("Rahuldsds");
                //System.out.println(received);
                if(received.substring(0,9).equals("FETCHKEY ")){
                    //System.out.println("Rahuldsds");
                    splited = received.split("\\s+",3);
                    Client.recipent.user = splited[1];
                    Client.recipent.publicKey = splited[2];
                    Client.data_came = true;
                }
                else if(received.substring(0,10).equals("FETCHKEYA ")){
                    //System.out.println("Rahuldsds");
                    splited = received.split("\\s+",3);
                    Client.sender.user = splited[1];
                    Client.sender.publicKey = splited[2];
                    //System.out.println("Fetchkey");
                    Client.data_came_from_sender = true;
                }
                else{
                    System.out.println(received);
                }
                
            } catch (Exception e) {
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
    final byte[] privateKey;
    public messageFromMe(BufferedReader inFromUser, DataOutputStream ToServer, DataOutputStream ToClient, byte[] privateKey)
    {
        this.inFromUser = inFromUser;
        this.ToClient = ToClient;
        this.ToServer = ToServer;
        this.privateKey = privateKey;
    }
    public void run()
    {
        String received;
        while (true)
        {
            try {
                received = inFromUser.readLine();
                Client.data_came = false;
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
                        ToServer.writeBytes("FETCHKEY " + opponent + "\n");
                        boolean tx = true;
                        String pu = "";
                        //System.out.println("Rahul");
                        while(tx){
                            //System.out.println("Rahulwh");

                            if(Client.data_came){
                                tx = false;
                            }
                            if(Client.recipent.user.equals(opponent)){
                                pu = Client.recipent.publicKey;
                            }
                        }

                        if(!pu.equals("")){
                            int k=Client.type;
                            if(k==0){
                                ToClient.writeBytes("SEND "+ opponent + "\n" + "Content-length: " + messgae.length() + "\n" + "\n" + messgae + "\n");
                                Client.data_came = false;
                            }
                            if(k==1){
                                byte[] sx = Base64.getDecoder().decode(pu);
                                byte[] encryptedData = CryptographyExample.encrypt(sx,
                                                                                   messgae.getBytes());
                                messgae = Base64.getEncoder().encodeToString(encryptedData);
                                ToClient.writeBytes("SEND "+ opponent + "\n" + "Content-length: " + messgae.length() + "\n" + "\n" + messgae + "\n");
                                Client.data_came = false;
                            }
                            
                            if(k==2){
                                byte[] sx = Base64.getDecoder().decode(pu);
                                byte[] encryptedData = CryptographyExample.encrypt(sx,
                                                                                   messgae.getBytes());
                                messgae = Base64.getEncoder().encodeToString(encryptedData);
                                MessageDigest md = MessageDigest.getInstance("SHA-256");
                                byte[] shaBytes = md.digest(encryptedData);
                                encryptedData = CryptographyExample.encrypt1(privateKey,shaBytes);
                                String messgae1 = Base64.getEncoder().encodeToString(encryptedData);
                                //System.out.println(messgae1);
                                //System.out.println(messgae);
                                //System.out.println("Send");
                                //System.out.println("SEND "+ opponent + "\n" + "Content-length: " + messgae.length() + "\n" + "\n" +"SIGNATURE " + messgae1 + "\n" + messgae + "\n");
                                ToClient.writeBytes("SEND "+ opponent + "\n" + "Content-length: " + messgae.length() + "\n" + "\n" +"SIGNATURE " + messgae1 + "\n" + messgae + "\n");
                                Client.data_came = false;
                            }
                        }
                    }
                    else{
                        System.out.println("Incorrect Format");
                    }
                }
                else{
                    System.out.println("Incorrect Format");
                }
            } catch (Exception e) {
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
