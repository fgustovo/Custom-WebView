package com.videodetector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum MimeType {
    Threegp(false, "video/3gp"),
    avi(false, "video/avi"),
    asf(false, "video/asf"),
    divx(false, "video/divx"),
    f4v(false, "video/f4v"),
    flv(false, "video/flv"),
    m2v(false, "video/m2v"),
    m4v(false, "video/m4v"),
    m3u8(true, "video/m3u8"),
    m3u(true, "video/m3u"),
    mkv(false, "video/mkv"),
    mp4(false, "video/mp4"),
    mpeg(false, "video/mpeg"),
    mpg(false, "video/mpg"),
    mov(false, "video/mov"),
    ogg(false, "video/ogg"),
    rmvb(false, "video/rmvb"),
    rm(false, "video/rm"),
    vob(false, "video/vob"),
    webm(false, "video/webm"),
    wmv(false, "video/wmv"),
    wtv(false, "video/wtv"),
    application_mpegurl(true, "application/mpegurl"),
    application_x_mpegurl(true, "application/x-mpegurl"),
    audio_mpegurl(true, "audio/mpegurl"),
    audio_x_mpegurl(true, "audio/x-mpegurl"),
    vnd_apple_mpegurl(true, "application/vnd.apple.mpegurl"),
    vnd_apple_mpegurl_audio(true, "application/vnd.apple.mpegurl.audio");

    private static List<String> list;
    private final String type;
    private final boolean playlist;

    private MimeType(boolean playlist, String type) {
        this.playlist = playlist;
        this.type = type;
    }

    public boolean isPlaylist() {
        return playlist;
    }

    public String getType() {
        return this.type;
    }

    private static void initialize() {
        if (list == null) {
            list = new ArrayList<>();
            for (MimeType type : values()) {
                list.add(type.getType());
            }
        }
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() <= 0;
    }


    public static boolean isVideo(String str) {
        initialize();
        if (str != null) {
            str = str.toLowerCase(Locale.ENGLISH);

            for (MimeType a2 : values()) {
                if (str.contains(a2.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static MimeType getMimeType(String str) {
        if (!isEmpty(str)) {
            for (MimeType mt : values()) {
                if (str.equalsIgnoreCase(mt.getType())) {
                    return mt;
                }
            }
        }
        return null;
    }

    public String toString() {
        return getType();
    }

/*
    public static void main(String[] args) throws IOException {
        MediaPlaylistParser parser = new MediaPlaylistParser();

// Parse playlist
//        InputStream input = new URL("https://s3-us-west-2.amazonaws.com/hls-playground/hls.m3u8").openStream();
        InputStream input = new FileInputStream("C:\\Users\\a\\Downloads\\playlist_16907543534780276937.m3u8");
        MediaPlaylist playlist = parser.readPlaylist(input);

// Update playlist version
        MediaPlaylist updated = MediaPlaylist.builder()
                .from(playlist)
                .version(2)
                .build();

// Write playlist to standard out
        System.out.println(parser.writePlaylistAsString(updated));
    }*/


}