// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory
// as it would cause a memory overflow.
import java.util.*;
import java.sql.Timestamp;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private TransactionPool txPool = new TransactionPool();
    private BlockInfo maxHeightBlockInfo;
    private List<BlockInfo> blockInfos = new ArrayList<>();

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS

        // handle coinbase transaction
        if (genesisBlock == null) return;
        UTXOPool utxoPool = new UTXOPool();
        Transaction coinbase = genesisBlock.getCoinbase();
        txPool.addTransaction(coinbase);
        for (int i=0; i < coinbase.numOutputs(); i++) {
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            utxoPool.addUTXO(utxo, coinbase.getOutput(i));
        }

        ArrayList<Transaction> transactions = genesisBlock.getTransactions();
        if (transactions != null) {
            for (Transaction transaction : transactions) {
                if (transaction != null) {
                    txPool.addTransaction(transaction);
                    for (int i=0; i < transaction.numOutputs(); i++) {
                        UTXO utxo = new UTXO(transaction.getHash(), i);
                        utxoPool.addUTXO(utxo, transaction.getOutput(i));
                    }
                }
            }
        }

        BlockInfo blockInfo = new BlockInfo(genesisBlock, 1, utxoPool);
        blockInfos.add(blockInfo);
        maxHeightBlockInfo = blockInfo;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return maxHeightBlockInfo == null ? null : maxHeightBlockInfo.getBlock();
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return maxHeightBlockInfo == null ? null : maxHeightBlockInfo.getUTXOPool();
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     *
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     *
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        if (block == null || block.getPrevBlockHash() == null) {
            System.out.println ("BlockChain null block or null previous block hash");
            return false;
        }

        BlockInfo parentBlockInfo = getParentBlock(block.getPrevBlockHash());
        if (parentBlockInfo == null) {
            System.out.println ("BlockChain null parent block info");
            return false;
        }

        int height = parentBlockInfo.getHeight()+1;
        if (height <= (maxHeightBlockInfo.getHeight() - CUT_OFF_AGE)) {
            System.out.println ("BlockChain Height is greater than max height");
            return false;
        }

        UTXOPool utxoPool = new UTXOPool(parentBlockInfo.getUTXOPool());
        Transaction coinbase = block.getCoinbase();
        txPool.addTransaction(coinbase);

        TxHandler txHandler = new TxHandler(utxoPool);
        Transaction[] possibleTxs = new Transaction[block.getTransactions().size()];
        possibleTxs = block.getTransactions().toArray(possibleTxs);
        Transaction[] handledTxs = txHandler.handleTxs(possibleTxs);

        utxoPool = new UTXOPool(txHandler.getUTXOPool());

        for (int i=0; i < coinbase.numOutputs(); i++) {
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            utxoPool.addUTXO(utxo, coinbase.getOutput(i));
        }

        if (block.getTransactions() != null) {
            for (Transaction transaction : block.getTransactions()) {
                txPool.removeTransaction(transaction.getHash());
            }
        }

        BlockInfo newBlockInfo = new BlockInfo(block , height, utxoPool);
        if (maxHeightBlockInfo == null) {
            maxHeightBlockInfo = newBlockInfo;
        }
        else {
            if (height >= maxHeightBlockInfo.getHeight()) {
                maxHeightBlockInfo = newBlockInfo;
            }
        }
        blockInfos.add(newBlockInfo);

        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        txPool.addTransaction(tx);
    }

    private BlockInfo getParentBlock (byte[] txHash) {
        if (txHash == null || blockInfos == null) return null;
        for (BlockInfo blockInfo : blockInfos) {
            ByteArrayWrapper byteArrayWrapper = new ByteArrayWrapper (blockInfo.getBlock().getHash());
            ByteArrayWrapper other = new ByteArrayWrapper (txHash);
            if (byteArrayWrapper.equals(other)) {
                return blockInfo;
            }
        }
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Block Chain Max height: ").append (maxHeightBlockInfo.toString()).append("\n").append("Blocks:");
        for (BlockInfo blockInfo : blockInfos) {
            sb.append("\n").append(blockInfo.toString());
        }
        return sb.toString();
    }

    public class BlockInfo {
      private Block block;
      private int height;
      private UTXOPool utxoPool;
      private Timestamp timestamp;

      public BlockInfo(Block block, int height, UTXOPool utxoPool) {
          this.block = block;
          this.height = height;
          this.utxoPool = utxoPool;
          this.timestamp = new Timestamp(System.currentTimeMillis());
      }

      public Block getBlock () {
          return block;
      }

      public int getHeight() {
          return height;
      }

      public UTXOPool getUTXOPool() {
          return utxoPool;
      }

      public Timestamp getTimestamp() {
        return timestamp;
      }

      public String toString() {
          return "Block: " + block.toString() + " Height: " + height + " UTXOPool: " + utxoPool.toString() + " Timestamp: " + timestamp;
      }
    }
}
