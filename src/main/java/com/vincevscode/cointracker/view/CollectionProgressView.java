// v0.7.0: View model summarizing a user's collection completion progress.
package com.vincevscode.cointracker.view;

import java.util.Objects;

/**
 * Response shape for {@code GET /api/users/{userId}/progress}, assembled by
 * {@link com.vincevscode.cointracker.service.CollectionTrackingService#getCollectionProgress}
 * from two COUNT(*) queries rather than loading every entry — {@code totalCoinsInCatalog} is
 * derived (owned + missing), not queried directly.
 */
public class CollectionProgressView {
    private int userId;
    private long totalCoinsInCatalog;
    private long ownedCoinCount;
    private long missingCoinCount;
    private double percentageComplete;

    public CollectionProgressView(
            int userId,
            long totalCoinsInCatalog,
            long ownedCoinCount,
            long missingCoinCount,
            double percentageComplete
    ) {
        this.userId = userId;
        this.totalCoinsInCatalog = totalCoinsInCatalog;
        this.ownedCoinCount = ownedCoinCount;
        this.missingCoinCount = missingCoinCount;
        this.percentageComplete = percentageComplete;
    }

    public int getUserId() {
        return userId;
    }

    public long getTotalCoinsInCatalog() {
        return totalCoinsInCatalog;
    }

    public long getOwnedCoinCount() {
        return ownedCoinCount;
    }

    public long getMissingCoinCount() {
        return missingCoinCount;
    }

    public double getPercentageComplete() {
        return percentageComplete;
    }

    @Override
    public String toString() {
        return "CollectionProgressView{" +
                "userId=" + userId +
                ", totalCoinsInCatalog=" + totalCoinsInCatalog +
                ", ownedCoinCount=" + ownedCoinCount +
                ", missingCoinCount=" + missingCoinCount +
                ", percentageComplete=" + percentageComplete +
                '}';
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }

        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }

        CollectionProgressView otherView = (CollectionProgressView) otherObject;

        return userId == otherView.userId
                && totalCoinsInCatalog == otherView.totalCoinsInCatalog
                && ownedCoinCount == otherView.ownedCoinCount
                && missingCoinCount == otherView.missingCoinCount
                && Double.compare(percentageComplete, otherView.percentageComplete) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, totalCoinsInCatalog, ownedCoinCount, missingCoinCount, percentageComplete);
    }
}
