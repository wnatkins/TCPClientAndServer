import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;


public class TCPClient {

   public static void main(String[] args) throws Exception {
       String userName, password,choice;
       String fileName;
       String ack;
       String confirm = "y";
       
  
           while(true){
        	   BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
       	  	System.out.print("Enter user name:");
       	  	userName = inFromUser.readLine();
       	  	System.out.print("Enter password:");
          	password = inFromUser.readLine();
         
          	Socket clientSocket = new Socket("localhost",4040);
          	OutputStream outToServer = clientSocket.getOutputStream();
          	BufferedReader inFromServer = new BufferedReader(
                                                   new InputStreamReader(clientSocket.getInputStream()));
          	outToServer.write((userName+":"+password+"\n").getBytes());
          	String ret = inFromServer.readLine();
          	if(ret.equals("failed")){
              System.out.println("Authentication failed...");
              outToServer.write("ack\n".getBytes());
          	}else{
              System.out.println("Successfully logged in to current directory "+ret);
         }
               System.out.println("Menu");
               System.out.println("\n1. Upload File.");
               System.out.println("2. Download File.");
               System.out.println("3. Change Directory.");
               System.out.println("4. List Files in current directory.");
               System.out.println("5. Logout.");
               System.out.print("Enter Your Choice:");
               choice = inFromUser.readLine();
               switch(choice){
                   case "1":
                       System.out.print("Enter file name to upload: ");
                       fileName = inFromUser.readLine();
                       File f = new File(fileName);
                       if(!f.exists()){
                           System.out.println(fileName+" does not exist!");
                       }else{
                           outToServer.write("1\n".getBytes());
                           //Now send the file name
                           outToServer.write((fileName+"\n").getBytes());
                           //Finally send the file content.
                           sendFile(fileName, clientSocket);
                           System.out.println(fileName+" uploaded successfully.");
                       }
                       break;
                   case "2":
                	   	   System.out.println("Enter file name to download: ");
                	   	   fileName = inFromUser.readLine();
                	   	   File n = new File(fileName);
                	   	   if(!n.exists()) {
                	   		   System.out.println(fileName + " does not exist!");
                	   	   }else {
                	   		   outToServer.write("2\n".getBytes());
                	   		   outToServer.write((fileName+"\n").getBytes());
                	   		   saveFile(fileName, clientSocket);
                	   		   System.out.println(fileName + " uploaded successfully");
                	   	   }
                       
                       break;
                   case "3":
                       outToServer.write("3\n".getBytes());
                       System.out.print("Enter new path:");
                       String newPath=inFromUser.readLine();
                       //Now send the new path to change
                       outToServer.write((newPath+"\n").getBytes());
                       ret = inFromServer.readLine();
                       System.out.println("Current directory changed to "+ret);
                       break;
                   case "4":
                       outToServer.write("4\n".getBytes());
                       //Receive list of files from the server and print them
                       String fileNames = inFromServer.readLine();
                       System.out.println("List of files in the current directory are:");
                       for(String fName:fileNames.split(":")){
                           System.out.println(fName);
                       }
                       break;
                   case "5":
                       outToServer.write("5\n".getBytes());
                       System.out.println("Logged out successfully.");
                       System.out.println("\nWould you like to login another user? y/n");
                       confirm = inFromUser.readLine();
                       if(confirm.equalsIgnoreCase("n")) {
                    	   		System.out.println("Session complete. Thanks!");
                    	   		System.exit(0);
                    	   		
                       }
                       else if(confirm.equalsIgnoreCase("y")) {
                    	   confirm = "y";
                       }
                       
               }
           }
       }

   

   private static void saveFile(String fileName, Socket t) throws IOException {
	   DataInputStream dis = new DataInputStream(t.getInputStream());
       File file = new File(fileName).getAbsoluteFile();
       FileOutputStream fos = new FileOutputStream(file);
       byte[] buffer = new byte[4096];
      
       int filesize = 15123;
       int read = 0;
       int totalRead = 0;
       int remaining = filesize;
       while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
           totalRead += read;
           remaining -= read;
           fos.write(buffer, 0, read);
       }
      
       fos.close();
}

public static void sendFile(String file, Socket s) throws IOException {
       DataOutputStream dos = new DataOutputStream(s.getOutputStream());
       FileInputStream fis = new FileInputStream(file);
       byte[] buffer = new byte[4096];
      
       while (fis.read(buffer) > 0) {
           dos.write(buffer);
       }
      
       fis.close();
   }

}
