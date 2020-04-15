//Group 16 Vanilla Client DS-SIM

import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.*; 

public class Client {
    private Socket socket            = null;
    private PrintWriter out = null;
    private BufferedReader input = null;
    
    //Global Variables
    private String input1 = "";
    private String finalServerName = "";
    private int finalServerNumber = 0;
    private int jobSubTime, jobnumber, jobRunTime, jobCPU, jobMemory, jobDisk;
    private String serverType;
    private int serverID, serverState, serverTime, serverCPU, serverMemory, serverDisk;
    private int jobCount = 0;


    //allToLargest variables
    private int largestCore = 0;

    public Client(String address, int port, String option) {
        try {
        	//try to open connection to server given IP address and port
        	 try {
                 socket = new Socket(address, port);
                 out = new PrintWriter(socket.getOutputStream());
                 input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
                 sendToServer("HELO");
             } catch(IOException io) {
                 System.out.println(io);
             }
        	 
        	 //check if server sends OK back
            if(newStatus("OK")) {
                sendToServer("AUTH " + System.getProperty("user.name"));
            }

            //Check for Jobs
            while (!newStatus("NONE")){
                if(currEquals("OK")) {
                    sendToServer("REDY");
                } else if (input1.startsWith("JOBN")) {
                    jobRecieve();
                    sendToServer("RESC All");
                    
                    if(newStatus("DATA")) {
                        sendToServer("OK");
                    }

                    //wait until data being sent has finished
                    while (!newStatus(".")) {
                        serverRecieve();    
                        allToLargest();
                        sendToServer("OK");
                    }
                    sendToServer("SCHD " + jobCount + " " + finalServerName + " " + finalServerNumber);
                    jobCount++;
                } 
            }

            closeConnection();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    
    public void allToLargest() {
        if(largestCore < serverCPU) {
            finalServerName = serverType;
            finalServerNumber = 0;
            largestCore = serverCPU;
        }
    }


    public void jobRecieve() {
        String[] splitJob = input1.split("\\s+");
        jobSubTime = Integer.parseInt(splitJob[1]);
        jobnumber = Integer.parseInt(splitJob[2]);
        jobRunTime = Integer.parseInt(splitJob[3]);
        jobCPU = Integer.parseInt(splitJob[4]);
        jobMemory = Integer.parseInt(splitJob[5]);
        jobDisk = Integer.parseInt(splitJob[6]);
    }

    public void serverRecieve() {
        String[] splitServer = input1.split("\\s+");
        serverType = splitServer[0];
        serverID = Integer.parseInt(splitServer[1]);
        serverState = Integer.parseInt(splitServer[2]);
        serverTime = Integer.parseInt(splitServer[3]);
        serverCPU = Integer.parseInt(splitServer[4]);
        serverMemory = Integer.parseInt(splitServer[5]);
        serverDisk = Integer.parseInt(splitServer[6]);
    }



    // Terminate connection
    public void closeConnection() {
        try {
            sendToServer("QUIT");
            input.close();
            out.close();
            socket.close();
        } catch(IOException io) {
            System.out.println(io);
        }
    }

    // Send message to server
    public void sendToServer(String msg) {
        out.write(msg + "\n");
        out.flush();
    }

    //check if current input line is equal to String str
    public boolean currEquals(String str) {
        try {
            if(input1.equals(str)){
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    //check if new input line equals to String str 
    public boolean newStatus(String str) {
    	
    	System.out.print(str);
        try {
        
            input1 = input.readLine();
            if(input1.equals(str)){
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public static void main(String args[]) {
        String option = "allToLargest";

        if (args.length == 2 && args[0].equals("-a")) {
            option = args[1];
        } 

        Client client = new Client("127.0.0.1", 50000, option);
    }
}

