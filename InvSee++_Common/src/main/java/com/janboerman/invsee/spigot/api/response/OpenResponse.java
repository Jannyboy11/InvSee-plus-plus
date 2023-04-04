package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.SpectatorInventoryView;

import java.util.NoSuchElementException;
import java.util.Objects;

// Either<NotCreatedReason, InventoryView>
public interface OpenResponse<SIV extends SpectatorInventoryView<?>> {

    public boolean isOpen();

    public SIV getOpenInventory() throws NoSuchElementException;

    public NotOpenedReason getReason() throws NoSuchElementException;

    public static <SIV extends SpectatorInventoryView<?>> OpenResponse<SIV> open(SIV inventoryView) {
        return new Open<>(inventoryView);
    }

    public static <SIV extends SpectatorInventoryView<?>> OpenResponse<SIV> closed(NotOpenedReason reason) {
        return new Closed<>(reason);
    }

    @Deprecated
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