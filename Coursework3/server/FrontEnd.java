import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;

public class FrontEnd implements Auction
{
    private Auction primaryReplica;
    private ReplicaInterface pReplicaInterface;
    boolean alive = false;
    public FrontEnd() 
    {
    }

    public void setUp()
    {
            try {
            // Assuming the RMI registry is running on localhost with the default port (1099)
            Registry registry = LocateRegistry.getRegistry("localhost", 0);

            // Get an array of names bound in the registry
            String[] bindingNames = registry.list();

            // Specify the prefix you're looking for
            String prefix = "Replica";
            // while(true)
            // {
                // Loop through the names and perform name lookup for those matching the prefix
                for (String bindingName : bindingNames) {
                    //System.out.println("Found: " + bindingName + " -> " );
                    if (bindingName.startsWith(prefix)) {
                        try {
                            // Perform name lookup for entries matching the prefix
                            Object obj = registry.lookup(bindingName);
                            String numberStr = bindingName.replaceAll("\\D+", "");
                            int replicaNumber = Integer.parseInt(numberStr);
                            System.out.println("Found: " + bindingName + " -> " + obj);
                            primaryReplica = (Auction) obj;
                            Object obj2 = registry.lookup("RepI"+replicaNumber);
                            pReplicaInterface = (ReplicaInterface) obj2;
                            //checkIfAlive(replicaNumber);
                            //this.alive = pReplicaInterface.checkIfAlive();
                            if(pReplicaInterface.checkIfAlive() == true)
                            {
                                System.out.println(pReplicaInterface.checkIfAlive());
                                System.out.println("Found: " + bindingName + " -> " );
                                pReplicaInterface.makePrimaryReplica(replicaNumber);
                                return;
                            }
                            else
                            {
                                continue;
                            }
                            // break;
                        } catch (Exception e) {
                            System.err.println("Error looking up " + bindingName + ": " + e.getMessage());
                            continue;
                            // this.alive = false;
                            //System.out.println(alive);
                            // Handle lookup exceptions
                        }
                    }
                }
            // }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("LMAO");
    }
    // // Implementation of Auction interface methods forwarding to the primary replica
    
    public int getPrimaryReplicaID() {
        try
        {
            setUp();
            int primaryReplicaID = primaryReplica.getPrimaryReplicaID();
            return primaryReplicaID;
        }
        catch(Exception e)
        {
            return 3;
        }
    }
    public TokenInfo authenticate(int userID, byte signature[])
    {
        try {
            setUp();
            TokenInfo tokenInfo = primaryReplica.authenticate(userID, signature);
            System.out.println("DONE");
            return null;
        } catch (Exception e) {
            return null;
            // TODO: handle exception
        }
    }
    public ChallengeInfo challenge(int userID, String challenge)
    {
        try {
            setUp();
            ChallengeInfo challengeInfo = primaryReplica.challenge(userID, challenge);
            System.out.println("DONE");
            return null;
        } catch (Exception e) {
            return null;
            // TODO: handle exception
        }
    }
    public Integer register(String email, PublicKey key) 
    {
        try
        {
            setUp();
            System.out.println("REGISTER");
            Integer userId = pReplicaInterface.primaryRegister(email, key);
            System.out.println("userID " + userId);
            return userId;
        }
        catch(Exception e)
        {
            System.out.println("FAIL");
            return null;
        }
    }
    public AuctionItem getSpec(int userID ,int itemID, String token)
    {
        try {
            setUp();
            AuctionItem auctionItem = primaryReplica.getSpec(userID,itemID, token);
            return auctionItem;
        } catch (Exception e) {
            return null;
            // TODO: handle exception
        }
    }
    public Integer newAuction(int userID, AuctionSaleItem item, String token)
    {
        try {
            setUp();
            Integer itemID = pReplicaInterface.primaryNewAuction(userID, item, token);
            System.out.println("NEW Auction " +itemID);
            return itemID;
        } catch (Exception e) {
            return null;
            // TODO: handle exception
        }
    }
    public AuctionItem[] listItems(int userID, String token)
    {
        try {
            setUp();
            AuctionItem[] auctionItems = primaryReplica.listItems(userID, token);
            System.out.println("ListItems");
            return auctionItems;
        } catch (Exception e) {
            return null;
            // TODO: handle exception
        }
    }
    public AuctionResult closeAuction(int userID, int itemID, String token)
    {
        try {
            setUp();
            AuctionResult auctionResult = pReplicaInterface.primaryCloseAuction(userID, itemID, token);
            System.out.println("Close Auction  " + auctionResult.winningPrice);
            return auctionResult;
        } catch (Exception e) {
            return null;
            // TODO: handle exception
        }
    }
    public boolean bid(int userID, int itemID, int price, String token) 
    {
        try {
            setUp();
            Boolean bidBoolean = pReplicaInterface.primaryBid(userID, itemID, price, token);
            System.out.println("BID  " + bidBoolean);
            return bidBoolean;
        } catch (Exception e) {
            return false;
            // TODO: handle exception
        }
    }
    

    public static void main(String[] args) {
        FrontEnd frontEnd = new FrontEnd();
        try 
        {
            String name = "FrontEnd";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(frontEnd, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            frontEnd.setUp();
            System.out.println("Server ready");
            // Get an array of names bound in the registry
        } 
        catch (Exception e) 
        {
            System.err.println(e);
            // System.err.println("Exception:");
        }
        
    }
}