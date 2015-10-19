package tst.mobidev.player;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import tst.mobidev.player.Adapter.SongAdapter;
import tst.mobidev.player.Service.MusicService;
import tst.mobidev.player.customdialogs.OpenFileDialog;
import tst.mobidev.player.instance.Song;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author by Bramengton
 * @date 09.10.15.
 */
public class Player extends Fragment implements View.OnClickListener {

    private final int PLAYPAUSE = R.id.playpause;
    private final int PLAYICO = R.drawable.ic_play;
    private final int PAUSEICO = R.drawable.ic_pause;
    private final int STOP = R.id.stop;
    private final int PREVIOUS = R.id.prev;
    private final int NEXT = R.id.next;
    private final int SHUFFLE = R.id.shuffle;
    private final int SHUFFLEICO = R.drawable.ic_shuffle;
    private final int REPEATICO = R.drawable.ic_repeat;

    private final String SAVE_CURRENT_PATH = "SAVE_CURRENT_PATH";

    private static MusicService musicService;
    private Intent mIntent;
    private static boolean musicBound=false;

    private final Utils utils = new Utils();
    private ManageSeekTimer manageSeekTimer;
    private Thread manageTimer;

    private SeekBar songProgressBar;
    private TextView songInfo;
    private TextView songDuration;
    private String saveDirectoryPath;
    private ListView songView;

    private SongAdapter mAdapter;

