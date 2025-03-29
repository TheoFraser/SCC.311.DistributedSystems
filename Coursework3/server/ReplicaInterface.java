import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.HashMap;

public interface ReplicaInterface extends Remote {
    void makePrimaryReplica(int replicaID) throws RemoteException;
    void updateRegister(String email, int userSize) throws RemoteException;
    void updateNewAuction(int userID,  AuctionSaleItem item, int itemID, AuctionItem item2) throws RemoteException;
    void updateCloseAuction(int userID, int itemID) throws RemoteException;
    void updateBid(int userID, int itemID, int price)  throws RemoteException;
    boolean checkIfAlive() throws RemoteException;
    public Integer primaryRegister(String email, PublicKey key) throws RemoteException;
    public Integer primaryNewAuction(int userID, AuctionSaleItem item, String key) throws
    RemoteException;
    public AuctionResult primaryCloseAuction(int userID, int itemID, String key) throws
    RemoteException;
    public boolean primaryBid(int userID, int itemID, int price, String key) throws
    RemoteException;
    void updateAuctionItem(HashMap<Integer,AuctionItem> auctionItems)throws RemoteException;
    void updateWinningEmailList(HashMap<Integer,String> winningEmailList) throws RemoteException;
    void updateUsers(HashMap<Integer,User> users) throws RemoteException;
    void updatePrimaryReplicaID(int pReplicaID) throws RemoteException;
    void messageAllReplicas() throws RemoteException;
    int getState() throws RemoteException;
    int getPrimaryReplicaID() throws RemoteException;
    HashMap<Integer,AuctionItem> getAuctionItems() throws RemoteException;
    HashMap<Integer,String> getWinningEmailList() throws RemoteException;
    HashMap<Integer,User> getUsers() throws RemoteException;
}