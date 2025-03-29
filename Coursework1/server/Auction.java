import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.crypto.SealedObject;

public interface Auction extends Remote {
    public SealedObject getSpec(int itemID) throws RemoteException;
}