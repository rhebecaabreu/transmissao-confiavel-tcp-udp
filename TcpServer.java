public class TcpServer {
  private String fileName;
  private int udpPortServer;
  private int windowSize;
  private float lp;

  public TcpServer(String fileName, int udpPortServer, int windowSize, float lp) {
    this.fileName = fileName;
    this.udpPortServer = udpPortServer;
    this.windowSize = windowSize;
    this.lp = lp;

    initServer();
  }

  public void initServer() { 
    
  }

  public static void main(String[] args) {
    String fileName = args[0];
    int udpPortServer = args[1];
    int windowSize = args[2];
    float lp = args[3];

    TcpServer tcpServer = new TcpServer(fileName, udpPortServer, windowSize, lp);
  }
}