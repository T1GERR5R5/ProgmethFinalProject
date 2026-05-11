package game;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;

/**
 * Manages all game audio: background music (BGM) and looping sound effects (SFX).
 * All methods are {@code static}; audio resources are cached after the first load.
 */
public class SoundManager {
    private static HashMap<String, Clip> sfxClips = new HashMap<>();
    private static Clip bgmClip;

    private static final String PATH_FIGHT = "/sounds/fight.wav";
    private static final String PATH_LOBBY = "/sounds/lobby.wav";
    private static final String PATH_FIRE  = "/sounds/fire.wav";
    private static final String PATH_ICE   = "/sounds/ice.wav";
    private static final String PATH_WIND  = "/sounds/wind.wav";
    private static final String PATH_THROW = "/sounds/throw.wav";

    /** Stops the looping fire SFX. */
    public static void stopFire() { stopSFX("fire"); }
    /** Stops the looping ice SFX. */
    public static void stopIce()  { stopSFX("ice"); }
    /** Stops the looping wind SFX. */
    public static void stopWind() { stopSFX("wind"); }

    /** Starts the fire SFX in a continuous loop. */
    public static void playFireLoop() { playSFXLoop("fire", PATH_FIRE); }
    /** Starts the ice SFX in a continuous loop. */
    public static void playIceLoop()  { playSFXLoop("ice", PATH_ICE); }
    /** Starts the wind SFX in a continuous loop. */
    public static void playWindLoop() { playSFXLoop("wind", PATH_WIND); }

    /**
     * Plays a named SFX in a continuous loop until {@link #stopSFX} is called.
     * @param name         identifier used to cache and stop the clip
     * @param resourcePath classpath resource path to the WAV file
     */
    private static void playSFXLoop(String name, String resourcePath) {
        Clip clip = getOrCreateClip(name, resourcePath);
        if (clip != null) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }

    /**
     * Stops a looping SFX by name.
     * @param name the identifier passed to {@link #playSFXLoop}
     */
    private static void stopSFX(String name) {
        Clip clip = sfxClips.get(name);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    private static Clip getOrCreateClip(String name, String resourcePath) {
        Clip clip = sfxClips.get(name);
        if (clip == null) {
            try {
                URL url = SoundManager.class.getResource(resourcePath);
                if (url != null) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
                    clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    sfxClips.put(name, clip);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        return clip;
    }

    /** Starts the lobby BGM loop (stops any currently playing BGM first). */
    public static void playLobbyBGM() { playBGM(PATH_LOBBY); }
    /** Starts the fight BGM loop (stops any currently playing BGM first). */
    public static void playFightBGM() { playBGM(PATH_FIGHT); }
    /** Stops the currently playing BGM. */
    public static void stopBGM() { if (bgmClip != null && bgmClip.isRunning()) bgmClip.stop(); }

    private static void playBGM(String resourcePath) {
        stopBGM();
        try {
            URL url = SoundManager.class.getResource(resourcePath);
            if (url != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(audioStream);
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
