package com.janboerman.invsee.glowstone;

import net.glowstone.entity.GlowPlayer;
import net.glowstone.entity.meta.profile.GlowPlayerProfile;
import net.glowstone.io.PlayerDataService.PlayerReader;
import net.glowstone.net.GlowSession;

class FakePlayer extends GlowPlayer {


    FakePlayer(GlowSession session, GlowPlayerProfile profile, PlayerReader reader) {
        super(session, profile, reader);
    }


}
