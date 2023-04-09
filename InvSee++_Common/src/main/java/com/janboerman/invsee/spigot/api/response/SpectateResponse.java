package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.SpectatorInventory;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A SpectateResponse can either be successful, or not.
 * If a SpectateResponse is successful, {@link #isSuccess()} returns true and {@link #getInventory()} returns a {@link SpectatorInventory} normally.
 * @param <SI> the type of SpectatorInventory
 *
 * @see <a href="https://github.com/Jannyboy11/InvSee-plus-plus/wiki/Basic-usage#spectateresponse">SpectateResponse on the InvSee++ wiki</a>
 * @see OpenResponse
 */
//like Either<NotCreatedReason, SpectatorInventory>
public interface SpectateResponse<SI extends SpectatorInventory<?>> {

    /** Get whether this response was successful.
     * @return true if this response is successful, otherwise false */
    public boolean isSuccess();

    /** Get the SpectatorInventory of this response.
     * @return the spectator inventory
     * @throws NoSuchElementException if this response is not successful */
    public SI getInventory() throws NoSuchElementException;

    /**
     * Get the reason why the spectator inventory could not be created.
     * @return the reason
     * @throws NoSuchElementException if this response is successful
     */
    public NotCreatedReason getReason() throws NoSuchElementException;

    /** Much like {@link Optional#ifPresent(Consumer)} */
    public default void ifSuccess(Consumer<? super SI> inventoryConsumer) {
        if (isSuccess()) {
            inventoryConsumer.accept(getInventory());
        }
    }

    /** Much like {@link Optional#ifPresent(Consumer)} */
    public default void ifFailure(Consumer<? super NotCreatedReason> reasonConsumer) {
        if (!isSuccess()) {
            reasonConsumer.accept(getReason());
        }
    }

    /** Create a succeeded SpectateResponse.
     * @param spectatorInventory the spectator inventory to succeed with
     * @return a new SpectateResponse */
    public static <SI extends SpectatorInventory<?>> SpectateResponse<SI> succeed(SI spectatorInventory) {
        return new Succeed<>(spectatorInventory);
    }

    /** Create a failed SpectateResponse.
     * @param reason the reason why it failed
     * @return a new SpectateResponse */
    public static <SI extends SpectatorInventory<?>> SpectateResponse<SI> fail(NotCreatedReason reason) {
        return new Fail<>(reason);
    }

    /** Convert an {@link Optional} into a SpectateResponse.
     * @param optional the optional value used for success
     * @param ifEmpty the reason used to fail the SpectateReponse if the optional {@linkplain Optional#isEmpty()}
     * @return a new SpectateResponse */
    public static <SI extends SpectatorInventory<?>> SpectateResponse<SI> fromOptional(Optional<SI> optional, NotCreatedReason ifEmpty) {
        if (optional.isPresent()) {
            return succeed(optional.get());
        } else {
            return fail(ifEmpty);
        }
    }

    /** Convert an {@link Optional} into a SpectateResponse. */
    public static <SI extends SpectatorInventory<?>> SpectateResponse<SI> fromOptional(Optional<SI> optional) {
        return fromOptional(optional, NotCreatedReason.generic());
    }

    /** Convert a SpectateResponse into an {@link Optional}. The optional will have a value present if the spectate response is successful.
     * If the spectate response is not successful, then the returned optional will be empty.
     * @param response the SpectateResponse
     * @return the optional */
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

        Succeed<?> that = (Succeed<?>) obj;
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

        Fail<?> that = (Fail<?>) obj;
        return Objects.equals(this.getReason(), that.getReason());
    }
}