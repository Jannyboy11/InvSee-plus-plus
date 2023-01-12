package com.janboerman.invsee.spigot.api.response;

import org.bukkit.inventory.InventoryView;

import java.util.NoSuchElementException;
import java.util.Objects;

// Either<NotCreatedReason, InventoryView>
public interface OpenResponse<IV extends InventoryView> {

    public boolean isOpen();

    public IV getOpenInventory() throws NoSuchElementException;

    public NotOpenedReason getReason() throws NoSuchElementException;

    public static <IV extends InventoryView> OpenResponse<IV> open(IV inventoryView) {
        return new Open<>(inventoryView);
    }

    public static <IV extends InventoryView> OpenResponse<IV> closed(NotOpenedReason reason) {
        return new Closed<>(reason);
    }

    public static <IV extends InventoryView> OpenResponse<IV> ofNullable(IV nullableView, NotOpenedReason ifNull) {
        if (nullableView == null) {
            return closed(ifNull);
        } else {
            return open(nullableView);
        }
    }

}

class Open<IV extends InventoryView> implements OpenResponse<IV> {

    private final IV inventoryView;

    Open(IV inventoryView) {
        this.inventoryView = Objects.requireNonNull(inventoryView);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public IV getOpenInventory() {
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

class Closed<IV extends InventoryView> implements OpenResponse<IV> {

    private final NotOpenedReason reason;

    Closed(NotOpenedReason reason) {
        this.reason = Objects.requireNonNull(reason);
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public IV getOpenInventory() throws NoSuchElementException {
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