import javax.swing.*;
import java.net.*;
import java.rmi.Naming;

public class ReceiveService implements Runnable {

    private CallbackServerInterface exportedObj;
    private String registryURL;

    public ReceiveService(CallbackServerInterface exportedObj, String registryURL) {
        this.exportedObj = exportedObj;
        this.registryURL = registryURL;
    }

    @Override
    public void run() {

        try {
//            NetworkInterface ni = NetworkInterface.getByName("interface");
//            InetAddress mcIPAddress = InetAddress.getByName(CallbackServer.MULTICAST_ADDRESS);
//            SocketAddress socketAddress = new InetSocketAddress(mcIPAddress, CallbackServer.MULTICAST_PORT);

            InetAddress mcIPAddress = InetAddress.getByName(CallbackServer.MULTICAST_ADDRESS);
            MulticastSocket mcSocket = new MulticastSocket(CallbackServer.MULTICAST_PORT);
            mcSocket.joinGroup(mcIPAddress);

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length, mcIPAddress, CallbackServer.MULTICAST_PORT );

            System.out.println("Waiting for a  multicast message...");


            while (true) {
                mcSocket.receive(packet);
                String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());

                if (!registryURL.equalsIgnoreCase(msg)) {
                    System.out.println("[Multicast  Receiver] Received " + msg);
                    CallbackServerInterface neighbor = (CallbackServerInterface) Naming.lookup(msg);
                    neighbor.addNeighbor(exportedObj);
                    System.out.println("you have sent your remote reference to: " + msg);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }


    }
}
