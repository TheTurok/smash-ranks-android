/**
 * Everything within this package is necessary for the app's syncing functionality. Large portions
 * of the code were taken (copied and pasted basically) entirely from these three sources:
 * 1. https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 * 2. the BasicSyncAdapter.zip download found at the link above
 * 3. https://developer.android.com/reference/android/content/AbstractThreadedSyncAdapter.html
 *
 * Doing all of this allows for our app to be found in the device's global Accounts & Sync
 * settings menu. This way, the user can easily enable / disable this functionality.
 */
package com.garpr.android.data.sync;
