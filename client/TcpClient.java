import java.io.*;
import java.util.*;
import java.net.*;

public class TcpClient {

  private String fileName;
  private String endIpServer;
  private int udpPortServer;
  private int windowSize;
  private int timeout;
  private int tcpMss;
  private int dupack;
  private float lp;

  public TcpClient(String fileName, String endIpServer, int udpPortServer, int windowSize, int timeout, 
    int tcpMss, int dupack, float lp) {

      this.fileName = fileName;
      this.endIpServer = endIpServer;
      this.udpPortServer = udpPortServer;
      this.windowSize = windowSize;
      this.timeout = timeout;
      this.tcpMss = tcpMss;
      this.dupack = dupack;
      this.lp = lp; 

      initClient();
  }

  public void initClient() {
    
    timeout = 0;
    int clientPortNumber = 8081;
    int seqNum = 0;
    int receivedSeqNum;
    boolean ackReceived, timeoutIncorrectSeqNum;

    try {
      System.out.println("Sending the file... ");
      
      DatagramSocket socket = new DatagramSocket();
      InetAddress address = InetAddress.getByName(endIpServer);
      File file = new File(fileName);
      
      InputStream fileToSend = new FileInputStream(file);
      byte sendData[] = new byte[(int)file.length()];
      fileToSend.read(sendData);

      Timer timer = new Timer();

      // datagramSocketACK.setSoTimeout(timeout);
      // System.out.println("Client Host Address: " + datagramSocketACK.getLocalAddress());
      // System.out.println("Port Number to Recieve ACKS: " + datagramSocketACK.getLocalPort());
      
      // Create a flag to indicate the last message and a 16-bit sequence number
      int sequenceNumber = 0;
      boolean lastMessageFlag = false;
      
      // Create a flag to indicate the last acknowledged message and a 16-bit sequence
      // number
      int ackSequenceNumber = 0;
      int lastAckedSequenceNumber = 0;
      boolean lastAcknowledgedFlag = false;

      // Create a counter to count number of retransmissions and initialize window
      // size
      int retransmissionCounter = 0;

      // Vector to store the sent messages
      Vector<byte[]> sentMessageList = new Vector<byte[]>();

      // For as each message we will create
      for (int i = 0; i < sendData.length; i = i + 1021) {

        // Increment sequence number
        sequenceNumber += 1;

        // Create new byte array for message
        byte[] message = new byte[1024];

        // Set the first and second bytes of the message to the sequence number
        message[0] = (byte) (sequenceNumber >> 8);
        message[1] = (byte) (sequenceNumber);

        // Set flag to 1 if packet is last packet and store it in third byte of header
        if ((i + 1021) >= sendData.length) {
          lastMessageFlag = true;
          message[2] = (byte) (1);
        } else { // If not last message store flag as 0
          lastMessageFlag = false;
          message[2] = (byte) (0);
        }

        // Copy the bytes for the message to the message array
        if (!lastMessageFlag) {
          for (int j = 0; j != 1021; j++) {
            message[j + 3] = sendData[i + j];
          }
        } else if (lastMessageFlag) { // If it is the last message
          for (int j = 0; j < (sendData.length - i); j++) {
            message[j + 3] = sendData[i + j];
          }
        }

        // Package the message
        DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, udpPortServer);

        // Add the message to the sent message list
        sentMessageList.add(message);

        while (true) {
          // If next sequence number is outside the window
          if ((sequenceNumber - windowSize) > lastAckedSequenceNumber) {

            boolean ackRecievedCorrect = false;
            boolean ackPacketReceived = false;

            while (!ackRecievedCorrect) {
              // Check for an ack
              byte[] ack = new byte[2];
              DatagramPacket ackpack = new DatagramPacket(ack, ack.length);

              try {
                socket.setSoTimeout(timeout);
                socket.receive(ackpack);
                ackSequenceNumber = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff);
                ackPacketReceived = true;
              } catch (SocketTimeoutException e) {
                ackPacketReceived = false;
                // System.out.println("Socket timed out while waiting for an acknowledgement");
                // e.printStackTrace();
              }

              if (ackPacketReceived) {
                if (ackSequenceNumber >= (lastAckedSequenceNumber + 1)) {
                  lastAckedSequenceNumber = ackSequenceNumber;
                }
                ackRecievedCorrect = true;
                System.out.println("Ack recieved: Sequence Number = " + ackSequenceNumber);
                break; // Break if there is an ack so the next packet can be sent
              } else { // Resend the packet
                System.out.println("Resending: Sequence Number = " + sequenceNumber);
                // Resend the packet following the last acknowledged packet and all following
                // that (cumulative acknowledgement)
                for (int y = 0; y != (sequenceNumber - lastAckedSequenceNumber); y++) {
                  byte[] resendMessage = new byte[1024];
                  resendMessage = sentMessageList.get(y + lastAckedSequenceNumber);

                  DatagramPacket resendPacket = new DatagramPacket(resendMessage, resendMessage.length, address, 
                      udpPortServer);
                  socket.send(resendPacket);
                  retransmissionCounter += 1;
                }
              }
            }
          } else { // Else pipeline is not full, break so we can send the message
            break;
          }
        }

        // Send the message
        socket.send(sendPacket);
        System.out.println("Sent: Sequence number = " + sequenceNumber + ", Flag = " + lastMessageFlag);

        // Check for acknowledgements
        while (true) {
          boolean ackPacketReceived = false;
          byte[] ack = new byte[2];
          DatagramPacket ackpack = new DatagramPacket(ack, ack.length);

          try {
            socket.setSoTimeout(timeout);
            socket.receive(ackpack);
            ackSequenceNumber = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff);
            ackPacketReceived = true;
          } catch (SocketTimeoutException e) {
            // System.out.println("Socket timed out waiting for an ack");
            ackPacketReceived = false;
            // e.printStackTrace();
            break;
          }

          // Note any acknowledgements and move window forward
          if (ackPacketReceived) {
            if (ackSequenceNumber >= (lastAckedSequenceNumber + 1)) {
              lastAckedSequenceNumber = ackSequenceNumber;
              System.out.println("Ack recieved: Sequence number = " + ackSequenceNumber);
            }
          }
        }
      }

      // Continue to check and resend until we receive final ack
      while (!lastAcknowledgedFlag) {

        boolean ackRecievedCorrect = false;
        boolean ackPacketReceived = false;

        while (!ackRecievedCorrect) {
          // Check for an ack
          byte[] ack = new byte[2];
          DatagramPacket ackpack = new DatagramPacket(ack, ack.length);

          try {
            socket.setSoTimeout(timeout);
            socket.receive(ackpack);
            ackSequenceNumber = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff);
            ackPacketReceived = true;
          } catch (SocketTimeoutException e) {
            // System.out.println("Socket timed out waiting for an ack1");
            ackPacketReceived = false;
            // e.printStackTrace();
          }

          // If its the last packet
          if (lastMessageFlag) {
            lastAcknowledgedFlag = true;
            break;
          }
          // Break if we receive acknowledgement so that we can send next packet
          if (ackPacketReceived) {
            System.out.println("Ack recieved: Sequence number = " + ackSequenceNumber);
            if (ackSequenceNumber >= (lastAckedSequenceNumber + 1)) {
              lastAckedSequenceNumber = ackSequenceNumber;
            }
            ackRecievedCorrect = true;
            break; // Break if there is an ack so the next packet can be sent
          } else { // Resend the packet
            // Resend the packet following the last acknowledged packet and all following
            // that (cumulative acknowledgement)
            for (int j = 0; j != (sequenceNumber - lastAckedSequenceNumber); j++) {
              byte[] resendMessage = new byte[1024];
              resendMessage = sentMessageList.get(j + lastAckedSequenceNumber);
              DatagramPacket resendPacket = new DatagramPacket(resendMessage, resendMessage.length, address, udpPortServer);
              socket.send(resendPacket);
              System.out.println("Resending: Sequence Number = " + lastAckedSequenceNumber);

              // Increment retransmission counter
              retransmissionCounter += 1;
            }
          }
        }
      }

      socket.close();
      System.out.println("File " + fileName + " has been sent");

      // Calculate the average throughput
      // int fileSizeKB = (sendData.length) / 1024;
      // // int transferTime = timer.getTimeElapsed() / 1000;
      // double throughput = (double) fileSizeKB / transferTime;
      // System.out.println("File size: " + fileSizeKB + "KB, Transfer time: " + 0 + " seconds. Throughput: "
      //     + throughput + "KBps");
      System.out.println("Number of retransmissions: " + retransmissionCounter);

   
    } catch (Exception e) {
      e.printStackTrace();
    }
  
  }

  public void startTransference(DatagramSocket datagramSocket, byte[] sendData, InetAddress address) {
 


  }

  public static void main(String[] args) {
    System.out.println(args.length);
    
    if (args.length > 8 || args.length < 8) {
      System.out.println("Syntax error \n use  <blablabla>");
      return;
    }

    String fileName = args[0];
    String endIpServer = args[1];
    int udpPortServer = Integer.valueOf(args[2]);
    int windowSize = Integer.valueOf(args[3]);
    int timeout = Integer.valueOf(args[4]);
    int tcpMss = Integer.valueOf(args[5]);
    int dupack = Integer.valueOf(args[6]);
    float lp = Float.valueOf(args[7]);
    

    if(dupack != 0 && dupack != 1) {
      System.out.println("Dupack invalid valor. \n(Must be 0 or 1)");
      return;
    }

    TcpClient tcpClient = new TcpClient(fileName, endIpServer, udpPortServer, windowSize, timeout, tcpMss, dupack, lp);

    // TcpClient tcpClient = new TcpClient("test.txt", "endIpServer", 8080, 200, 200, 200, 200, 200);
  }
}