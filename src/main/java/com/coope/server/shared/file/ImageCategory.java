package com.coope.server.shared.file;

public enum ImageCategory {
    NOTICE("notices"),
    COMMENT("comments"),
    PROFILE("profiles"),
    CHAT("chats"),
    COVER("covers"),
    DOCUMENT("documents"),
    INQUIRY("inquiries"),
    COMMUNITY("community");

    private final String dir;

    ImageCategory(String dir) { this.dir = dir; }

    public String dir() { return dir; }
}
