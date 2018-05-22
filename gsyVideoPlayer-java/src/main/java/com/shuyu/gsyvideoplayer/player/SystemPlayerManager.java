package com.shuyu.gsyvideoplayer.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.cache.ICacheManager;
import com.shuyu.gsyvideoplayer.model.GSYModel;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import java.util.List;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 系统播放器，总觉得不好用
 * Created by guoshuyu on 2018/1/11.
 */

public class SystemPlayerManager implements IPlayerManager {

    private AndroidMediaPlayer mediaPlayer;

    private Surface surface;

    private boolean release;

    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, Message msg, List<VideoOptionModel> optionModelList, ICacheManager cacheManager) {
        mediaPlayer = new AndroidMediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        release = false;
        GSYModel gsyModel = (GSYModel) msg.obj;
        try {
            if (gsyModel.isCache() && cacheManager != null) {
                cacheManager.doCacheLogic(context, mediaPlayer, gsyModel.getUrl(), gsyModel.getMapHeadData(), gsyModel.getCachePath());
            } else {
                mediaPlayer.setDataSource(context, Uri.parse(gsyModel.getUrl()), gsyModel.getMapHeadData());
            }
            mediaPlayer.setLooping(gsyModel.isLooping());
            if (gsyModel.getSpeed() != 1 && gsyModel.getSpeed() > 0) {
                setSpeed(gsyModel.getSpeed());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showDisplay(Message msg) {
        if (msg.obj == null && mediaPlayer != null && !release) {
            mediaPlayer.setSurface(null);
        } else if (msg.obj != null) {
            Surface holder = (Surface) msg.obj;
            surface = holder;
            if (mediaPlayer != null && holder.isValid() && !release) {
                mediaPlayer.setSurface(holder);
            }
        }
    }

    @Override
    public void setSpeed(float speed, boolean soundTouch) {
        setSpeed(speed);
    }

    @Override
    public void setNeedMute(boolean needMute) {
        try {
            if (mediaPlayer != null && !release) {
                if (needMute) {
                    mediaPlayer.setVolume(0, 0);
                } else {
                    mediaPlayer.setVolume(1, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void releaseSurface() {
        if (surface != null) {
            surface.release();
            surface = null;
        }
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            release = true;
            mediaPlayer.release();
        }
    }

    @Override
    public int getBufferedPercentage() {
        return -1;
    }

    @Override
    public long getNetSpeed() {
        if (mediaPlayer != null) {
            //todo
        }
        return 0;
    }

    @Override
    public void setSpeedPlaying(float speed, boolean soundTouch) {

    }

    private void setSpeed(float speed) {
        if (release) {
            return;
        }
        if (mediaPlayer != null && mediaPlayer.getInternalMediaPlayer() != null && mediaPlayer.isPlayable()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PlaybackParams playbackParams = new PlaybackParams();
                    playbackParams.setSpeed(speed);
                    mediaPlayer.getInternalMediaPlayer().setPlaybackParams(playbackParams);
                } else {
                    Debuger.printfError(" not support setSpeed");
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
