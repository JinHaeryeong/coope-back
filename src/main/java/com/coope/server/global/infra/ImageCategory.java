package com.coope.server.global.infra;

public enum ImageCategory {
    NOTICE("notices"),
    COMMENT("comments"),
    PROFILE("profiles");

    private final String dir;

    ImageCategory(String dir) {
        this.dir = dir;
    }

    public String dir() {
        return dir;
    }
}
