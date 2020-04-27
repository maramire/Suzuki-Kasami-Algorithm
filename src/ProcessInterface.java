import java.io.IOException;
import java.rmi.*;

public interface ProcessInterface extends Remote {
    void attemptSC() throws RemoteException, IOException, InterruptedException;
    void request(int id, int seq) throws RemoteException;
    void takeToken(Token token) throws RemoteException;
    void kill() throws RemoteException, InterruptedException;
    boolean supreme() throws RemoteException;
}