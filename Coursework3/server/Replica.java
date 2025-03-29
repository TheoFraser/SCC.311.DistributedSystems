import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.HashMap;

public class Replica extends Server implements ReplicaInterface{
    int replicaID;
    int primaryReplicaID;
    HashMap<Integer, ReplicaInterface> replicas = new HashMap<Integer,ReplicaInterface>();
    Replica replica;
    Server s;
    int state = 0;
    public Replica(int replicaID)
    {
        super();
        this.replicaID = replicaID;
        try 
        {
            s = new Server();
            String name = "Replica" + replicaID;
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } 
        catch (Exception e) 
        {
            System.err.println(e);
        }
        
    }
    public boolean checkIfAlive()
    {
        try {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public void makePrimaryReplica(int primaryReplicaID)
    {
        this.primaryReplicaID = primaryReplicaID;
        s.primaryReplicaID = primaryReplicaID;
        getAllReplicas();
        //s.changePrimaryReplicaID(primaryReplicaID);   
    }
    public void getAllReplicas()
    {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 0);
            String[] bindingNames = registry.list();
            String prefix = "RepI";
            int replicaNumber = 0;
            for (String bindingName : bindingNames) {
                if (bindingName.startsWith(prefix)) {
                    try {
                        System.out.println("Found: " + bindingName + " -> " );
                        Object obj = registry.lookup(bindingName);
                        String numberStr = bindingName.replaceAll("\\D+", "");
                        replicaNumber = Integer.parseInt(numberStr);
                        ReplicaInterface otherReplica = (ReplicaInterface) obj; 
                        if(otherReplica.checkIfAlive() == false)
                        {
                            replicas.remove(replicaNumber);
                            System.out.println("DEAD" + bindingName + " -> ");                                   
                        }
                        else
                        {
                            replicas.put(replicaNumber, otherReplica);
                        }
                    } catch (Exception e) 
                    {
                        replicas.remove(replicaNumber);
                        continue;
                    }
                }
            }
        } catch (Exception e) {
        }
    }
    public int getPrimaryReplicaID()
    {
        return s.primaryReplicaID;
    }
    public HashMap<Integer,AuctionItem> getAuctionItems()
    {
        return s.auctionItems;
    }
    public HashMap<Integer,String> getWinningEmailList()
    {
        return s.winningEmailList;
    }
    public HashMap<Integer,User> getUsers()
    {
        return s.users;
    }
    public void updatePrimaryReplicaID(int pReplicaID)
    {
        s.primaryReplicaID = pReplicaID;
    }
    public void updateAuctionItem(HashMap<Integer,AuctionItem> auctionItems)
    {
        s.auctionItems = auctionItems;
        // showItems();
    }
    public void updateWinningEmailList(HashMap<Integer,String> winningEmailList)
    {
        s.winningEmailList = winningEmailList;
        //System.out.println("WINNING EMAIL LIST " + s.winningEmailList.get(0));
    }
    public void updateUsers(HashMap<Integer,User> users)
    {
        s.users = users;
        // System.out.println("USERS " + s.users.get(0).email);
    }
    public void showItems()
    {
        for (int key : s.auctionItems.keySet()) {
            System.out.println(s.auctionItems.get(key).itemID + " | " + s.auctionItems.get(key).name + " | " + s.auctionItems.get(key).description + " | " + s.auctionItems.get(key).highestBid);
        }
    }
    public void updateRegister(String email, int userID)
    {
        s.users.put(userID, new User(email));
        state = state + 1;
    }
    public void updateNewAuction(int userID,  AuctionSaleItem item, int itemID, AuctionItem item2)
    {
        s.users.get(userID).auctionSaleItems.put(itemID, item);
        s.winningEmailList.put(itemID, s.users.get(userID).email);
        s.auctionItems.put(itemID, item2);
        state = state + 1;
        System.out.println("STATE is " + state);
        showItems();
    } 
    public void updateCloseAuction(int userID, int itemID)
    {
        s.auctionItems.remove(itemID);
        s.winningEmailList.remove(itemID);
        s.users.get(userID).auctionSaleItems.remove(itemID);
        state = state + 1;
        showItems();
    }
    public void updateBid(int userID, int itemID, int price)
    {
        s.auctionItems.get(itemID).highestBid = price;
        s.winningEmailList.put(itemID, s.users.get(userID).email);
        state = state + 1;
    }
    public Integer primaryRegister(String email, PublicKey pKey)
    {
        getAllReplicas();
        try {
            int userID = s.register(email, pKey);
            for (int key : replicas.keySet()) {
                replicas.get(key).updateRegister(email, userID);
                System.out.println("LOL");
            }
            return userID;
            
        } catch (Exception e) {
            return null;
            // TODO: handle exception
        }
    }
    public Integer primaryNewAuction(int userID, AuctionSaleItem item, String token) 
    {
        getAllReplicas();
        try {
            int itemID = s.newAuction(userID, item, token);
            System.out.println("Auction ITEM IS "+ s.auctionItems.get(itemID).itemID);
            for (int key : replicas.keySet()) {
                System.out.println(key);
                replicas.get(key).updateNewAuction(userID, item, itemID, s.auctionItems.get(itemID));
            }
            System.out.println("DONE");
            return itemID;
            
        } catch (Exception e) {
            return null;
            // TODO: handle exception
        }
    }
    public AuctionResult primaryCloseAuction(int userID, int itemID, String token)
    {
        getAllReplicas();
        try {
            AuctionResult auctionResult = s.closeAuction(userID, itemID, token);
            for (int key : replicas.keySet()) {
                replicas.get(key).updateCloseAuction(userID, itemID);
            }
            return auctionResult;
        } catch (Exception e) {
            return null;
            // TODO: handle exception
        }
    }
    public boolean primaryBid(int userID, int itemID, int price, String token)
    {
        getAllReplicas();
        try {
            System.out.println("BID");
            boolean result = s.bid(userID, itemID, price, token);
            for (int key : replicas.keySet()) {
                replicas.get(key).updateBid(userID, itemID, price);
            }
            System.out.println("Result BID "+result);
            return result;
        } catch (Exception e) {
            return false;
            // TODO: handle exception
        }
    }
    public int getState()
    {
        return state;
    }
    public void messageAllReplicas()
    {
        try {
            getAllReplicas();
            int replicaNumber = replicaID;
            System.out.println("REPLICA SIZE " + replicas.size());
            for (int key : replicas.keySet()) {
                System.out.println("REPLICA STATE" + replicas.get(key).getState());
                if(replicas.get(key).getState() > state)
                {
                    state = replicas.get(key).getState();
                    replicaNumber = key;
                }
            }
            System.out.println("repplica NUMBER" + replicaID);
            if(replicaNumber != replicaID)
            {
                System.out.println("REQUEST");;
                s.auctionItems = replicas.get(replicaNumber).getAuctionItems();
                s.primaryReplicaID = replicas.get(replicaNumber).getPrimaryReplicaID();
                s.users = replicas.get(replicaNumber).getUsers();
                s.winningEmailList = replicas.get(replicaNumber).getWinningEmailList();
                System.out.println("FINISH REQUEST");
                showItems();
            }
        }
        catch(Exception e)
        {
            
        }
    }
    public static void main(String[] args) {
        Replica replica = new Replica(Integer.parseInt(args[0]));
        try 
        {
            String name = "RepI" + args[0];
            ReplicaInterface stub = (ReplicaInterface) UnicastRemoteObject.exportObject(replica, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
            replica.messageAllReplicas();
        } 
        catch (Exception e) 
        {
            System.err.println(e);
        }
    }
}
