import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class TCPServer {

   public static void main(String[] args) throws Exception {
       //Create a database of userinfo using Hashmap
       Map<String, String> userDB = new HashMap<String, String>();
       userDB.put("user1", "password1");
       userDB.put("user2", "password2");
       String fileName,ack;
       ServerSocket welcomeSocket = new ServerSocket(4040);
       while(true){
           System.out.println("Server waiting for new request.");
           Socket connectionSocket = welcomeSocket.accept();
          
            BufferedReader inFromClient = new BufferedReader(new
                                         InputStreamReader(connectionSocket.getInputStream()));
            OutputStream outToClient = connectionSocket.getOutputStream();
          
            String userInfo = inFromClient.readLine();
            try{
                if(userDB.get(userInfo.split(":")[0])==null || !userDB.get(userInfo.split(":")[0]).equals(userInfo.split(":")[1])){
                   outToClient.write("failed\n".getBytes());
                   ack = inFromClient.readLine();
                }else{
                   Path currentRelativePath = Paths.get("");
                   String s = currentRelativePath.toAbsolutePath().toString();
                   outToClient.write((s+"\n").getBytes());
                   while(true){
                       String cmd = inFromClient.readLine();
                       if(cmd.equals("1")){//Upload files
                           fileName = inFromClient.readLine();
                           saveFile(connectionSocket, fileName);
                       }else if(cmd.equals("2")) {//download files
                    	   	  fileName = inFromClient.readLine();
                    	   	  sendFile(connectionSocket, fileName);
                    	   	  }
                       else if(cmd.equals("3")){//Change directory
                           String newPath = inFromClient.readLine();
                           System.setProperty("user.dir", newPath);
                          
                           outToClient.write((Paths.get(newPath).toAbsolutePath().toString()+"\n").getBytes());
                           //File file = new File("data.csv").getAbsoluteFile();
                           //System.out.println(file.getPath());
                       }else if(cmd.equals("4")){//List files
                           File directory = new File(".");
                            File[] fList = directory.listFiles();
                            for (File file : fList){
                               if(file.isFile())
                                   outToClient.write((file.getName()+":").getBytes());
                            }
                            outToClient.write("\n".getBytes());
                       }else if(cmd.equals("5")){//Logout
                           connectionSocket.close();
                           break;
                       }
                       
                   }
                }
            }catch (Exception e) {
               e.printStackTrace();
            }
       }
   }
  
   private static void sendFile(Socket clientSocket, String fileName) throws IOException {
	   DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
       FileInputStream fis = new FileInputStream(fileName);
       byte[] buffer = new byte[4096];
      
       while (fis.read(buffer) > 0) {
           dos.write(buffer);
       }
      
       fis.close();
	
}

private static void saveFile(Socket clientSock, String fileName) throws IOException {
       DataInputStream dis = new DataInputStream(clientSock.getInputStream());
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
}
