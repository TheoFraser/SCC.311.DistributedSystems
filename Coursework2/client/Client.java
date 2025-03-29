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
    
    // public static AuctionItem decrypt(SealedObject object)
    // {
    //     AuctionItem item = new AuctionItem();

    //     try{
    //         FileInputStream fileStream = new FileInputStream("..//keys/testKey.aes");
    //         ObjectInputStream objStream = new ObjectInputStream(fileStream);
    //         SecretKey key = (SecretKey) objStream.readObject();
    //         item = (AuctionItem) object.getObject(key);
    //         return item;
    //     }
    //     catch(Exception e){
    //         System.err.println("Error");
    //     }
    //     return null;
    // }
    public byte[] generateString(PrivateKey privateKey, String clientChallenge)
    {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedData = digest.digest(clientChallenge.getBytes(StandardCharsets.UTF_8));
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(hashedData);
            byte[] signedHash = signature.sign();
            return signedHash;
            // Cipher encryptCipher = Cipher.getInstance("RSA");
            // encryptCipher.init(Cipher.ENCRYPT_MODE, privateKey);
            // byte[] secretMessageBytes = clientChallenge.getBytes(StandardCharsets.UTF_8);
            // byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
            // System.out.println("CLIENT CHALLENGE:    " + clientChallenge);
            // return encryptedMessageBytes;
        }
        catch(Exception e)
        {

        }
        return null;
    }
    public String generateRandomString()
    {
        byte[] bytes = new byte[20];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        String s = new String(bytes, StandardCharsets.UTF_8);
        System.out.println("CHALLENGE RANDOM STRING:   " + s);
        return s;
    }
    public Boolean decrypt(ChallengeInfo challengeInfo, String randomString)
    {
        try {
            String filePath = "..//keys/serverKey.pub";
            PublicKey publicKey = readPublicKeyFromFile(filePath);
            // System.out.println(publicKey);
                // Now you can use the public key for encryption or other purposes

            // FileInputStream fileStream = new FileInputStream("..//keys/serverKey.pub");
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedData = digest.digest(randomString.getBytes((StandardCharsets.UTF_8)));
            signature.update(hashedData);
            // boolean isValid = signature.verify(signedHash);
            // int key = fileStream.read();
            // System.out.println(key);
            // byte[] decryptedMessageBytes = decryptCipher.doFinal(challengeInfo.response);
            // String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
            // String decryptedMessage = new String(challengeInfo.response, StandardCharsets.UTF_8);
            // System.out.println("AFTER DESCRIPTION RANDOM BYTES   " + decryptedMessage);
            if(signature.verify(challengeInfo.response))
            {
                return true;
            }
            return false;
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return null;
    }
    public PublicKey readPublicKeyFromFile(String filePath) throws Exception {
        // Read the Base64 encoded public key from the file
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get(filePath));
        String publicKeyBase64 = new String(publicKeyBytes);
    
        // Decode the Base64 encoded public key bytes
        byte[] decodedBytes = Base64.getDecoder().decode(publicKeyBase64);
    
        // Create a PublicKey object from the decoded bytes
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // You may need to change the algorithm based on your key type
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedBytes);
        //    RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
        //     new java.math.BigInteger(1, publicKeyBytes), // assuming it's an RSA key
        //     java.math.BigInteger.valueOf(65537) // assuming it's an RSA key with public exponent 65537
        // );
        return keyFactory.generatePublic(keySpec);
    }

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
        System.out.println("7. | -GENT   | GENERATE TOKEN | ");
    }
    
    public static void main(String[] args) {
        try 
        {
            Client client = new Client();
            String name = "Auction";
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction server = (Auction) registry.lookup(name);
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            PublicKey publicKey = pair.getPublic();
            PrivateKey privateKey = pair.getPrivate();
            Scanner options = new Scanner(System.in); 
            TokenInfo tokenInfo = new TokenInfo();
            String input = "";
            int userID = -1;
            while(userID == -1)
            {
                try
                {
                    System.out.print("PLEASE TYPE EMAIL:   ");
                    String email = options.nextLine();
                    userID = server.register(email, publicKey);
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
                String randomString = client.generateRandomString();
                ChallengeInfo challengeInfo = server.challenge(userID, randomString);
                if(client.decrypt(challengeInfo,randomString) == false)
                {
                    System.out.println("FAILED AUTHENTICATION KICKING USER OUT");
                    break;
                }
                tokenInfo = server.authenticate(userID, client.generateString(privateKey, challengeInfo.serverChallenge));
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
                    AuctionItem item = server.getSpec(userID,itemID, tokenInfo.token);
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
                    Integer itemID = server.newAuction(userID, saleItem,tokenInfo.token);
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
                    AuctionItem[] items = server.listItems(userID, tokenInfo.token);
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
                    AuctionResult result = server.closeAuction(userID, itemID, tokenInfo.token);
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
                    Boolean bid = server.bid(userID, itemID, bidAmount, tokenInfo.token);
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
                if(input.equals("-GENT"))
                {
                    randomString = client.generateRandomString();
                    challengeInfo = server.challenge(userID, randomString);
                    if(client.decrypt(challengeInfo,randomString) == false)
                    {
                        System.out.println("FAILED AUTHENTICATION KICKING USER OUT");
                        break;
                    }
                    tokenInfo = server.authenticate(userID, client.generateString(privateKey, challengeInfo.serverChallenge));
                }
            }
        }
        catch (Exception e) {
            System.err.println("Exception: server doesnt exist");
        }
    }
}