package com.janboerman.invsee.faststats;

public final class FastStats {

    /** API Token as presented in the 'project settings' on the FastStats website. */
    public static final String API_TOKEN = "3e3ed377fb507fb659ff4b33151e055e";

    /** Which library are we using to talk to FastStats? */
    // We copied the sources from faststats-java: https://github.com/faststats-dev/faststats-java.
    public static final String SDK_NAME = "faststats-java";
    /** And what version of the library are we using? */
    // At the time of copying, version 0.21.0 was the latest released version.
    public static final String SDK_VERSION = "0.21.0";

    private FastStats() {
    }

}
