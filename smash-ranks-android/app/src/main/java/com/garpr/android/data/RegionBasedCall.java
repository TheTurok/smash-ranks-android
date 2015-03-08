package com.garpr.android.data;


import com.garpr.android.misc.Utils;


abstract class RegionBasedCall<T> extends Call<T> {


    final String mRegionId;




    RegionBasedCall(final Response<T> response) throws IllegalArgumentException {
        this(response, Settings.getRegion().getId());
    }


    RegionBasedCall(final Response<T> response, final String regionId) throws IllegalArgumentException {
        super(response);

        if (!Utils.validStrings(regionId)) {
            throw new IllegalArgumentException("regionId is invalid");
        }

        mRegionId = regionId;
    }


    @Override
    String getBaseUrl() {
        return super.getBaseUrl() + mRegionId + '/';
    }


}
