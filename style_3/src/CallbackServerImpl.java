import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.function.Predicate;

public class CallbackServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

//    private final Map<CallbackClientInterface, String[]> clientFiles;
    private String[] fileNames;
    private String sourceDir;
    private String serverName, path;
    private List<CallbackServerInterface> neighbors;

    private boolean visited = false;

    public CallbackServerImpl(String serverName, String path) throws RemoteException {
        super();
        File dir = new File("E:/" + path);
        this.fileNames = dir.list();
        this.sourceDir = "E:/" + path;
        this.serverName = serverName;
        this.path = path;
        neighbors = new ArrayList<>();
    }

    @Override
    public String sayHello() throws RemoteException {
        return "Hello";
    }

    @Override
    public void addNeighbor(CallbackServerInterface neighbor) throws RemoteException {
        neighbors.add(neighbor);
        System.out.println("you added neighbor");

    }

    @Override
    public List<CallbackServerInterface> getNeighbors() throws RemoteException {

        return neighbors;
    }

    @Override
    public CallbackServerInterface searchFile(String fileName) throws RemoteException {

        if (!visited) {
            System.out.println("**********************************\n"
                    + "Searching For File ----");

            for (String file : fileNames) {
                if (file.equalsIgnoreCase(fileName)) {
                    System.out.println("Found file at:" + sourceDir);

                    return this;
                }
            }

            for (CallbackServerInterface neighbor : neighbors) {

                return ((CallbackServerInterface) neighbor).searchFile(fileName);
            }
        }
        visited = true;
        return null;
    }

    @Override
    public void giveMeFile(String fileName, CallbackServerInterface client) throws RemoteException {
        File f1 = new File(sourceDir + "/" + fileName);
        try {

            FileInputStream in = new FileInputStream(f1);
            byte[] myData = new byte[1024 * 1242];
            int len = in.read(myData);

            while (len > 0) {
                client.consumeData(f1.getName(), myData, len);
                len = in.read(myData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void consumeData(String fileName, byte[] data, int length) throws RemoteException {
        System.out.println("start transfer!");
        File file = new File(this.sourceDir + "/" + fileName);

        try {
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
            os.write(data, 0, length);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(null, "download complete" + "\n");
    }
}
