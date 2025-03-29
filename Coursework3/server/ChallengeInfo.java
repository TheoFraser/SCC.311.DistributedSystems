public class ChallengeInfo implements java.io.Serializable
{
    byte [] response; // server’s response (signature) to client’s challenge
    String serverChallenge; // server’s challenge to the client
}