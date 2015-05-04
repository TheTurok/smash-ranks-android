package com.garpr.android.data;


abstract class RegionBasedCall<T> extends Call<T> {


    RegionBasedCall(final Response<T> response, final boolean ignoreCache)
            throws IllegalArgumentException {
        super(response, ignoreCache);
    }


    @Override
    String getUrl() {
        final String regionId = Settings.getRegion().getId();
        return super.getUrl() + regionId + '/';
    }


}
