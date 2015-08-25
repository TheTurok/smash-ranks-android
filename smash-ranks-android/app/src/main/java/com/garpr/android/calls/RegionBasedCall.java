package com.garpr.android.calls;


import com.garpr.android.models.Region;
import com.garpr.android.settings.Settings;


public abstract class RegionBasedCall<T> extends Call<T> {


    RegionBasedCall(final Response<T> response, final boolean ignoreCache)
            throws IllegalArgumentException {
        super(response, ignoreCache);
    }


    Region getRegion() {
        return Settings.Region.get();
    }


    @Override
    String getUrl() {
        final Region region = getRegion();
        final String regionId = region.getId();
        return super.getUrl() + regionId + '/';
    }


}
