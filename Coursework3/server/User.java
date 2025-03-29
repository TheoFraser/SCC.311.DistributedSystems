import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable 
{
    String email;
    //HashMap<Integer,AuctionSaleItem> auctionSaleItems = new HashMap<Integer, AuctionSaleItem>();
    HashMap<Integer,AuctionSaleItem> auctionSaleItems = new HashMap<Integer,AuctionSaleItem>();
    // ArrayList<String> winningEmails = new ArrayList<String>();
    public User(String email)
    {
        this.email = email;
    }
}
