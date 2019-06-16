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
  }

  public void initClient() {

  }

  public static void main(String[] args) {
    String fileName = args[0];
    String endIpServer = args[1];
    int udpPortServer = args[2];
    int windowSize = args[3];
    int timeout = args[4];
    int tcpMss = args[5];
    int dupack = args[6];
    float lp = args[7];

    if(dupack != 0 && dupack != 1) {
      System.out.println("Dupack invalid valor. \n(Must be 0 or 1)");
      System.exit(0);
    }

    TcpClient tcpClient = new TcpClient(fileName, endIpServer, udpPortServer, windowSize, timeout, 
   tcpMss, dupack, lp);
  }
}