import java.io.*;
import java.net.*;

public class TcpServer implements Runnable {
  private String fileName;
  private int udpPortServer;
  private int windowSize;
  private float lp;  
  
  private DatagramSocket socketIn, socketOut;

  public TcpServer(String fileName, int udpPortServer, int windowSize, float lp) {
    this.fileName = fileName;
    this.udpPortServer = udpPortServer;
    this.windowSize = windowSize;
    this.lp = lp;
  }

  @Override
  public void run() {
    // Cria um novo thread para transmitir mensagens do cliente para o servidor.
    new Thread() {
      @Override
      public void run() {
        initServer();
      }
    }.start();
  }

  public void initServer() { 
    try {
      byte b[] = new byte[3072];
      DatagramSocket dsoc = new DatagramSocket(udpPortServer);
      FileOutputStream f = new FileOutputStream(fileName);
      while (true) {
        System.out.println("alo");
        DatagramPacket dp = new DatagramPacket(b, b.length);
        dsoc.receive(dp);

        File file = new File(fileName);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(new String(dp.getData(), 0, dp.getLength()));
        fileWriter.flush();
        fileWriter.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    String fileName = args[0];
    int udpPortServer = Integer.valueOf(args[1]);
    int windowSize = Integer.valueOf(args[2]);
    float lp = Float.valueOf(args[3]);

    if (args.length < 4) {
      System.out.println("Syntax error \n use  <blablabla>");
      return;
    }
    try {
      TcpServer tcpServer = new TcpServer(fileName, udpPortServer, windowSize, lp);
      Thread thread = new Thread(tcpServer);
      thread.start();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}