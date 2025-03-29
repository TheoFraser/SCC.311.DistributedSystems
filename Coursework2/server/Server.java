import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Base64;
import java.rmi.RemoteException;

public class Server implements Auction{
    HashMap<Integer,AuctionItem> auctionItems = new HashMap<Integer,AuctionItem>();
    HashMap<Integer,String> winningEmailList = new HashMap<Integer,String>();
    HashMap<Integer,User> users = new HashMap<Integer,User>();
    HashMap<Integer, PublicKey> userKeys = new HashMap<Integer, PublicKey>();
    HashMap<Integer, String> challenges = new HashMap<Integer, String>();
    HashMap<Integer, TokenInfo> tokens = new HashMap<Integer, TokenInfo>();
    int userID = 0;
    PrivateKey privateKey ;
    public Server() {
        // Call the superclass constructor
        super();
        try {
            // Generate an RSA key pair using a KeyPairGenerator
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            
            // Initialize the key pair generator with a key size of 2048 bits
            generator.initialize(2048);
            
            // Generate the key pair
            KeyPair pair = generator.generateKeyPair();
            
            // Get the public key from the generated key pair
            PublicKey publicKey = pair.getPublic();
            
            // Get the private key from the generated key pair (not shown in the code snippet)
            privateKey = pair.getPrivate();
            
            // Store the public key in a file, passing the key and the file path
            storePublicKey(publicKey, "..//keys/serverKey.pub");
        } catch (Exception e) 
        {
            // Handle any exceptions that might occur during key generation or storage
        }
    }

