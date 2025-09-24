package com.pt.ifp.neolauncher;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

public class DeviceProfile {
    public final boolean isLandscape;
    public final boolean isLargeTablet;
    public final InvariantDeviceProfile inv = new InvariantDeviceProfile();

    public DeviceProfile(Context c) {
        Resources res = c.getResources();
        Configuration cfg = res.getConfiguration();
        isLandscape   = cfg.orientation == Configuration.ORIENTATION_LANDSCAPE;
        isLargeTablet = cfg.smallestScreenWidthDp >= 600; // 600dp 常用做平板門檻
        inv.numHotseatIcons   = 5; // 你要幾格
        inv.hotseatAllAppsRank = inv.numHotseatIcons / 2; // 若沒用到可忽略
    }

    public boolean isVerticalBarLayout() {
        // Launcher3 的邏輯大概是：橫向但不是大平板則側邊列；你也可直接 return false
        return isLandscape && !isLargeTablet;
    }

    public static class InvariantDeviceProfile {
        public int numHotseatIcons;
        public int hotseatAllAppsRank;
    }
}
