import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
public class Client{

    public static AuctionItem decrypt(SealedObject object)
    {
        AuctionItem item = new AuctionItem();

        try{
            FileInputStream fileStream = new FileInputStream("..//keys/testKey.aes");
            ObjectInputStream objStream = new ObjectInputStream(fileStream);
            SecretKey key = (SecretKey) objStream.readObject();
            item = (AuctionItem) object.getObject(key);
            return item;
        }
        catch(Exception e){
            System.err.println("Error");
        }
        return null;
    }
    
    public static void main(String[] args) {
    if (args.length < 1 ) {
        System.out.println("Please give a autionID");
        return;
    }
    int itemID = Integer.parseInt(args[0]);
    try {
            String name = "Auction";
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction server = (Auction) registry.lookup(name);
            SealedObject sealed = server.getSpec(itemID);
            if(sealed != null)
            {
                AuctionItem item = decrypt(sealed);
                System.out.println(item.name);
                System.out.println(item.itemID);
                System.out.println(item.description);
                System.out.println(item.highestBid);
            }
            if(sealed == null)
            {
                System.err.println("Item doesnt exits");
            }
    }
    catch (Exception e) {
        System.err.println("Exception: server doesnt exist");
    }
    }
}