import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface CallbackServerInterface extends Remote {


    public String sayHello() throws RemoteException;

    void addNeighbor(CallbackServerInterface URL) throws RemoteException;

    List<CallbackServerInterface> getNeighbors() throws RemoteException;

    CallbackServerInterface searchFile(String fileName) throws RemoteException;

    void giveMeFile(String fileName, CallbackServerInterface client) throws RemoteException;

    void consumeData(String fileName, byte[] data, int length) throws RemoteException;
//    public void registerForCallback(CallbackClientInterface callbackClientObj, String[] fileNames) throws RemoteException;
//
//    public void unregisterForCallback(CallbackClientInterface callbackClientObj) throws RemoteException;
//
//    public CallbackClientInterface searchFile(String fileName) throws RemoteException;
}
