package org.tron.common.utils;

import static org.tron.core.Constant.TRANSACTION_MAX_BYTE_SIZE;

import org.tron.core.ChainBaseManager;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.store.DynamicPropertiesStore;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract;
import org.tron.protos.contract.AssetIssueContractOuterClass.TransferAssetContract;
import org.tron.protos.contract.BalanceContract.TransferContract;

public class TxUtil {
  public static boolean contractCreateNewAccount(Contract contract) {
    AccountCapsule toAccount;
    switch (contract.getType()) {
      case AccountCreateContract:
        return true;
      case TransferContract:
        TransferContract transferContract;
        try {
          transferContract = contract.getParameter().unpack(TransferContract.class);
        } catch (Exception ex) {
          throw new RuntimeException(ex.getMessage());
        }
        toAccount = ChainBaseManager.getChainBaseManager().getAccountStore()
            .get(transferContract.getToAddress().toByteArray());
        return toAccount == null;
      case TransferAssetContract:
        TransferAssetContract transferAssetContract;
        try {
          transferAssetContract = contract.getParameter().unpack(TransferAssetContract.class);
        } catch (Exception ex) {
          throw new RuntimeException(ex.getMessage());
        }
        toAccount = ChainBaseManager.getChainBaseManager().getAccountStore()
            .get(transferAssetContract.getToAddress().toByteArray());
        return toAccount == null;
      default:
        return false;
    }
  }

  public static boolean isTooBigTransactionSize(Transaction tx) {
    DynamicPropertiesStore propertiesStore = ChainBaseManager.getChainBaseManager()
        .getDynamicPropertiesStore();
    boolean allowMaxCreateAccountTxSize = propertiesStore.allowMaxCreateAccountTxSize();
    if (allowMaxCreateAccountTxSize) {
      long maxCreateAccountTxSize = propertiesStore.getMaxCreateAccountTxSize();
      Contract contract = tx.getRawData().getContract(0);
      long createAccountBytesSize = tx.toBuilder().clearSignature().build().getSerializedSize();
      long generalBytesSize = tx.getSerializedSize();
      return generalBytesSize > TRANSACTION_MAX_BYTE_SIZE
          || (createAccountBytesSize > maxCreateAccountTxSize && contractCreateNewAccount(contract));
    }
    return false;
  }
}
