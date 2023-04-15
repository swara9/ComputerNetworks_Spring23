import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    public static void main(String[] args) throws IOException {
        int sPort = 8000;

        System.out.println("Waiting for connection on port " + sPort);
        ServerSocket listener = new ServerSocket(sPort);
        int clientNum = 1;
        try {
            while(true) {
                new ServerThread(listener.accept(),clientNum).start();
                System.out.println("Client "  + clientNum + " is connected!");
                clientNum++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            listener.close();
        }

        // ArrayList<ServerThread> threadList = new ArrayList<>();
        // if(args.length == 1) {
        //     sPort = Integer.parseInt(args[0]);
        // }
        // try(ServerSocket serverSocket = new ServerSocket(sPort)){

        //     while(true){

        //         Socket socket = serverSocket.accept();
        //         ServerThread serverThread = new ServerThread(socket, threadList);
        //         threadList.add(serverThread);
        //         serverThread.start();
        //     }
        // }
    }
}

class ServerThread extends Thread {
    int sPort = 8000;    //The server will be listening on this port number
    //    ServerSocket sSocket;   //serversocket used to lisen on port number 8000
    Socket connection = null; //socket for the connection with the client
    String message;    //message received from the client

    ObjectOutputStream out;  //stream write to the socket
    ObjectInputStream in;    //stream read from the socket
    int no;

    public ServerThread(Socket socket, int no) {
        this.connection = socket;
        this.no = no;
    }

    @Override
    public void run() {
        try {
            //create a server socket
            //Wait for connection
            //accept a connection from the client
//            connection = sSocket.accept();
            // System.out.println("Connection received from client " + no);
            //initialize Input and Output streams
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());
            while (true) {
                //receive the message sent from the client
                message = (String) in.readObject();
                System.out.println("\nReceived message: " + message + " from client " + no);

                switch (message) {
                    //receive file from client
                    case "upload": {
                        String filename = (String) in.readObject();
                        System.out.println("filename = " + filename);
                        receiveFileFromClient(filename);
                        break;
                    }
                    //send file to client
                    case "get": {
                        String filename = (String) in.readObject();
                        System.out.println("filename = " + filename);
                        sendFileToClient(filename);
                        break;
                    }

                    case "exit":
//                        System.exit(1);
                        break;

                }
            }
        } catch (SocketException se) {
            System.out.println("Client " + no +" disconnected...");
        } catch (IOException | ClassNotFoundException ioException) {
            System.out.println("Client " + no +" disconnected...");
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            //Close connections
            try {
                in.close();
                out.close();
//                sSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void receiveFileFromClient(String fileName) throws IOException {
        int bytes = 0;
        String path = "server/new" + fileName;

        FileOutputStream fileOutputStream
                = new FileOutputStream(path);

        long size
                = in.readLong(); // read file size
        byte[] buffer = new byte[1024];
        while (size > 0
                && (bytes = in.read(
                buffer, 0,
                (int) Math.min(buffer.length, size)))
                != -1) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        // Here we received file
        fileOutputStream.close();
        System.out.println("File Received");
    }

    private void sendFileToClient(String filename) throws Exception {
        int bytes = 0;
        // Open the File where he located in your pc
        String path = "server/" + filename;
        File file = new File(path);
        if (!file.exists()) {
            String errMessage = "File does not exist on server...";
            System.out.println(errMessage);
            out.writeObject(errMessage);
        } else {
            out.writeObject("Receiving file from server...");
            FileInputStream fileInputStream
                    = new FileInputStream(file);

            // Here we send the File to Server
            out.writeLong(file.length());
            // Here we  break file into chunks
            byte[] buffer = new byte[1024];
            while ((bytes = fileInputStream.read(buffer)) > 0) {
                // Send the file to Server Socket
                out.write(buffer, 0, bytes);
                out.flush();
            }
            // close the file here
            fileInputStream.close();
            System.out.println("File sent");
        }

    }
}
