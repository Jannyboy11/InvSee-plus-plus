package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.SpectatorInventoryView;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * <p>
 *      Much like SpectateResponse, this class represents the result of opening a SpectatorInventory.
 * </p>
 * <p>
 *      An OpenResponse can either be <i>open</i> or <i>closed</i>. If it is open, then {@link #getOpenInventory()} will return a {@link SpectatorInventoryView} normally.
 *      If the OpenResponse is closed, then {@link #getReason()} will tell you the reason why the SpectatorInventory could not be opened.
 * </p>
 *
 * @see SpectateResponse
 */
// Either<NotCreatedReason, InventoryView>
public interface OpenResponse<SIV extends SpectatorInventoryView<?>> {

    /**
     * Get whether the SpectatorInventory could be opened successfully.
     * @return true if the inventory could be opened successfully, otherwise false
     */
    public boolean isOpen();

    /**
     * Get the {@link SpectatorInventoryView} associated with the opened inventory.
     * @return the spectator inventory view
     * @throws NoSuchElementException if this response is closed.
     */
    public SIV getOpenInventory() throws NoSuchElementException;

    /**
     * Get the {@link NotOpenedReason} associated with the failed open attempt.
     * @return the reason
     * @throws NoSuchElementException if this response is open.
     */
    public NotOpenedReason getReason() throws NoSuchElementException;

    /**
     * Create an open OpenResponse
     * @param inventoryView the window which is opened.
     * @return a new open OpenResponse
     * @param <SIV> the spectator inventory view type
     */
    public static <SIV extends SpectatorInventoryView<?>> OpenResponse<SIV> open(SIV inventoryView) {
        return new Open<>(inventoryView);
    }

    /**
     * Create a closed OpenResponse
     * @param reason the reason why a window could not be opened.
     * @return a new closed OpenResponse
     * @param <SIV> the spectator inventory type
     */
    public static <SIV extends SpectatorInventoryView<?>> OpenResponse<SIV> closed(NotOpenedReason reason) {
        return new Closed<>(reason);
    }

    @Deprecated//(forRemoval = true, since = "0.25.2") //TODO remove in 1.0
    public static <SIV extends SpectatorInventoryView<?>> OpenResponse<SIV> ofNullable(SIV nullableView, NotOpenedReason ifNull) {
        if (nullableView == null) {
            return closed(ifNull);
        } else {
            return open(nullableView);
        }
    }
}

class Open<SIV extends SpectatorInventoryView<?>> implements OpenResponse<SIV> {

    private final SIV inventoryView;

    Open(SIV inventoryView) {
        this.inventoryView = Objects.requireNonNull(inventoryView);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public SIV getOpenInventory() {
        return inventoryView;
    }

    @Override
    public NotOpenedReason getReason() throws NoSuchElementException {
        throw new NoSuchElementException("Open");
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(inventoryView);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Open)) return false;

        Open<?> that = (Open<?>) o;
        return Objects.equals(this.getOpenInventory(), that.getOpenInventory());
    }
}

class Closed<SIV extends SpectatorInventoryView<?>> implements OpenResponse<SIV> {

    private final NotOpenedReason reason;

    Closed(NotOpenedReason reason) {
        this.reason = Objects.requireNonNull(reason);
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public SIV getOpenInventory() throws NoSuchElementException {
        throw new NoSuchElementException("Closed");
    }

    @Override
    public NotOpenedReason getReason() {
        return reason;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(reason);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Closed)) return false;

        Closed<?> that = (Closed<?>) o;
        return Objects.equals(this.getReason(), that.getReason());
    }
}