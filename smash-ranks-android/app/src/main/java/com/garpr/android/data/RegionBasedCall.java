package com.garpr.android.data;


import com.garpr.android.misc.Utils;


abstract class RegionBasedCall<T> extends Call<T> {


    final String mRegionId;




    RegionBasedCall(final Response<T> response, final boolean ignoreCache)
            throws IllegalArgumentException {
        this(response, Settings.getRegion().getId(), ignoreCache);
    }


    RegionBasedCall(final Response<T> response, final String regionId, final boolean ignoreCache)
            throws IllegalArgumentException {
        super(response);

        if (!Utils.validStrings(regionId)) {
            throw new IllegalArgumentException("regionId is invalid");
        }

        mRegionId = regionId;
    }


    @Override
    String getUrl() {
        return super.getUrl() + mRegionId + '/';
    }


}
