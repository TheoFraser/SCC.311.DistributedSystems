import java.lang.reflect.Array;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

public class User {
    String email;
    //HashMap<Integer,AuctionSaleItem> auctionSaleItems = new HashMap<Integer, AuctionSaleItem>();
    HashMap<Integer,AuctionSaleItem> auctionSaleItems = new HashMap<Integer,AuctionSaleItem>();
    PublicKey publicKey;
    // ArrayList<String> winningEmails = new ArrayList<String>();
    public User(String email, PublicKey publicKey)
    {
        this.email = email;
        this.publicKey = publicKey;
    }
}