    // Method to write a public key to a file.
// Example use: storePublicKey(aPublicKey, ‘../keys/serverKey.pub’)
    public void storePublicKey(PublicKey publicKey, String filePath) throws Exception {
        // Convert the public key to a byte array
        byte[] publicKeyBytes = publicKey.getEncoded();
        // Encode the public key bytes as Base64
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);
         // Write the Base64 encoded public key to a file
        try (FileOutputStream fos = new FileOutputStream(filePath)) 
        {
            fos.write(publicKeyBase64.getBytes());
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
    public void addAuctionItem(int itemID, String itemName, String description, int highestBid) {
        // Create a new AuctionItem object
        AuctionItem item = new AuctionItem();
        
        // Set the properties of the AuctionItem using the provided parameters
        item.itemID = itemID;
        item.name = itemName;
        item.description = description;
        item.highestBid = highestBid;
        
        // Add the AuctionItem to the hashmap called auctionItems using its itemID as the key
        auctionItems.put(itemID, item);
    }
    // Method to handle a challenge for a given user
    public ChallengeInfo challenge(int userID, String clientChallenge) 
    {
        // Create a new ChallengeInfo object to store challenge-related information
        ChallengeInfo challengeInfo = new ChallengeInfo();
        
        // Generate a random server challenge using SecureRandom
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        String serverChallenge = new String(bytes, StandardCharsets.UTF_8);
        
        //System.out.println("CLIENT CHALLENGE BEFORE:    " + clientChallenge);
        
        // make challengeInfo.clientChallenge equal serverChallenge and put server challenge in the hashmap challenges with the key userID
        challengeInfo.serverChallenge = serverChallenge;
        challenges.put(userID, serverChallenge);
        
        try {
            // Compute the hash of the client challenge using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedData = digest.digest(clientChallenge.getBytes(StandardCharsets.UTF_8));
            
            // Sign the hashed client challenge using RSA with the server's private key
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(hashedData);
            byte[] signedHash = signature.sign();
            
            // Make challengeInfo.response equal to signedHash and then return challengeInfo
            challengeInfo.response = signedHash;
            return challengeInfo;
        } catch (Exception e) {
            // Handle any exceptions that might occur during the challenge process
        }
        
        // Return null in case of an exception or failure
        return null;
    }
    // Method to authenticate a user based on a provided signature
    public TokenInfo authenticate(int userID, byte signature[]) {
        try {
            // Create a Signature object for verification using RSA with SHA-256
            Signature signatureS = Signature.getInstance("SHA256withRSA");
            
            // Initialize the signature object with the user's public key
            signatureS.initVerify(userKeys.get(userID));
            
            // Compute the hash of the original challenge associated with the user
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedData = digest.digest(challenges.get(userID).getBytes(StandardCharsets.UTF_8));
            
            // Update the signature object with the hashed data
            signatureS.update(hashedData);
            
            // Verify the provided signature against the computed hash
            if (signatureS.verify(signature)) {
                // If the signature is valid, remove the challenge for the user
                
                challenges.remove(userID);
                
                // Generate a random string to be used as a token
                byte[] array = new byte[7]; // length is bounded by 7
                new Random().nextBytes(array);
                String generatedString = new String(array, Charset.forName("UTF-8"));
                
                // Create a new TokenInfo object and set its properties
                TokenInfo tokenInfo = new TokenInfo();
                tokenInfo.expiryTime = System.currentTimeMillis() / 1000L + 5; // Token expiry time (5 seconds from the current time)
                tokenInfo.token = generatedString;
                
                // Store the token information associated with the user
                tokens.put(userID, tokenInfo);
                
                // Return the generated TokenInfo object
                return tokenInfo;
            }
        } catch (Exception e) {
            // Handle any exceptions that might occur during the authentication process
        }

        // Return null in case of an exception or authentication failure
        return null;
    }
    // Method to register a new user with the given email and public key
    public synchronized Integer register(String email, PublicKey pubKey) {
        // Variable to track the size of the users collection
        int userSize = 0;

        // Output the current size of the users collection (for debugging or logging purposes)
        System.out.println(users.size());

        // Check if the users collection is not empty
        if (users.size() != 0) {
            // Iterate through the users collection to find an available user ID
            for (int i = 0; i < users.size(); i++) {
                // If an available user ID is found, break out of the loop
                if (!users.containsKey(i)) {
                    break;
                }

                // Check if the email already exists for another user
                if (users.get(i).email.equals(email)) {
                    // Output a message indicating that the email already exists
                    System.out.println(email + " NAME ALREADY EXISTS");

                    // Return null to indicate registration failure
                    return null;
                }

                // Update the userSize variable to reflect the current size of the users collection
                userSize = i + 1;
            }
        }

        // Create a new User object and add it to the users collection with the unique user ID
        users.put(userSize, new User(email, pubKey));

        // Also, store the user's public key in the userKeys collection
        userKeys.put(userSize, pubKey);

        // Return the unique user ID assigned during registration
        return userSize;
    }
    /**
     * This function enters a for loop which increments i from 0 to auctionList.size(). Then if auctionList.get(i) itemID is equal to the requested 
     * itemID it will enter a try catch. In the try part it will return a SealedObject with the Object as AuctionItem at position i in the arraylist auctionList
     * and the Cipher cipher in the catch part it will return null. Then outside the for loop the function returns null
     * 
     * @param itemID the ID of the AuctionItem the client is searching for
     */
    // Method to retrieve information about a specific auction item for a given user
    public AuctionItem getSpec(int userID, int itemID, String token) {
        // Check if the provided token is valid and has not expired
        if (!token.equals(tokens.get(userID).token) && tokens.get(userID).expiryTime < System.currentTimeMillis() / 1000L) {
            // If the token is invalid or expired, return null to indicate access denied
            return null;
        }

        // Remove the token after successful validation
        tokens.remove(userID);

        // Output a message indicating that the client request has been handled
        System.out.println("Client request handled");

        // Iterate through the auctionItems collection to find the requested item
        for (int i = 0; i < auctionItems.size(); i++) {
            // Check if the current auction item has the specified item ID
            if (auctionItems.get(i).itemID == itemID) {
                // Return the found auction item
                return auctionItems.get(i);
            }
        }

        // If the specified item ID is not found, return null
        return null;
    }
    // Method to create a new auction item and associate it with a user
    public  Integer newAuction(int userID, AuctionSaleItem item, String token) {
        // Check if the provided token is valid and has not expired
        if (!token.equals(tokens.get(userID).token) && tokens.get(userID).expiryTime < System.currentTimeMillis() / 1000L) {
            // If the token is invalid or expired, return null to indicate access denied
            return null;
        }

        // Remove the token after successful validation
        tokens.remove(userID);

        // Check if the user with the provided ID exists
        if (users.get(userID) == null) {
            // If the user does not exist, return null to indicate an invalid user
            return null;
        }

        // Variable to track the size of the auctionItems collection
        int auctionSize = 0;

        // Check if the auctionItems collection is not empty
        if (auctionItems.size() != 0) {
            // Iterate through the auctionItems collection to find an available auction ID
            for (int i = 0; i < auctionItems.size(); i++) {
                // If an available auction ID is found, break out of the loop
                if (!auctionItems.containsKey(i)) {
                    break;
                }

                // Update the auctionSize variable to reflect the current size of the auctionItems collection
                auctionSize = i + 1;
            }
        }

        // Add a new auction item to the auctionItems collection using the available auction ID
        addAuctionItem(auctionSize, item.name, item.description, 0);

        // Associate the auction item with the user by adding it to the user's auctionSaleItems collection
        users.get(userID).auctionSaleItems.put(auctionSize, item);

        // Initialize the winningEmailList entry for the new auction item to null
        winningEmailList.put(auctionSize, users.get(userID).email);

        // Return the itemID of the newly created auction item
        return auctionItems.get(auctionSize).itemID;
    }
    // Method to list all available auction items for a given user
    public  AuctionItem[] listItems(int userID, String token) {
        // Check if the provided token is valid and has not expired
        if (!token.equals(tokens.get(userID).token) && tokens.get(userID).expiryTime < System.currentTimeMillis() / 1000L) {
            // If the token is invalid or expired, return null to indicate access denied
            return null;
        }

        // Remove the token after successful validation
        tokens.remove(userID);

        // Check if there are any auction items available
        if (auctionItems.size() > 0) {
            // Create an array to hold the auction items
            AuctionItem[] itemArray = new AuctionItem[auctionItems.size()];

            // Convert the values of the auctionItems collection to an array
            itemArray = auctionItems.values().toArray(itemArray);

            // Return the array of auction items
            return itemArray;
        }

        // If there are no auction items available, return null
        return null;
    }
    // Method to close an auction for a specific user and item
    public synchronized AuctionResult closeAuction(int userID, int itemID, String token) {
        // Check if the provided token is valid and has not expired
        if (!token.equals(tokens.get(userID).token) && tokens.get(userID).expiryTime < System.currentTimeMillis() / 1000L) {
            // If the token is invalid or expired, return null to indicate access denied
            return null;
        }

        // Remove the token after successful validation
        tokens.remove(userID);

        // Check if the user has the specified auction item in their auctionSaleItems collection
        if (users.get(userID).auctionSaleItems.get(itemID) != null) {
            System.out.println("AUCTION RESULT");
            
            // Retrieve the reserve price for the auction item
            int reservePrice = users.get(userID).auctionSaleItems.get(itemID).reservePrice;
            System.out.println(reservePrice);
            
            // Create a new AuctionResult object to store the result of the auction
            AuctionResult result = new AuctionResult();
            result.winningEmail = winningEmailList.get(itemID);
            result.winningPrice = auctionItems.get(itemID).highestBid;
            
            // Remove the auction item and related information from the respective collections
            auctionItems.remove(itemID);
            winningEmailList.remove(itemID);
            users.get(userID).auctionSaleItems.remove(itemID);
            
            // Check if the winning bid is below the reserve price
            if (result.winningPrice < reservePrice) {
                // If the winning bid is below the reserve price, return null to indicate auction failure
                return null;
            }

            // Return the result of the auction
            return result;
        }

        // If the user does not have the specified auction item, return null
        return null;
    }
    // Method for a user to place a bid on a specific auction item
    public synchronized boolean bid(int userID, int itemID, int price, String token) 
    {
        // Check if the provided token is valid and has not expired
        if (!token.equals(tokens.get(userID).token) && tokens.get(userID).expiryTime < System.currentTimeMillis() / 1000L) {
            // If the token is invalid or expired, return false to indicate bid failure
            return false;
        }

        // Remove the token after successful validation
        tokens.remove(userID);

        // Check if the specified auction item exists
        if (auctionItems.get(itemID) != null) {
            // Check if the user does not already own the auction item
            if (!users.get(userID).auctionSaleItems.containsKey(itemID)) {
                // Compare the bid price with the current highest bid for the item
                if (auctionItems.get(itemID).highestBid > price) {
                    // If the bid price is lower than the current highest bid, return false to indicate bid failure
                    return false;
                }
                
                // Update the highest bid and winning email for the auction item
                auctionItems.get(itemID).highestBid = price;
                winningEmailList.put(itemID, users.get(userID).email);
                
                // Return true to indicate a successful bid
                return true;
            }
        }

        // If the specified auction item does not exist or the user already owns it, return false
        return false;
    }
    /**
     * This creates an instance of Server called s then creates a new String called name with value Auction. 
     * Then it exports s as a remote object and returns a stub object of type Auction. Then it gets a reference to the RMI registry 
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
