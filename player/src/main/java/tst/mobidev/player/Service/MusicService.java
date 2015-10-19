package tst.mobidev.player.Service;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import tst.mobidev.player.instance.Song;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author by Bramengton
 * @date 07.10.15.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener
{

    private ServiceListener mListener;
    public interface ServiceListener {
        void onCompleted();
        void onError(Exception e);
        void onPaused();
        void onStoped(int position);
        void onChange(int oldPosition, int newPosition);
        void onSeek();
        void onPlayed(int position, Song song);
    }

    public void registerListener(ServiceListener listener) {
        mListener = listener;
        if(isPlaying() && mListener != null && songsArray!=null && !songsArray.isEmpty()){
            mListener.onPlayed(songPosn,songsArray.get(songPosn));
        }
    }

    public void unregisterListener() {
        mListener = null;
    }

    private MediaPlayer mMediaPlayer;
    private ArrayList<Song> songsArray;

    private int songPosn;

    private static IBinder mBind;

    private static boolean shuffle=false;
    private static boolean pause=false;

    private Random rand;

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBind == null) mBind = new LocalBinder();
        return mBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        rand=new Random();
        songPosn=0;
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void initMusicPlayer(){
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs){
        songsArray =theSongs;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if(mp.getCurrentPosition()>0){
            if(mListener != null)
                mListener.onSeek();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mp.getCurrentPosition()>0){
            mp.reset();
            if(mListener != null)
                mListener.onCompleted();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if(mListener != null)
            mListener.onError(new Exception("MediaPlayer got some error"));
        mp.reset();
        return false;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        start();
    }

    public void playSong(int songIndex){
        mMediaPlayer.reset();
        songPosn=songIndex;
        pause = false;
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songsArray.get(songIndex).getID());
        try{
            mMediaPlayer.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            if(mListener != null)
                mListener.onError(new Exception("MediaPlayer - Error setting data source"));
        }
        mMediaPlayer.prepareAsync();
    }

    //============================= control methods

    public int getCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration(){
        return mMediaPlayer.getDuration();
    }

    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    public boolean isOnPause(){
        return pause;
    }

    public boolean isShuffle(){
        return shuffle;
    }

    public void pausePlayer(){
        pause = true;
        mMediaPlayer.pause();
        if(mListener != null)
            mListener.onPaused();
    }

    public void seekTo(int msec){
        mMediaPlayer.seekTo(msec);
    }

    public void start(){
        pause = false;
        mMediaPlayer.start();
        if(mListener != null && songsArray!=null)
            mListener.onPlayed(songPosn,songsArray.get(songPosn));
    }

    public void stop(){
        pause = false;
        mMediaPlayer.stop();
        if(mListener != null)
            mListener.onStoped(songPosn);
    }

    public void playPrevious(){
        int olPosn = songPosn;
        songPosn--;
        if(songPosn<0) songPosn= songsArray.size()-1;
        playSong(songPosn);
        if(mListener != null)
            mListener.onChange(olPosn,songPosn);
    }

    public void playNext(){
        int olPosn = songPosn;
        if(shuffle){
            int newSong = songPosn;
            while(newSong==songPosn){
                newSong=rand.nextInt(songsArray.size());
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn>= songsArray.size()) songPosn=0;
        }
        playSong(songPosn);
        if(mListener != null)
            mListener.onChange(olPosn, songPosn);
    }

    //toggle shuffle
    public boolean setShuffle(){
        return (shuffle = !shuffle);
    }
}
