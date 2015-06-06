package com.garpr.android.calls;


import com.garpr.android.settings.Settings;


abstract class RegionBasedCall<T> extends Call<T> {


    RegionBasedCall(final Response<T> response, final boolean ignoreCache)
            throws IllegalArgumentException {
        super(response, ignoreCache);
    }


    @Override
    String getUrl() {
        final String regionId = Settings.Region.get().getId();
        return super.getUrl() + regionId + '/';
    }


}
