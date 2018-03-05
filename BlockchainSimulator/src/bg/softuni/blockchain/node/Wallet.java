package bg.softuni.blockchain.node;

import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.HexEncoder;

import bg.softuni.blockchain.json.Block;
import bg.softuni.blockchain.json.BlockChain;
import bg.softuni.blockchain.json.Transaction;
import bg.softuni.blockchain.peers.Node;
import bg.softuni.blockchain.utils.BIP39;
import bg.softuni.blockchain.utils.Base58;

public class Wallet {

	//private Node owner;
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	private int lastBlockIndexWhenBalanceWasUpdated = 0;
	
	private int confirmedBalance; //6 > confirmations
	private int pendingBalance;
	
	private String address;
	private String seed;
	
	private List<Transaction> transactionHistory;
	
	
	protected Wallet(){
		this.transactionHistory = new ArrayList<Transaction>();
		Security.addProvider(new BouncyCastleProvider());
		transactionHistory = new ArrayList<Transaction>();
	}
	
	
	public static Wallet createWallet(){
		String seed = generateRandomSeed(24);
	
		Wallet wallet = new Wallet();
		try {
			wallet.generateKeyPair(seed);
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			wallet.generateAddress();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wallet.seed = seed;
		return wallet;
	}
	
	public static Wallet restoreWallet(BlockChain blockchain, String seed){
		Wallet wallet = new Wallet();
		try {
			wallet.generateKeyPair(seed);
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			wallet.generateAddress();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Check balance in blockchain
		wallet.seed = seed;
		wallet.syncWallet(blockchain);
		return wallet;
	}
	
	
	private static String generateRandomSeed(int wordsCount){
		String seed = "";
		for (int i = 0; i < wordsCount; i++) {
			seed += BIP39.getRandom() + " ";
		}
		return seed.trim();
	}
	
	private void generateKeyPair(String seed) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException{
		ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("secp256k1");
		KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
		SecureRandom rand = new SecureRandom(seed.getBytes());
		g.initialize(ecGenSpec, rand);
		KeyPair pair = g.generateKeyPair();
		this.privateKey = pair.getPrivate();
		this.publicKey = pair.getPublic();
	}
	
	
	public void generateAddress() throws NoSuchAlgorithmException{
		MessageDigest sha256 = new SHA256.Digest();
		byte[] hashedPubKey = sha256.digest(this.publicKey.getEncoded());
		MessageDigest ripeMd160Digest = new RIPEMD160.Digest();
		byte[] ripeMd = ripeMd160Digest.digest(hashedPubKey);
		//Add network
		byte[] hashWithNetwork = new byte[ripeMd.length+1];
		hashWithNetwork = Arrays.concatenate(new byte[]{0}, ripeMd);
		byte[] pubHash = sha256.digest(sha256.digest(hashWithNetwork));
		//Append checksum		
		byte[] address = new byte[hashWithNetwork.length + 4];	
		address = Arrays.concatenate(hashWithNetwork, Arrays.copyOfRange(pubHash, pubHash.length-5, pubHash.length-1));

		this.address = Base58.encodeBase58(address);
	}
	
	
	public String getAddress(){
		return this.address;
	}

	
	public void syncWallet(BlockChain currentblockChain){
		this.pendingBalance = 0;
		this.confirmedBalance = 0;
		List<Block> blockChain = currentblockChain.getBlockChain();
		for (int i = 0; i < blockChain.size(); i++) {
			Block block = blockChain.get(i);
			List<Transaction> transactions = block.getTransactions();
			for (Iterator iterator2 = transactions.iterator(); iterator2.hasNext();) {
				Transaction transaction = (Transaction) iterator2.next();
				if(transaction.isPaid()){
					
					if(transaction.getAddressFrom().equals(this.address) ||
							transaction.getAddressTo().equals(this.address)){
						int confirmations = blockChain.size() - block.getIndex();
						
						transaction.setConfirmations(confirmations);
						this.transactionHistory.add(transaction);
					}
					//System.out.println(transaction.getAddressTo());
					if(transaction.getAddressFrom().equals(this.address)){
						//owner sent money
						this.pendingBalance -= transaction.getAmount();
						this.pendingBalance -= transaction.getFee();
						
						if(transaction.getConfirmations() >= 6){
							//Transaction has 6 or more confirmations
							this.confirmedBalance -= transaction.getAmount();
							this.confirmedBalance -= transaction.getFee();
							this.pendingBalance += transaction.getAmount();
							this.pendingBalance += transaction.getFee();
						}
					}
					if(transaction.getAddressTo().equals(this.address)){
						//owner recieved money
						this.pendingBalance += transaction.getAmount();
						if(transaction.getConfirmations() >= 6){
							//Transaction has 6 or more confirmations
							this.confirmedBalance += transaction.getAmount();
							this.pendingBalance -= transaction.getAmount();;
						}
					}
				}
			}
		}
		this.lastBlockIndexWhenBalanceWasUpdated = blockChain.size() - 1;
	}
	
	public int getPendingBalance(){
		return this.pendingBalance;
	}
	
	public int getConfirmedBalance(){
		return this.confirmedBalance;
	}
	
	
	public Transaction sign(String addressTo, int amount, int fee){
		long currentTime = System.currentTimeMillis();
		
		MessageDigest sha256 = new SHA256.Digest();
		byte[] transactionHashBytes = sha256.digest(
				(this.address + addressTo + String.valueOf(amount) + String.valueOf(fee) + String.valueOf(currentTime)).getBytes());
		String transactionHash = Hex.toHexString(transactionHashBytes);
		
		ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("SECP256K1");
	    ECDomainParameters domain = new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN());
		ECDSASigner signer = new ECDSASigner();
		BigInteger pk = new BigInteger(privateKey.getEncoded());
		signer.init(true, new ECPrivateKeyParameters(pk, domain));
		BigInteger[] signature = signer.generateSignature(transactionHashBytes);
		
		String signature1 = Hex.toHexString(signature[0].toByteArray());
		String signature2 = Hex.toHexString(signature[1].toByteArray());
		String[] signatureArray = new String[]{signature1, signature2};
		
		Transaction transaction = new Transaction();
		transaction.setAddressFrom(this.address);
		transaction.setAddressTo(addressTo);
		transaction.setAmount(amount);
		transaction.setFee(fee);
		transaction.setCreatedTimestamp(currentTime);
		transaction.setSenderPublicKey(Hex.toHexString(this.publicKey.getEncoded()));
		transaction.setSenderSigniture(signatureArray);
		transaction.setTransactionHash(transactionHash);

		return transaction;
	}


	public boolean verify(Transaction trx){
		return true;
	}
	
	public String getSeed() {
		return seed;
	}


	public List<Transaction> getTransactionHistory() {
		return transactionHistory;
	}



	
}
