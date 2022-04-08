package com.anas.jcmusintegrationwithdiscord.discord;

import com.anas.jcmusintegrationwithdiscord.PartFormatter;
import com.anas.jcmusintegrationwithdiscord.configs.ConfigsManger;
import com.anas.jcmusintegrationwithdiscord.track.Track;
import com.anas.jcmusintegrationwithdiscord.track.TrackInfo;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

public class DiscordController {
    private final String ID;
    private long startTime;

    public DiscordController(String ID) {
        this.ID = ID;
        setup();
        startTime = System.currentTimeMillis();
    }

    private void setup() {
        // Discord shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (ConfigsManger.getInstance().isDebug())
                System.out.println("Shutting down Discord controller...");
            DiscordRPC.discordShutdown();
        }));

        DiscordRPC.discordInitialize(ID, null, true);
        DiscordRPC.discordRegister(ID, "");
    }

    public void updateActivity(Track track) {
        if (track == null ||
                track.getTrackInfo().getStatus() == TrackInfo.Status.STOPPED) {
            // If track is paused for while of time or track is stopped, clear the activity
            DiscordRPC.discordClearPresence();
            return;
        }
        DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(
                PartFormatter.format(ConfigsManger.getInstance().getConfigs().getPartOneFormat(), track));

        builder.setDetails(PartFormatter.format(ConfigsManger.getInstance().getConfigs().getPartTowFormat(), track));
        builder.setBigImage(ConfigsManger.getInstance().getConfigs().getCaverImage(), "CMUS Player");
        builder.setStartTimestamps(startTime);
        if (track.getTrackInfo().getStatus() == TrackInfo.Status.PLAYING) {
            builder.setSmallImage(ConfigsManger.getInstance().getConfigs().getPlayIcon(), "Playing");
        } else if (track.getTrackInfo().getStatus() == TrackInfo.Status.PAUSED) {
            builder.setSmallImage(ConfigsManger.getInstance().getConfigs().getPauseIcon(), "Paused");
        }

        DiscordRPC.discordUpdatePresence(builder.build());
    }
}
