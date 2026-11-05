package com.horrormod.client.entity;

import com.horrormod.entity.NullClone;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.Level;

public class ClientNullClone extends NullClone implements ClientAvatarEntity {
    private final ClientAvatarState avatarState = new ClientAvatarState();
    private final PlayerSkinRenderCache skinRenderCache;
    private PlayerSkin skin = ClientMannequin.DEFAULT_SKIN;
    private CompletableFuture<Optional<PlayerSkin>> skinLookup;

    public ClientNullClone(EntityType<NullClone> type, Level level) {
        super(type, level);
        this.skinRenderCache = Minecraft.getInstance().playerSkinRenderCache();
        this.updateSkin();
    }

    @Override
    public void tick() {
        super.tick();
        this.avatarState.tick(this.position(), this.getDeltaMovement());
        if (this.skinLookup != null && this.skinLookup.isDone()) {
            try {
                this.skinLookup.get().ifPresent(resolved -> this.skin = resolved);
            } catch (Exception ignored) {
                this.skin = ClientMannequin.DEFAULT_SKIN;
            }
            this.skinLookup = null;
        }
    }

    @Override
    public void onSyncedDataUpdated(net.minecraft.network.syncher.EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_PROFILE.equals(accessor)) {
            this.updateSkin();
        }
    }

    private void updateSkin() {
        if (this.skinLookup != null) {
            this.skinLookup.cancel(false);
        }
        this.skinLookup = this.skinRenderCache.lookup(this.getProfile())
                .thenApply(resolved -> resolved.map(PlayerSkinRenderCache.RenderInfo::playerSkin));
    }

    @Override
    public ClientAvatarState avatarState() {
        return this.avatarState;
    }

    @Override
    public PlayerSkin getSkin() {
        return this.skin;
    }

    @Override
    public Parrot.Variant getParrotVariantOnShoulder(boolean left) {
        return null;
    }

    @Override
    public boolean showExtraEars() {
        return false;
    }
}
