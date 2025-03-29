import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
public class Client
{
    
    public void showOptions()
    {
        // System.out.println("NUM| OPTIONS:| DESCRIPTION    | ");
        // System.out.println("1. | -REG    | REGISTER       | ");
        // System.out.println("2. | -SPEC   | GET SPEC       | ");
        // System.out.println("3. | -NA     | NEW AUCTION    | ");
        // System.out.println("4. | -LIST   | LIST ITEMS     | ");
        // System.out.println("5. | -CA     | CLOSE AUCTION  | ");
        // System.out.println("6. | -BID    | BID ON AUCTION | ");
        // System.out.println("7. | -HELP   | SHOW OPTIONS   | ");
        System.out.println("NUM| OPTIONS:| DESCRIPTION    | ");
        System.out.println("1. | -SPEC   | GET SPEC       | ");
        System.out.println("2. | -NEWA   | NEW AUCTION    | ");
        System.out.println("3. | -LIST   | LIST ITEMS     | ");
        System.out.println("4. | -CA     | CLOSE AUCTION  | ");
        System.out.println("5. | -BID    | BID ON AUCTION | ");
        System.out.println("6. | -HELP   | SHOW OPTIONS   | ");
        System.out.println("7. | -PR     | P-REPLICA ID   | ");
        System.out.println("8. | -GEN    | Generate       | ");
        System.out.println("9. | -CHAL   | CHALLANGE      | ");
    }
    
    public static void main(String[] args) {
        try 
        {
            Client client = new Client();
            String name = "FrontEnd";
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction server = (Auction) registry.lookup(name);
            Scanner options = new Scanner(System.in); 
            String input = "";
            int userID = -1;
            while(userID == -1)
            {
                try
                {
                    System.out.print("PLEASE TYPE EMAIL:   ");
                    String email = options.nextLine();
                    userID = server.register(email, null);
                    System.out.println("YOUR USER ID IS:       " + userID);
                }
                catch(Exception e)
                {
                    System.out.println("EMAIL ALREADY EXITS");
                }
            }
            client.showOptions();
            while(!input.equals("EXIT"))
            {
                System.out.print("PLEASE CHOOSE A OPTION   ");
                input = options.nextLine();
                // if(input.equals("-REG"))
                // {
                //     System.out.print("PLEASE TYPE EMAIL:   ");
                //     String email = options.nextLine();
                //     try
                //     {
                //         int x = server.register(email);
                //         System.out.println(x);
                //     }
                //     catch(Exception e)
                //     {
                //         System.out.println("EMAIL ALREADY EXITS");
                //     }
                //     System.out.println("");
                // }
                if(input.equals("-SPEC"))
                {
                    System.out.print("ENTER A ITEM ID:         ");
                    int itemID = options.nextInt();
                    AuctionItem item = server.getSpec(userID,itemID,"token");
                    if(item != null)
                    {
                        System.out.println(item.itemID);
                        System.out.println(item.name);
                        System.out.println(item.description);
                        System.out.println(item.highestBid);
                    }
                    if(item == null)
                    {
                        System.out.println("getSpec failed");
                    }
                }
                // // newAuction
                if(input.equals("-NEWA") )
                {
                    AuctionSaleItem saleItem = new AuctionSaleItem();
                    System.out.print("ENTER A NAME FOR YOUR ITEM:            ");
                    saleItem.name = options.nextLine();;
                    System.out.print("ENTER A DESCRIPTION FOR YOUR ITEM:     ");
                    saleItem.description = options.nextLine();
                    System.out.print("ENTER A RESERVE PRICE FOR YOUR ITEM:   ");
                    saleItem.reservePrice = options.nextInt();
                    // System.out.print("ENTER A YOUR USER ID:   ");
                    // int userID = options.nextInt();
                    Integer itemID = server.newAuction(userID, saleItem, null);
                    if(itemID != null)
                    {
                        System.out.println(itemID);
                    }
                    if(itemID == null)
                    {
                        System.out.println("newAuction failed");
                    }
                }
                // // listItems
                if(input.equals("-LIST"))
                {
                    AuctionItem[] items = server.listItems(userID, "LOL");
                    if(items != null)
                    {
                        System.out.println("ITEM ID | NAME | ITEM DESCRIPTION | HIGHEST BID");
                        for(int i =0; i< items.length;i++)
                        {
                            System.out.println(items[i].itemID + " | " + items[i].name + " | " + items[i].description + " | " + items[i].highestBid);
                        }
                    }
                    if(items == null)
                    {
                        System.out.println("listItems failed");
                    }
                }
                // // closeAuction
                if(input.equals("-CA"))
                {
                    // System.out.print("ENTER YOUR USER ID:   ");
                    // int userID = options.nextInt();
                    System.out.print("ENTER THE ITEM ID YOU WISH TO CLOSE:   ");
                    int itemID = options.nextInt();
                    AuctionResult result = server.closeAuction(userID, itemID, "LOL");
                    if(result != null)
                    {
                        if(result.winningEmail == null)
                        {
                            System.out.println("There is no winner");
                        }
                        else
                        {
                            System.out.println(result.winningEmail);
                        }
                    }
                    if(result == null)
                    {
                        System.out.println("closeAuction failed");
                    }
                }
                if(input.equals("-BID"))
                {
                    // System.out.print("ENTER YOUR USER ID:                  ");
                    // int userID = options.nextInt();
                    System.out.print("ENTER A ITEM ID YOU WISH TO BID ON:  ");
                    int itemID = options.nextInt();
                    System.out.print("ENTER HOW MUCH YOU WANT TO BID:      ");
                    int bidAmount = options.nextInt();
                    Boolean bid = server.bid(userID, itemID, bidAmount, "LOL");
                    if(bid == true)
                    {
                        System.out.println("bid was successful");
                    }
                    if(bid == false)
                    {
                        System.out.println("bid failed");
                    }
                }
                if(input.equals("-HELP"))
                {
                    client.showOptions();
                }
                if(input.equals("-PR"))
                {
                    int primaryReplicaID = server.getPrimaryReplicaID();
                    System.out.println("PRIMARY REPLICA ID IS " + primaryReplicaID);
                }
                if(input.equals("-CHAL"))
                {
                    ChallengeInfo challengeInfo = server.challenge(1,null);
                }
                if(input.equals("-GEN"))
                {
                    TokenInfo tokenInfo = server.authenticate(1,null);
                }
            }
        }
        catch (Exception e) {
            System.err.println("Exception: server doesnt exist");
        }
    }
}