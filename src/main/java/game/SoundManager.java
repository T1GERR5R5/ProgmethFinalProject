package game;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;

public class SoundManager {
    private static HashMap<String, Clip> sfxClips = new HashMap<>();
    private static Clip bgmClip;

    private static final String PATH_FIGHT = "/sounds/fight.wav";
    private static final String PATH_LOBBY = "/sounds/lobby.wav";
    private static final String PATH_FIRE  = "/sounds/fire.wav";
    private static final String PATH_ICE   = "/sounds/ice.wav";
    private static final String PATH_WIND  = "/sounds/wind.wav";
    private static final String PATH_THROW = "/sounds/throw.wav";

    // --- ฟังก์ชันสำหรับหยุดเสียงเฉพาะอย่าง ---
    public static void stopFire() { stopSFX("fire"); }
    public static void stopIce()  { stopSFX("ice"); }
    public static void stopWind() { stopSFX("wind"); }

    // --- ฟังก์ชันเล่นแบบวนลูป (Loop) สำหรับ Status Effect ---
    public static void playFireLoop() { playSFXLoop("fire", PATH_FIRE); }
    public static void playIceLoop()  { playSFXLoop("ice", PATH_ICE); }
    public static void playWindLoop() { playSFXLoop("wind", PATH_WIND); }

    // ==========================================
    // ระบบจัดการภายใน
    // ==========================================

    // เล่น SFX แบบวนลูปจนกว่าจะสั่ง Stop
    private static void playSFXLoop(String name, String resourcePath) {
        Clip clip = getOrCreateClip(name, resourcePath);
        if (clip != null) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // วนลูปไม่สิ้นสุด
            clip.start();
        }
    }

    // หยุดการทำงานของ SFX ตามชื่อที่ระบุ
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

    // BGM Management (เหมือนเดิม)
    public static void playLobbyBGM() { playBGM(PATH_LOBBY); }
    public static void playFightBGM() { playBGM(PATH_FIGHT); }
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