package com.gw;

import org.apache.commons.cli.*;

public class ArgumentParser {

    public static final String ARG_LOGIN = "login";
    public static final String ARG_PASSWORD = "password";
    public static final String ARG_HEADLESS = "headless";
    public static final String ARG_THREADS = "threads";


    // example string: -l login -p password -h headless -t threads

    public CommandLine parse(String[] args){
        Options options = new Options();
        options.addOption("l", ARG_LOGIN, true, "SSP portal login");
        options.addOption("p", ARG_PASSWORD, true, "SSP portal password");
        options.addOption("h", ARG_HEADLESS, false, "Headless mode ON");
        options.addOption("t", ARG_THREADS, true, "Number of threads");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            return cmd;
        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
