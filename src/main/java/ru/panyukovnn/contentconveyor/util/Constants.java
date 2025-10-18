package ru.panyukovnn.contentconveyor.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final String DEFAULT_DB_USER = "retelling-bot";
    public static final int MAX_TG_MESSAGE_SIZE = 4096;
    public static final Pattern YOUTUBE_VIDEO_ID_PATTERN = Pattern.compile("^[\\w-]{11}$");

    public static final UUID HABR_PUBLISHING_CHANNEL_SET_ID = UUID.fromString("72cd37bb-d821-49f5-8ae1-a952c3ffcd23");
}
