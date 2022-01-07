package com.example.frameworkdemo;

import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;

import java.util.List;

import static android.os.storage.VolumeInfo.STATE_EJECTING;
import static android.os.storage.VolumeInfo.STATE_MOUNTED;
import static android.os.storage.VolumeInfo.STATE_MOUNTED_READ_ONLY;
import static android.os.storage.VolumeInfo.STATE_REMOVED;
import static android.os.storage.VolumeInfo.STATE_UNMOUNTABLE;

/**
 * Listen Volume mount event
 * FAT mount:
 * {@link VolumeInfo#STATE_UNMOUNTED} -> {@link VolumeInfo#STATE_CHECKING} -> {@link VolumeInfo#STATE_MOUNTED}
 * FAT unmount:
 * {@link VolumeInfo#STATE_MOUNTED} -> {@link VolumeInfo#STATE_EJECTING} -> {@link VolumeInfo#STATE_UNMOUNTED}
 * -> {@link VolumeInfo#STATE_BAD_REMOVAL}
 * NTFS mount:
 * {@link VolumeInfo#STATE_UNMOUNTED} -> {@link VolumeInfo#STATE_CHECKING} -> {@link VolumeInfo#STATE_UNMOUNTABLE}
 * NTFS unmount:
 * {@link VolumeInfo#STATE_UNMOUNTABLE} -> {@link VolumeInfo#STATE_REMOVED}
 *
 * @author GW00241230
 * @date 2021/8/6
 */
public class VolumeDetector {
    private final String TAG = "DeviceDetector";
    private final StorageEventListener mStorageEventListener;
    private final StorageManager mStorageManager;

    public VolumeDetector(StorageManager mStorageManager) {
        this.mStorageManager = mStorageManager;
//        mStorageManager = ContextHolder.getContext().getSystemService(StorageManager.class);
        mStorageEventListener = new StorageEventListenerImpl();
    }

    public void startDetect() {
        //framework中的方法
        mStorageManager.registerListener(mStorageEventListener);
        checkInvalidDisk();
        checkExistVol();
    }

    public void stopDetect() {
        mStorageManager.unregisterListener(mStorageEventListener);
    }

    private void handleVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
        if (isVolInvalid(vol)) {
            return;
        }
        switch (newState) {
            case STATE_MOUNTED:
            case STATE_MOUNTED_READ_ONLY:
            case STATE_UNMOUNTABLE:
                break;
            case STATE_EJECTING:
            case STATE_REMOVED:
                break;
            default:
                break;
        }
    }

    private boolean isVolInvalid(VolumeInfo volumeInfo) {
        return volumeInfo == null
                || TextUtils.isEmpty(volumeInfo.getFsUuid())
                || volumeInfo.getDisk() == null
                || TextUtils.isEmpty(volumeInfo.getDiskId());
    }

    private void checkInvalidDisk() {
        //framework中的方法
        List<DiskInfo> diskInfoList = mStorageManager.getDisks();
        if (diskInfoList == null || diskInfoList.size() == 0) {
            return;
        }
        for (DiskInfo disk : diskInfoList) {
            if (isInvalidDisk(disk)) {
                onInvalidDiskScanned(disk);
            }
        }
    }

    private void checkExistVol() {
        //framework中的方法
        List<VolumeInfo> volumeInfoList = mStorageManager.getVolumes();
        if (volumeInfoList == null || volumeInfoList.size() == 0) {
            return;
        }
        for (VolumeInfo volumeInfo : volumeInfoList) {
            int state = volumeInfo.getState();
            handleVolumeStateChanged(volumeInfo, -1, state);
        }
    }

    private boolean isInvalidDisk(DiskInfo disk) {
        return disk.volumeCount == 0;
    }

    /**
     * construct a empty volume for invalid volumeCount disk
     *
     * @param disk empty disk
     */
    private void onInvalidDiskScanned(DiskInfo disk) {
        VolumeInfo volumeInfo = new VolumeInfo(disk.id, disk.flags, disk, disk.id);
    }

    /**
     * construct a empty volume for invalid volumeCount disk
     *
     * @param disk empty disk
     */
    private void onInvalidDiskDestroyed(DiskInfo disk) {
        VolumeInfo volumeInfo = new VolumeInfo(disk.id, disk.flags, disk, disk.id);
    }

    private class StorageEventListenerImpl extends StorageEventListener {
        @Override
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            handleVolumeStateChanged(vol, oldState, newState);
        }

        @Override
        public void onDiskScanned(DiskInfo disk, int volumeCount) {
            if (isInvalidDisk(disk)) {
                onInvalidDiskScanned(disk);
            }
        }

        @Override
        public void onDiskDestroyed(DiskInfo disk) {
            if (isInvalidDisk(disk)) {
                onInvalidDiskDestroyed(disk);
            }
        }
    }
}
