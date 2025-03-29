import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.util.ArrayList;

public class Server implements Auction{
    ArrayList<AuctionItem> auctionList;
    private SecretKey key;
    private Cipher cipher;

    /**
     * Create an arraylist for type AuctionItem and call create key. Then it will enter a try catch and call encryptMessage(). After the try catch will call the function addItem()
     * and add two items
     */
    public Server() {
        super();
        auctionList = new ArrayList<AuctionItem>();
        createKey();
        try{
            encryptMessage();
        }
        catch(Exception e){
            System.out.println("No such Algorithm");
        }
        addItem(0, "Cheese", "This is Cheese", 10);
        addItem(1, "Ham", "This is ham", 20);
    }
    /**
     * It will enter a try statement then read the file tesKeys.aes then create a cipher with the instance of "AES" and then read the SecretKey
     * in testKey.aes. Then the cipher will call init with the parameters Cipher.ENCRYPT_MODE and key.
     * 
     */
    public void encryptMessage() 
    {
        try{
            FileInputStream fileStream = new FileInputStream("..//keys/testKey.aes");
            ObjectInputStream objIStream = new ObjectInputStream(fileStream);
            cipher = Cipher.getInstance("AES");
            key = (SecretKey) objIStream.readObject();
            cipher.init(Cipher.ENCRYPT_MODE, key);
            objIStream.close();
        }
        catch(Exception e)
        {
            System.out.println("ERR");
        }
    }
    /**
     * This function creates a file called testKey.aes and then creates a SecureRandom called secRandom and KeyGenerator called keyGen with the instance of ("AES"). 
     * Then I call keyGen.init(secRandom) and set SecretKey key to keyGen.generateKey() then I write key to teskKey.aes then close the ObjectOutputStream.
     */
    public void createKey()
    {
        try{
            FileOutputStream file = new FileOutputStream("..//keys/testKey.aes");
            ObjectOutputStream objOStream = new ObjectOutputStream(file);
            SecureRandom secRandom = new SecureRandom();
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(secRandom);
            key = keyGen.generateKey();
            objOStream.writeObject(key);
            objOStream.close();

        }
        catch(Exception e){
            System.out.println("ERROR");
        }
    }
    /**
     * Firstly it creates a AuctionItem called item and puts itemID, itemName, description and highestBid into item
     * Then it adds item to an arraylist of AuctionItems at position itemId.
     * 
     * @param itemID the ID of the item this function is creating
     * @param itemName the Name of the item this function is creating
     * @param description the desciption of the item this function is creating
     * @param highestBid the highestBid of the item this function is creating
     */
    public void addItem(int itemID, String itemName, String description, int highestBid)
    {
        AuctionItem item = new AuctionItem();
        item.itemID = itemID;
        item.name = itemName;
        item.description = description;
        item.highestBid = highestBid;
        auctionList.add(itemID, item);
    }
    /**
     * This function enters a for loop which increments i from 0 to auctionList.size(). Then if auctionList.get(i) itemID is equal to the requested 
     * itemID it will enter a try catch. In the try part it will return a SealedObject with the Object as AuctionItem at position i in the arraylist auctionList
     * and the Cipher cipher in the catch part it will return null. Then outside the for loop the function returns null
     * 
     * @param itemID the ID of the AuctionItem the client is searching for
     */
    public SealedObject getSpec(int itemID) {
        System.out.println("client request handled");
        for(int i=0; i< auctionList.size(); i ++)
        {
            if(auctionList.get(i).itemID == itemID)
            {
                try
                {
                    return new SealedObject(auctionList.get(i), cipher);
                }
                catch(Exception e)
                {
                    return null;
                }
            }
        }
        return(null);
    }
    /**
     * This creates an instance of Server called s then creates a new String called name with value Auction. 
     * Then it exports s as a remote object and retuns a stub object of type Auction. Then it gets a reference to the RMI registry 
     * called registry. Then it binds name and stub in the RMI registry.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "Auction";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println(e);
            // System.err.println("Exception:");
        }
    }
}
