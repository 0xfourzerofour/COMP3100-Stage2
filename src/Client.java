//Group 16 Vanilla Client DS-SIM

import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {
    private Socket socket            = null;
    private PrintWriter out = null;
    private BufferedReader input = null;
    private String input1 = "";
  
    private int jobCpuCores, jobMemory, jobDisk;
    private String serverType;
    private int serverID;
    private int jobCount = 0;


    public Client(String algo ,String address, int port) {
        try {
        	
        	 try {
                 socket = new Socket(address, port);
                 out = new PrintWriter(socket.getOutputStream());
                 input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
                 sendToServer("HELO");
             } catch(IOException io) {
                 System.out.println(io);
             }
  
            if(newStatus("OK")) {
                sendToServer("AUTH " + System.getProperty("user.name"));
            }
           
            while (!newStatus("NONE")){
                if(currentStatus("OK")) {
                    sendToServer("REDY");
                } else if (input1.startsWith("JOBN")) {
                    jobRecieve();
                    sendToServer("RESC Avail" + " " + jobCpuCores  + " " +  jobMemory  + " " +  jobDisk);
                    
                    if(newStatus("DATA")) {
                        sendToServer("OK");
                    }

                    while (!newStatus(".")) {
                        serverRecieve();    
                        sendToServer("OK");
                    }
                    sendToServer("SCHD " + jobCount + " " + serverType + " " + serverID);

                  jobCount++;
                } 
            }

            closeConnection();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void jobRecieve() {
        String[] jobInput = input1.split("\\s+");
        jobCpuCores = Integer.parseInt(jobInput[4]);
        jobMemory = Integer.parseInt(jobInput[5]);
        jobDisk = Integer.parseInt(jobInput[6]);
    }

    public void serverRecieve() {
        String[] serverInput = input1.split("\\s+");
        serverType = serverInput[0];
        serverID = Integer.parseInt(serverInput[1]);
    }

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

   
    public void sendToServer(String x) {
        out.write(x + "\n");
        out.flush();
    }


    public boolean currentStatus(String x) {
        try {
            if(input1.equals(x)){
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public boolean newStatus(String x) {
    	
        try {
        	input1 = input.readLine();
            
            if(input1.equals(x)){
                return true;
            }
            
            
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public static void main(String[] args) {
    	
    	String algo = "bf"; 
    	
    	if(args.length > 1 && args[0] == "-a") {
    		algo = args[1]; 
    	}

        Client client = new Client( algo, "127.0.0.1", 50000);
    }
}

