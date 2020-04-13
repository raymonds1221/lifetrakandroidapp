package com.salutron.lifetrakwatchapp.util;

import android.os.Environment;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;

//import ch.qos.logback.classic.Level;
import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by raymondsarmiento on 8/10/15.
 */
public class LifeTrakLogger {
    private static Logger mLogger = LoggerFactory.getLogger(LifeTrakLogger.class);


    public static void configure() {
       // org.apache.log4j.Logger.getLogger(LifeTrakLogger.class).setAdditivity(false);

        LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(getLogPath());
        logConfigurator.setRootLevel(Level.ALL);
        logConfigurator.setLevel("com.salutron", Level.ALL);
        logConfigurator.setMaxFileSize(1024 * 1024 * 5);
        logConfigurator.configure();

      //  BasicConfigurator.configure();
    }

    public static void info(String msg) {
        mLogger.info(msg);

    }

    public static void warn(String msg) {
        mLogger.warn(msg);
    }

    public static void debug(String msg) {
        mLogger.debug(msg);
    }

    public static void error(String msg) {
        mLogger.error(msg);
    }

    public static String getLogPath() {
        return Environment.getExternalStorageDirectory() + File.separator + "LifetrakLogs"+ File.separator + "lifetrak.log";
    }

    public static String getLogPathDIR() {
        return Environment.getExternalStorageDirectory() + File.separator + "LifetrakLogs";
    }


    public static File lastFileModified(String dir) {
        File fl = new File(dir);
        File[] files = fl.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        return choice;
    }
}
