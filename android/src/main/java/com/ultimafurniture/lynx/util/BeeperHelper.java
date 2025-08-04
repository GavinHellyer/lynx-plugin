package com.ultimafurniture.lynx.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.IntDef;

import com.ultimafurniture.lynx.GlobalCfg;
import com.ultimafurniture.lynx.R;
import com.ultimafurniture.lynx.bean.type.Key;
import com.ultimafurniture.lynx.bean.type.LinkType;
import com.orhanobut.hawk.Hawk;
import com.payne.reader.base.Consumer;
import com.payne.reader.bean.config.Beeper;
import com.payne.reader.bean.receive.Failure;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author naz
 *         Date 2020/4/13
 */
public class BeeperHelper {
    /**
     * 声音资源类型
     */
    public static final int SOUND_FILE_TYPE_NORMAL = 1;
    public static final int SOUND_FILE_TYPE_SHORT = 2;

    public static boolean sUseSysRing;
    private static MediaPlayer mMediaPlayer; // 创建media player
    private static Uri sUri;

    @IntDef({ SOUND_FILE_TYPE_NORMAL, SOUND_FILE_TYPE_SHORT })
    @Retention(RetentionPolicy.SOURCE)
    @interface SOUND_FILE_TYPE {
    }

    private static volatile Beeper mBeeperType;

    private static SoundPool mSoundPool;

    /**
     * 初始化
     *
     * @param context Context
     */
    public static void init(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSoundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(2)
                    .build();
        } else {
            // 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
            mSoundPool = new SoundPool(2, AudioManager.STREAM_ALARM, 1);
        }
        int load1 = mSoundPool.load(context, R.raw.beeper, SOUND_FILE_TYPE_NORMAL);
        int load2 = mSoundPool.load(context, R.raw.beeper_short, SOUND_FILE_TYPE_SHORT);
        // XLog.i("load1+load2 = " + load1 + "," + load2);
        try {
            mBeeperType = Hawk.get(Key.BEEPER_TYPE, null);
        } catch (Exception ignored) {
        }
        sUseSysRing = Hawk.get(Key.BEEPER_USE_SYS, false);
    }

    public static void setup() {
        Beeper beeperType = mBeeperType;
        if (mBeeperType == null) {
            beeperType = mBeeperType = Beeper.ONCE_END_BEEP;
        } else if (GlobalCfg.get().getLinkType() != LinkType.LINK_TYPE_NET) {
            beeperType = Beeper.QUIET;
        }
        Beeper finalType = beeperType;
        Consumer<Failure> failureConsumer = failure -> {
            ReaderHelper.getReader().setBeeperMode(finalType, null, null);
        };
        ReaderHelper.getReader().setBeeperMode(finalType, null, failureConsumer);
    }

    /**
     * 发声
     *
     * @param soundFileType 类型{@link SOUND_FILE_TYPE}
     */
    public static void beep(@SOUND_FILE_TYPE int soundFileType) {
        if (sUseSysRing) {
            playSysNotifySound(XLog.sContext);
            return;
        }
        if (mSoundPool == null) {
            init(XLog.sContext);
        }
        mSoundPool.play(soundFileType, 1, 1, 0, 0, 1);
    }

    public static void playSysNotifySound(Context context) {
        if (mMediaPlayer == null) {
            sUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.seekTo(0);
                }
            });
            try {
                mMediaPlayer.setDataSource(context, sUri);
                mMediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置蜂鸣类型
     *
     * @param beeper
     */
    public static void setBeeperType(Beeper beeper) {
        mBeeperType = beeper;
        Hawk.put(Key.BEEPER_TYPE, beeper);
    }

    /**
     * 获取蜂鸣类型
     *
     * @return Beeper
     */
    public static Beeper getBeeperType() {
        return mBeeperType;
    }

    /**
     * 释放资源
     */
    public static void release() {
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
