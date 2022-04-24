import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.List;

public class CallbackServer {

    static final String MULTICAST_ADDRESS = "230.1.1.1";
    static final int MULTICAST_PORT = 12345;

    public static void main(String[] args) {


        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        String portNum, serverName, registryURL;

        try {

            System.out.println("Enter the RMI registry port number: ");
            portNum = (br.readLine()).trim();
            int RMIPortNum = Integer.parseInt(portNum);

            System.out.println("Enter server name: ");
            serverName = (br.readLine()).trim();

            System.out.println("enter folder name you want to share, Please?(Folder must be in E drive)");
            String path = br.readLine();

            startRegistry(RMIPortNum);

            CallbackServerImpl exportedObj = new CallbackServerImpl(serverName, path);
            registryURL = "rmi://localhost:" + portNum + "/" + serverName;
            Naming.rebind(registryURL, exportedObj);
            System.out.println("Server ready.");

            ReceiveService receiveService = new ReceiveService(exportedObj, registryURL);
            Thread rs = new Thread(receiveService);
            rs.start();

            UIManager.put("OptionPane.minimumSize",new Dimension(150,150));

            do {
                String[] options = {"find neighbors", "Find File", "Exit"};
                int choice = JOptionPane.showOptionDialog(null, "Choose an action", serverName, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

                switch (choice) {

                    case 0 -> {
                        InetAddress mcIPAddress = InetAddress.getByName(MULTICAST_ADDRESS);
                        MulticastSocket mcSocket = new MulticastSocket(MULTICAST_PORT);
                        byte[] buf= registryURL.getBytes();
                        DatagramPacket packet = new DatagramPacket(buf, buf.length, mcIPAddress, MULTICAST_PORT);

                        mcSocket.send(packet);
                        System.out.println("Multicast Message Sent Successfully.");
                    }

                    case 1 -> {
                        String fileName = JOptionPane.showInputDialog("Type the name of the file you want to find.");
                        try {
                            List<CallbackServerInterface> neighbors = exportedObj.getNeighbors();
                            List<CallbackServerInterface> result = new ArrayList<>();

                            for (CallbackServerInterface neighbor : neighbors) {
                                        result.add(neighbor.searchFile(fileName));
                            }

                            for (CallbackServerInterface neighbor : result) {
                                if (neighbor != null) {
                                    neighbor.giveMeFile(fileName, exportedObj);
                                    System.out.println("you downloaded the file successfully");
                                    break;
                                }
                            }


                        } catch (NoSuchElementException ex) {
                            JOptionPane.showMessageDialog(null, "Not found");
                        }
                    }

                    default -> {
                        System.exit(0);
                    }
                }

            } while (true);

        } catch (Exception e) {
            System.out.println("Exception in server.main");
            e.printStackTrace();
        }
    }

    private static void startRegistry(int RMIPortNum) throws RemoteException {

        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            //I need to use registry so it doesn't get garbage collected. (I wasted too much time on this bug)
            System.out.println(Arrays.toString(registry.list()));
        }
        catch (RemoteException e) {

            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
        }
    }
}
