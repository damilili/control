package com.hoody.commonbase.log;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.hoody.commonbase.util.TimeUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 控制台输出
 */
class FileLogger extends AbsLogger {
    private static final String LOG_DIR_NAME = "log";
    private static String LOG_DIR;
    private static final int SaveLogCount = 5;
    private final LinkedList<String> logBuffer = new LinkedList<String>();
    private boolean isWriteThreadLive;
    private File mLogFile;
    private ExecutorService mThreadPool = Executors.newFixedThreadPool(1);
    private SimpleDateFormat mFileNameFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat df = new SimpleDateFormat("[MM-dd HH:mm:ss.SSS]");
    private String[] ps = {"", "", "V", "D", "I", "W", "E", "A", "A"};

    public FileLogger(Context context) {
        File logDir = context.getDir(LOG_DIR_NAME, Context.MODE_PRIVATE);
        Date today = new Date();
        String fileName = mFileNameFormat.format(today) + ".log";
        Date dayBefore = TimeUtils.getDay(today, -SaveLogCount);
        if (logDir.exists()) {
            File[] logFiles = logDir.listFiles();
            if (logFiles != null) {
                for (File file : logFiles) {
                    try {
                        String fileDateStr = file.getName().substring(0, 10);
                        Date fileDate = mFileNameFormat.parse(fileDateStr);
                        if (fileDate != null) {
                            boolean before = fileDate.before(dayBefore);
                            if (before) {
                                file.delete();
                            }
                        }
                    } catch (ParseException e) {
                        Log.d("FileLogger", "FileLogger() called with: context = [" + e + "]");
                        e.printStackTrace();
                    }
                }
            }
        }
        LOG_DIR = logDir.getAbsolutePath();
        mLogFile = new File(logDir, fileName);
    }

    @Override
    void e(String tag, String msg, Throwable e) {
        log(ERROR, tag, msg);
    }


    @Override
    void v(String tag, String msg) {
        log(VERBOSE, tag, msg);
    }

    @Override
    void d(String tag, String msg) {
        log(DEBUG, tag, msg);
    }

    @Override
    void i(String tag, String msg) {
        log(INFO, tag, msg);
    }

    @Override
    void w(String tag, String msg) {
        log(WARN, tag, msg);
    }

    @Override
    void e(String tag, String msg) {
        log(ERROR, tag, msg);
    }

    private void log(int level, String tag, String msg) {
        if (!enable()) {
            return;
        }
        synchronized (logBuffer) {
            logBuffer.add(logImpl(level, tag, msg));
            if (logBuffer.size() >= 1 && !isWriteThreadLive) {
                isWriteThreadLive = true;
                mThreadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        BufferedOutputStream logOutputStream = null;
                        try {
                            logOutputStream = new BufferedOutputStream(new FileOutputStream(mLogFile, true));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (logOutputStream == null) {
                            return;
                        }
                        while (logBuffer.size() > 0) {
                            String msg;
                            synchronized (logBuffer) {
                                msg = logBuffer.poll();
                            }
                            if (msg != null) {
                                try {
                                    logOutputStream.write(msg.getBytes("UTF-8"));
                                    logOutputStream.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        try {
                            logOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        isWriteThreadLive = false;
                    }
                });
            }
        }
    }

    protected String logImpl(int level, String tag, String msg) {
        String time = df.format(new Date());
        StringBuilder sb = new StringBuilder(100 + tag.length() + msg.length());
        sb.append(time);
        sb.append("\t");
        sb.append(ps[level]);
        sb.append("/");
        sb.append(tag);
        sb.append("(");
        sb.append(Process.myPid());
        sb.append(',');
        sb.append(Thread.currentThread().getId());
        sb.append("):");
        sb.append(msg);
        sb.append("\n");
        return sb.toString();
    }
}

