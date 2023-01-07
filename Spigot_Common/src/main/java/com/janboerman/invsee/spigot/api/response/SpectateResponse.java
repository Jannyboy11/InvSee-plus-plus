package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.SpectatorInventory;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

//like Either<NotCreatedReason, SpectatorInventory>
public interface SpectateResponse<SI extends SpectatorInventory<?>> {

    public boolean isSuccess();

    public SI getInventory() throws NoSuchElementException;

    public NotCreatedReason getReason() throws NoSuchElementException;

    public static <SI extends SpectatorInventory<?>> SpectateResponse<SI> succeed(SI spectatorInventory) {
        return new Succeed<>(spectatorInventory);
    }

    public static <SI extends SpectatorInventory<?>> SpectateResponse<SI> fail(NotCreatedReason reason) {
        return new Fail<>(reason);
    }

    public static <SI extends SpectatorInventory<?>> SpectateResponse<SI> fromOptional(Optional<SI> optional, NotCreatedReason ifEmpty) {
        if (optional.isPresent()) {
            return succeed(optional.get());
        } else {
            return fail(ifEmpty);
        }
    }

    public static <SI extends SpectatorInventory<?>> SpectateResponse<SI> fromOptional(Optional<SI> optional) {
        return fromOptional(optional, NotCreatedReason.generic());
    }

    public static <SI extends SpectatorInventory<?>> Optional<SI> toOptional(SpectateResponse<SI> response) {
        if (response.isSuccess()) {
            return Optional.ofNullable(response.getInventory());
        } else {
            return Optional.empty();
        }
    }
}

class Succeed<SI extends SpectatorInventory<?>> implements SpectateResponse<SI> {

    private final SI inventory;

    Succeed(SI inventory) {
        this.inventory = Objects.requireNonNull(inventory);
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    public SI getInventory() {
        return inventory;
    }

    @Override
    public NotCreatedReason getReason() throws NoSuchElementException {
        throw new NoSuchElementException("Succeed");
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(inventory);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Succeed)) return false;

        Succeed that = (Succeed) obj;
        return Objects.equals(this.getInventory(), that.getInventory());
    }
}

class Fail<SI extends SpectatorInventory<?>> implements SpectateResponse<SI> {

    private final NotCreatedReason reason;

    Fail(NotCreatedReason reason) {
        this.reason = Objects.requireNonNull(reason);
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public SI getInventory() throws NoSuchElementException {
        throw new NoSuchElementException("Fail");
    }

    public NotCreatedReason getReason() {
        return reason;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(reason);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Fail)) return false;

        Fail that = (Fail) obj;
        return Objects.equals(this.getReason(), that.getReason());
    }
}