package com.example.androiddemo.ai;

import android.content.Context;

import com.iflytek.sparkchain.core.SparkChain;
import com.iflytek.sparkchain.core.SparkChainConfig;

public class SparkChainManager {

    private SparkChainManager() {
    }

    public static void init(Context context) {
        SparkChainConfig config = SparkChainConfig.builder()
                .appID("6ddfba69")
                .apiKey("0184ea607ba31353f9fefa328b05b85c")
                .apiSecret("ZjM3OGQ4ZTE0NDY0MzkxMzZiODc3NjQx");
        SparkChain.getInst().init(context, config);
    }
}
