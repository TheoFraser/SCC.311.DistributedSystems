import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.PrintWriter;
import java.rmi.RemoteException;

public class ClientTest {

     public static void main(String[] args)
        {
        Registry registry = null;
        String name = "FrontEnd";
        
        try {
            registry = LocateRegistry.getRegistry("localhost");
            }
            catch (Exception e)
            {
            System.out.println("0:fail:RMI registry instance not found by LocateRegistry()");
            return;
            }
        
        System.out.println("0:pass:RMI registry found");
        boolean found = false;
        Auction frontEnd = null;
        try {
            String lst[] = registry.list();
            frontEnd = (Auction) registry.lookup(name);
            
            }
            catch (Exception e)
            {
            e.printStackTrace();
            System.out.println("1:fail:no RMI service found by the name: " + name);
            return;
            }
        
        System.out.println("1:pass:RMI service found with correct name: " + name);
        
        try {
            
            int replicaID = frontEnd.getPrimaryReplicaID();
            System.out.println("2:pass:Auction interface matches specification");
            
            }
            catch (Exception e) {
            System.out.println("2:fail – Auction interface does not match the specification");
            e.printStackTrace();
            return;
            }
        }
}
