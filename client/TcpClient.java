import java.io.*;
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
    
    try {
      InetAddress address = InetAddress.getByName(endIpServer);
      DatagramSocket datagramSocket = new DatagramSocket();

      File fileToSend = new File(fileName);
      byte sendData[] = new byte[(int) fileToSend.length()];


      FileInputStream f = new FileInputStream(fileName);
      int i = 0;
      while (f.available() != 0) {
        sendData[i] = (byte) f.read();
        i++;
      }
      f.close();
      datagramSocket.send(new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), udpPortServer));

    } catch (Exception e) {
      e.printStackTrace();
    }
  
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