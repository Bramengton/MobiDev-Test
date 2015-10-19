package tst.mobidev.player.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import tst.mobidev.player.R;
import tst.mobidev.player.Utils;
import tst.mobidev.player.instance.Song;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author by Bramengton
 * @date 07.10.15.
 */
public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs;

    private String secLabel;
    private LayoutInflater mInflater;

    public SongAdapter(Context context){
        super();
        secLabel = context.getString(R.string.secondLabel);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        songs = new ArrayList<Song>();
    }

    public void setUpdate(@NonNull final ArrayList<Song> update, @NonNull final Song.SortType sort) {
        super.notifyDataSetInvalidated();
        Collections.sort(update, new Song(sort));
        songs=update;
        super.notifyDataSetChanged();
    }

    public ArrayList<Song> getData() {
        return songs;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.child_song, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.SongName = (TextView) view.findViewById(R.id.song_name);
            viewHolder.SongInfo = (TextView) view.findViewById(R.id.dur_ar_al);
            viewHolder.image = (ImageView) view.findViewById(R.id.avatar);
            view.setTag(viewHolder);
        }
        final Song currSong = songs.get(position);
        final ViewHolder holder = (ViewHolder) view.getTag();
        if(holder!=null) {
            holder.SongName.setText(currSong.getTitle());
            String duration = new Utils().milliSecondsToTimer(currSong.getDuration());
            holder.SongInfo.setText(String.format(secLabel, duration, currSong.getArtist(), currSong.getAlbum()));
            int state = (currSong.getState() ? R.drawable.ic_play : R.drawable.ic_music_note);
            holder.image.setImageResource(state);
        }
        return view;
    }

    private static class ViewHolder {
        protected ImageView image;
        protected TextView SongName;
        protected TextView SongInfo;
    }
}

