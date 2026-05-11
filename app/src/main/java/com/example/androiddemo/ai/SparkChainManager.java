package com.example.androiddemo.ai;

import android.content.Context;
import android.util.Log;

/**
 * 讯飞 SparkChain SDK 管理器
 * 负责 SDK 初始化和销毁，整个应用生命周期只需一次
 */
import com.iflytek.sparkchain.core.SparkChain;
import com.iflytek.sparkchain.core.SparkChainConfig;

public class SparkChainManager {
    private static final String TAG = "SparkChainManager";
    private static boolean isInitialized = false;

    // 讯飞 SparkChain SDK 凭据
    private static final String APP_ID = "6ddfba69";
    private static final String API_KEY = "0184ea607ba31353f9fefa328b05b85c";
    private static final String API_SECRET = "ZjM3OGQ4ZTE0NDY0MzkxMzZiODc3NjQx";

    /**
     * 初始化 SparkChain SDK
     * @param context Application Context
     */
    public static synchronized void init(Context context) {
        if (isInitialized) {
            Log.d(TAG, "SparkChain SDK 已经初始化，跳过");
            return;
        }

        try {
            SparkChainConfig config = SparkChainConfig.builder()
                    .appID(APP_ID)
                    .apiKey(API_KEY)
                    .apiSecret(API_SECRET)
                    .workDir(context.getFilesDir().getAbsolutePath());

            int result = SparkChain.getInst().init(context, config);
            if (result == 0) {
                isInitialized = true;
                Log.d(TAG, "SparkChain SDK 初始化成功");
            } else {
                Log.e(TAG, "SparkChain SDK 初始化失败，错误码: " + result);
            }
        } catch (Exception e) {
            Log.e(TAG, "SparkChain SDK 初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 逆初始化 SparkChain SDK
     * 应在 Application 结束时调用
     */
    public static synchronized void unInit() {
        if (!isInitialized) {
            Log.d(TAG, "SparkChain SDK 未初始化，跳过逆初始化");
            return;
        }

        try {
            int result = SparkChain.getInst().unInit();
            isInitialized = false;
            if (result == 0) {
                Log.d(TAG, "SparkChain SDK 逆初始化成功");
            } else {
                Log.e(TAG, "SparkChain SDK 逆初始化失败，错误码: " + result);
            }
        } catch (Exception e) {
            Log.e(TAG, "SparkChain SDK 逆初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
}
