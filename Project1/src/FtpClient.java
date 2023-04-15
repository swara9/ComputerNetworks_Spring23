import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class FtpClient {

    Socket requestSocket;           //socket connect to the server
    ObjectOutputStream out;         //stream write to the socket
    ObjectInputStream in;          //stream read from the socket
    String message;                //message send to the server
    String MESSAGE;                //capitalized message read from the server
    int sPort = 8000; //default

    //get Input from standard input
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    void run() throws Exception {
        try{
            //create a socket to connect to the server
            requestSocket = new Socket("localhost", sPort);
            System.out.println("Connected to localhost in port "+sPort);
            //initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
            while(true)
            {
                System.out.print("Please input a command: ");
                //read a sentence from the standard input
                message = bufferedReader.readLine();
                String[] command = message.split(" ");
                switch(command[0]){
                    case "get":
                    {
                        out.writeObject(command[0]);
                        out.writeObject(command[1]);
                        String response = (String) in.readObject();
                        String errMessage = "File does not exist on server...";
                        if(response.equalsIgnoreCase(errMessage)){
                            System.out.println(errMessage);
                        }else{
                            System.out.println(response);
                            receiveFileFromServer(command[1]);
                        }
                        break;
                    }
                    case "upload":{
                        uploadFileToServer(command[1]);
                        break;
                    }
                    case "exit":
                    {
                        out.writeObject(command[0]);
                        System.exit(1);
                        break;
                    }
                    default:
                        break;
                }
                //Send the sentence to the server
                //Receive the upperCase sentence from the server
//                MESSAGE = (String)in.readObject();
                //show the message to the user
//                System.out.println("Receive message: " + MESSAGE);
            }
        }
        catch (ConnectException e) {
            System.err.println("Connection refused. Please check if the server is running OR enter the correct server port number.");
            System.out.println("Enter correct port number:");
            Scanner scanner = new Scanner(System.in);
            sPort = scanner.nextInt();
            run();
        }
        catch ( ClassNotFoundException e ) {
            System.err.println("Class not found");
        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
        finally{
            //Close connections
            try{
                in.close();
                out.close();
                requestSocket.close();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }


    private void receiveFileFromServer(String fileName) throws IOException {
        int bytes = 0;
        String path = "Project1/client/new"+fileName;

        FileOutputStream fileOutputStream
                = new FileOutputStream(path);

        long size
                = in.readLong(); // read file size
        byte[] buffer = new byte[1024];
        while (size > 0
                && (bytes = in.read(
                buffer, 0,
                (int)Math.min(buffer.length, size)))
                != -1) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        // Here we received file
        fileOutputStream.close();
        System.out.println("File Received");
    }

    private void uploadFileToServer(String filename) throws Exception
    {
        int bytes = 0;
        // Open the File where he located in your pc
        String path = "Project1/client/"+filename;
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("File does not Exist in client...");
        }else{
            out.writeObject("upload");
            out.writeObject(filename);
            FileInputStream fileInputStream
                    = new FileInputStream(file);

            // Here we send the File to Server
            out.writeLong(file.length());
            // Here we  break file into chunks
            byte[] buffer = new byte[1024];
            while ((bytes = fileInputStream.read(buffer))>0) {
                // Send the file to Server Socket
                out.write(buffer, 0, bytes);
                out.flush();
            }
            // close the file here
            fileInputStream.close();
            System.out.println("File sent");
        }

    }

    //main method
    public static void main(String args[]) throws Exception {

        FtpClient client = new FtpClient();

        if(args.length == 1) {
            client.sPort = Integer.parseInt(args[0]);
        }
        client.run();
    }

}