    private SearchAction mSearchAction;
    private enum SearchAction {
        SEARCH,
        DEFAULT;
        public boolean isDefault() {
            return this == DEFAULT;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context appContext= getActivity().getApplicationContext();
        if(mIntent ==null){
            mIntent = new Intent(appContext, MusicService.class);
            getContext().startService(mIntent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.player, container, false);

        setControls(view);
        songView = (ListView) view.findViewById(R.id.song_list);
        songView.setEmptyView(view.findViewById(R.id.empty));
        mAdapter = new SongAdapter(view.getContext());
        songView.setAdapter(mAdapter);

        Intent intent = getActivity().getIntent();
        Uri data = intent.getData();
        if (data!=null && data.getScheme().equals("file")){
            updSongList(data.getPath(), true);
            musicService.playSong(0);
        }else{
            if(!onRestoreInstanceState(savedInstanceState))updSongList(false);
        }

        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stop();
                play(position);
            }
        });
        getActivity().getApplicationContext().bindService(mIntent, mServiceConnector, Context.BIND_AUTO_CREATE);
        return view;
    }

    private void setControls(View view){
        view.findViewById(PLAYPAUSE).setOnClickListener(this);
        view.findViewById(STOP).setOnClickListener(this);
        view.findViewById(PREVIOUS).setOnClickListener(this);
        view.findViewById(NEXT).setOnClickListener(this);
        view.findViewById(SHUFFLE).setOnClickListener(this);
        songInfo = (TextView) view.findViewById(R.id.SongLabel);
        songDuration  = (TextView) view.findViewById(R.id.DurationLabel);
        songProgressBar = (SeekBar) view.findViewById(R.id.SongProgressBar);
        songProgressBar.setOnSeekBarChangeListener(new SeekBarChange());
        songProgressBar.setProgress(0);
        songProgressBar.setMax(99);
    }

    private ServiceConnection mServiceConnector = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService =  ((MusicService.LocalBinder)service).getService();
            musicService.setList(mAdapter.getData());
            musicBound = true;
            musicService.registerListener(mServiceListener);

            if(musicService.isShuffle()) {
                ((ImageView) getActivity().findViewById(R.id.shuffle_ico)).setImageResource(SHUFFLEICO);
            }else{
                ((ImageView) getActivity().findViewById(R.id.shuffle_ico)).setImageResource(REPEATICO);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private MusicService.ServiceListener mServiceListener= new MusicService.ServiceListener(){

        @Override
        public void onCompleted() {
            ((ImageView) getActivity().findViewById(R.id.playpause_ico)).setImageResource(PLAYICO);
        }

        @Override
        public void onError(Exception e) {
            Log.e("",e.getMessage());
        }

        @Override
        public void onPaused() {
            ((ImageView) getActivity().findViewById(R.id.playpause_ico)).setImageResource(PLAYICO);
        }

        @Override
        public void onStoped(int current) {
            songInfo.setText(R.string.noSong);
            songDuration.setText(R.string.noDuration);
            ((ImageView) getActivity().findViewById(R.id.playpause_ico)).setImageResource(PLAYICO);
            mAdapter.getData().get(current).setState(false);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChange(int oldPosition, int newPosition) {
            mAdapter.getData().get(oldPosition).setState(false);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSeek() {
            UpdateSongLabels();
        }

        @Override
        public void onPlayed(int position, Song song) {
            ((ImageView) getActivity().findViewById(R.id.playpause_ico)).setImageResource(PAUSEICO);
            String temp = String.format(
                    getString(R.string.controllerLabel),song.getArtist(), song.getTitle());
            songInfo.setText(temp);
            updateProgressBar();
            mAdapter.getData().get(position).setState(true);
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(SAVE_CURRENT_PATH, saveDirectoryPath);
    }

    private boolean onRestoreInstanceState(Bundle savedInstanceState){
        if(savedInstanceState!=null) {
            saveDirectoryPath = savedInstanceState.getString(SAVE_CURRENT_PATH);
            if(saveDirectoryPath!=null) {
                updSongList(saveDirectoryPath, false);
                savedInstanceState.clear();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (musicService != null) {
            musicService.unregisterListener();
        }
        getActivity().getApplicationContext().unbindService(mServiceConnector);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView == null) return;
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new SearchListener());
        mSearchAction = SearchAction.DEFAULT;
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuActionExpandListener());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                getCustomMediaPath();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updSongList(boolean playOnComplite){
        saveDirectoryPath = null;
        initList(null, null, playOnComplite);
    }

    private void updSongList(String customPath, boolean playOnComplite){
        String selection = MediaStore.Audio.Media.DATA + " like ? ";
        saveDirectoryPath = customPath;
        String[] selctionArg = new String[]{"%"+customPath+"%"};
        initList(selection, selctionArg, playOnComplite);
    }

    private void initList(String selection, String[] selctionArg, boolean playOnComplite){
        ContentResolver musicResolver = getContext().getContentResolver();
        ArrayList<Song> songList = new ArrayList<Song>();

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.IS_MUSIC};
        Cursor musicCursor = musicResolver.query(musicUri, projection, selection, selctionArg, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            do {
                if(musicCursor.getInt(5)==1) {
                    long Id = musicCursor.getLong(0);
                    String title = musicCursor.getString(1);
                    String artist = musicCursor.getString(2);
                    String album = musicCursor.getString(3);
                    long duaration = musicCursor.getLong(4);
                    songList.add(new Song(Id, title, artist, album, duaration, false));
                }
            }
            while (musicCursor.moveToNext());
            mAdapter.setUpdate(songList, Song.SortType.SORT_BY_ARTIST);
            if(musicService!=null){
                musicService.setList(songList);
                if(playOnComplite) musicService.playNext();
            }
            musicCursor.close();
        }
    }

    public boolean isPlaying() {
        return musicService != null && musicBound && musicService.isPlaying();
    }

    public boolean isOnPause() {
        return musicService != null && musicBound && musicService.isOnPause();
    }

    public int getCurrentPosition() {
        if(isPlaying())
            return musicService.getCurrentPosition();
        else return 0;
    }

    public int getDuration() {
        if(isPlaying())
            return musicService.getDuration();
        else return 0;
    }

    private void playNext(){
        musicService.playNext();
        if(manageSeekTimer!=null) manageSeekTimer.onStop();
    }

    private void playPrev(){
        musicService.playPrevious();
        if(manageSeekTimer!=null) manageSeekTimer.onStop();
    }

    private void play(int position){
        if(isPlaying()){
            musicService.pausePlayer();
            if(manageSeekTimer!=null) manageSeekTimer.onPause();
        }else if(isOnPause()){
            musicService.start();
            if(manageSeekTimer!=null) manageSeekTimer.onResume();
        }else{
            if(manageSeekTimer!=null) manageSeekTimer.onStop();
            musicService.playSong(position);
        }
    }

    private void stop(){
        if(isPlaying() || isOnPause()){
            musicService.stop();
            manageSeekTimer.onStop();
        }
    }

    private void shuffle(){
        if(musicService!=null && musicBound && musicService.setShuffle()) {
            ((ImageView) getActivity().findViewById(R.id.shuffle_ico)).setImageResource(SHUFFLEICO);
        }else{
            ((ImageView) getActivity().findViewById(R.id.shuffle_ico)).setImageResource(REPEATICO);
        }
    }

    public void UpdateSongLabels(){
        List<Song> songs = mAdapter.getData();
        if(songs!=null && songs.size()>0 && (isPlaying() || isOnPause())) {
            int totalDuration = getDuration();
            int currentDuration = getCurrentPosition();
            String label = String.format(getString(R.string.controllerDuration),
                    utils.milliSecondsToTimer(totalDuration),
                    utils.milliSecondsToTimer(currentDuration));
            songDuration.setText(label);
        }
    }

    private class SearchListener implements SearchView.OnQueryTextListener{
        @Override
        public boolean onQueryTextSubmit(final String newText){
            String newFilter = !TextUtils.isEmpty(newText) ? newText.toLowerCase() : null;
            if (newFilter == null) return true;
            mSearchAction = SearchAction.SEARCH;
            Collection<Song> filter = Song.Filter(mAdapter.getData(), new Song.Predicate<Song>() {
                @Override
                public boolean apply(Song type) {
                    return (type.getTitle().toLowerCase()).matches(".*" + newText + ".*");
                }
            });
            mAdapter.setUpdate(new ArrayList<Song>(filter), Song.SortType.SORT_BY_ARTIST);
            if(musicService!=null) musicService.setList(mAdapter.getData());
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s){
            return false;
        }
    }

    private class MenuActionExpandListener implements MenuItemCompat.OnActionExpandListener{
        private ArrayList<Song> mOldList;
        @Override
        public boolean onMenuItemActionExpand(MenuItem menuItem) {
            mOldList = new ArrayList<Song>(mAdapter.getData());
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            if(mSearchAction.isDefault()) return true;
            mAdapter.setUpdate(mOldList, Song.SortType.SORT_BY_ARTIST);
            if(musicService!=null) musicService.setList(mAdapter.getData());
            mSearchAction=SearchAction.DEFAULT;
            return true;
        }
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case PLAYPAUSE:
                play(0);
                break;
            case STOP:
                stop();
                break;
            case PREVIOUS:
                playPrev();
                break;
            case NEXT:
                playNext();
                break;
            case SHUFFLE:
                shuffle();
                break;
            default:
                break;
        }
    }

    private void getCustomMediaPath(){
        OpenFileDialog fileDialog = OpenFileDialog.newInstance(getContext(), null, true);
        fileDialog.setFilter(".*\\" + ".mp3");
        fileDialog.setOpenDialogListener(new OpenFileDialog.OpenDialogDirListener() {
            @Override
            public void OnSelectedDir(Context context, String dirPath) {
                updSongList(dirPath, false);
            }
        });
        fileDialog.show(getFragmentManager(), OpenFileDialog.getFragmentTAG());
    }


    //============================================================================

    public void updateProgressBar() {
        manageSeekTimer = new ManageSeekTimer();
        if(manageTimer!=null && manageTimer.isAlive()) manageTimer.interrupt();
        manageTimer = new Thread(manageSeekTimer);
        manageTimer.start();
    }

    private class SeekBarChange implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser && manageSeekTimer!=null && (isPlaying() || isOnPause())){
                manageSeekTimer.onPause();
                int totalDuration = getDuration();
                int currentPosition = utils.getProgress(progress, totalDuration);
                musicService.seekTo(currentPosition);
            }
            if(isPlaying() || isOnPause())
                UpdateSongLabels();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(manageSeekTimer!=null && isPlaying()) {
                manageSeekTimer.onResume();
            }
        }
    }

    private class ManageSeekTimer implements Runnable {
        private final Object mLock;
        private boolean mPaused;
        private boolean mStop;
        private boolean mFinished;

        public ManageSeekTimer() {
            mLock = new Object();
            mPaused = false;
            mFinished = false;
            mStop = false;
        }

        public void run() {
            while (!mFinished) {
                int totalDuration = getDuration();
                int currentDuration = getCurrentPosition();
                while (currentDuration < totalDuration) {
                    synchronized (mLock) {
                        while (mPaused) {
                            try {
                                mLock.wait();
                            }
                            catch (InterruptedException e) {
                                return;
                            }
                        }

                        if(mStop){mFinished = true; return;}
                    }
                    try {
                        Thread.sleep(25);
                        currentDuration = getCurrentPosition();
                        int progress = utils.getPercent(currentDuration, totalDuration);
                        songProgressBar.setProgress(progress);
                    }
                    catch (InterruptedException e) {
                        return;
                    }
                    catch (Exception e) {
                        return;
                    }

                }
            }
        }

        /**
         * Try pause.
         */
        public void onPause() {
            synchronized (mLock) {
                mPaused = true;
                mStop=false;
            }
        }

        /**
         * Just stop.
         */
        public void onStop() {
            synchronized (mLock) {
                mStop = true;
            }
        }

        /**
         * Try resume.
         */
        public void onResume() {
            synchronized (mLock) {
                mPaused = false;
                mStop = false;
                mFinished = false;
                mLock.notifyAll();
            }
        }
    }
}
