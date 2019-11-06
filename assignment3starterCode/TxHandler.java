import java.util.*;
public class TxHandler {
    private UTXOPool utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
    }

    public UTXOPool getUTXOPool () {
      return utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        List<Transaction.Output> transactionOutputs = tx.getOutputs();
        List<UTXO> allUTXO = utxoPool.getAllUTXO();
        Set<UTXO> claimedUTXO = new HashSet<>();
        double inputSum = 0;
        double outputSum = 0;
        for (Transaction.Output transactionOutput : transactionOutputs)
        {
          //Condition 4
          if (transactionOutput.value < 0)
          {
            return false;
          }
          outputSum += transactionOutput.value;
        }
        //Condition 2
        List<Transaction.Input> transactionInputs = tx.getInputs();
        for (int i=0; i < transactionInputs.size(); i++)
        {
          Transaction.Input transactionInput = transactionInputs.get(i);
          UTXO utxoInput = new UTXO(transactionInput.prevTxHash, transactionInput.outputIndex);
          if (!utxoPool.contains(utxoInput))
          {
            return false;
          }
          //Condition 3
          if (!claimedUTXO.add(utxoInput))
          {
            return false;
          }
          Transaction.Output claimedOutput = utxoPool.getTxOutput(utxoInput);
          if (!Crypto.verifySignature(claimedOutput.address, tx.getRawDataToSign(i), transactionInput.signature))
          {
            return false;
          }
          if (claimedOutput.value < 0)
          {
            return false;
          }
          inputSum += claimedOutput.value;
        }

        if (inputSum < outputSum)
        {
          return false;
        }
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        List<Transaction> transactions = new ArrayList<>(possibleTxs.length);
        for (Transaction possibleTransaction : possibleTxs)
        {
          if (isValidTx(possibleTransaction))
          {
            transactions.add(possibleTransaction);

            List<Transaction.Output> outputs = possibleTransaction.getOutputs();
            for (int i=0; i < outputs.size(); i++)
            {
              UTXO utxo = new UTXO(possibleTransaction.getHash(), i);
              utxoPool.addUTXO(utxo, outputs.get(i));
            }

            List<Transaction.Input> inputs = possibleTransaction.getInputs();
            for (Transaction.Input input : inputs)
            {
              UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
              utxoPool.removeUTXO(utxo);
            }

          }
        }
        Transaction[] result = new Transaction[transactions.size()];
        transactions.toArray(result);
        return result;
    }

}
