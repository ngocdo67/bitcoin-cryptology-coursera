import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;


public class TestBlockChain {
    public static void main (String[] args) throws Exception {
        byte[] prevHash = "sample prev hash".getBytes();
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize (512);
        keyGen.generateKeyPair();
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        Block genesisBlock = new Block (null, publicKey);
        Transaction coinbaseTransaction = new Transaction (10.0, publicKey);
        genesisBlock.addTransaction(coinbaseTransaction);
        BlockChain blockChain = new BlockChain(genesisBlock);

        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block addedBlock = blockHandler.createBlock(publicKey);
        System.out.println ("Test blockchain: " + blockChain.toString());
        if (addedBlock == null)
            System.out.println ("Test blockchain: null");
        else
        System.out.println ("Test blockchain: " + addedBlock.toString());
    }
}