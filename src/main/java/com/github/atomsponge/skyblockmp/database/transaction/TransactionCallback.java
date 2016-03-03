package com.github.atomsponge.skyblockmp.database.transaction;

/**
 * @author AtomSponge
 */
public abstract class TransactionCallback {
    public void success() {
    }

    public void failure(Throwable throwable) {
    }
}
