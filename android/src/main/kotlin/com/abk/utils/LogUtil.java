package com.abk.utils;

import android.content.Context;
import android.util.Log;

public class LogUtil {
    public static final int VERBOSE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;
    public static final int NO_LOG = 5;

    private static Context mContext;
    private static LogUtil instance = null;
    private static int LOG_LEVEL = VERBOSE;

    public static LogUtil init(Context context) {
        if (instance == null) {
            instance = new LogUtil(context);
        }
        return instance;
    }

    private LogUtil(Context context) {
        mContext = context;
    }

    /**
     * 设置日志等级
     *
     * @param level 日志等级
     */
    public static void setLogLevel(int level) {
        LOG_LEVEL = level;
    }

    /**
     * 作为日志的TAG，包括日志接口调用者的当前线程名称、类名称、类所在行数、方法名称。
     *
     * @return
     */
    private static String getFunctionName() {
        return "PrintAbk";
//        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
//        if (sts == null) {
//            return null;
//        }
//        for (StackTraceElement st : sts) {
//            if (st.isNativeMethod()) {
//                continue;
//            }
//            if (st.getClassName().equals(Thread.class.getName())) {
//                continue;
//            }
//            if (st.getClassName().equals(init(mContext).getClass().getName())) {
//                continue;
//            }
//            return "[" + Thread.currentThread().getName() + ": "
//                    + st.getFileName() + ":" + st.getLineNumber() + " "
//                    + st.getMethodName() + "]";
//        }
//        return null;
    }

    public static void v(String tag, String msg) {
        if (VERBOSE < LOG_LEVEL)
            return;
        Log.v(tag, msg);
    }

    public static void v(String msg) {
        v(getFunctionName(), msg);
    }

    public static void d(String tag, String msg) {
        if (DEBUG < LOG_LEVEL)
            return;
        Log.d(tag, msg);
    }

    public static void d(String msg) {
        d(getFunctionName(), msg);
    }

    public static void i(String tag, String msg) {
        if (INFO < LOG_LEVEL)
            return;
        Log.i(tag, msg);
    }

    public static void i(String msg) {
        i(getFunctionName(), msg);
    }

    public static void w(String tag, String msg) {
        if (WARN < LOG_LEVEL)
            return;
        Log.w(tag, msg);
    }

    public static void w(String msg) {
        w(getFunctionName(), msg);
    }

    public static void w(String msg, Throwable tr) {
        w(getFunctionName(), msg + tr.getMessage());
    }

    public static void w(String tag, String msg, Throwable tr) {
        w(tag, msg + tr.getMessage());
    }

    public static void e(String tag, String msg) {
        if (ERROR < LOG_LEVEL)
            return;
        Log.e(tag, msg);
    }

    public static void e(String msg) {
        e(getFunctionName(), msg);
    }

    public static void e(String msg, Throwable tr) {
        e(getFunctionName(), msg + tr.getMessage());
    }
}
