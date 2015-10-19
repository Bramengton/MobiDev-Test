package tst.mobidev.player.instance;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/**
 * @author by paladium
 * @date 07.10.15.
 */
public class Song  implements Parcelable, Comparator<Song> {

    private long id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private boolean state;

    public Song(long songID, String songTitle, String songArtist, String songAlbum, long songDuration, boolean isplay){
        id=songID;
        title=songTitle;
        artist=songArtist;
        album = songAlbum;
        duration = songDuration;
        state = isplay;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getAlbum(){return album;}
    public long getDuration(){return duration;}
    public Boolean getState(){return state;}
    public void setState(boolean newstate){ state= newstate;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeLong(id);
        arg0.writeString(title);
        arg0.writeString(artist);
        arg0.writeString(album);
        arg0.writeLong(duration);
        arg0.writeByte((byte) (state ? 1 : 0));
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>()
    {
        public Song createFromParcel(Parcel in)
        {
            return new Song(in.readLong(), in.readString(),in.readString(), in.readString(), in.readLong(), (in.readByte() != 0));
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    private SortType mSongSort;
    public enum SortType {
        SORT_BY_ID,
        SORT_BY_TITLE,
        SORT_BY_ALBUM,
        SORT_BY_ARTIST
    }
    public Song(SortType SortType){
        this.mSongSort = SortType;
    }

    @Override
    public int compare(Song o1, Song o2) {
        if(mSongSort.equals(SortType.SORT_BY_ID))
            return (o1.getID()==o2.getID()) ? 0:1;
        if(mSongSort.equals(SortType.SORT_BY_ARTIST))
            return o1.getArtist().compareToIgnoreCase(o2.getArtist());
        if(mSongSort.equals(SortType.SORT_BY_ALBUM))
            return o1.getAlbum().compareToIgnoreCase(o2.getAlbum());
        if(mSongSort.equals(SortType.SORT_BY_TITLE))
            return o1.getTitle().compareToIgnoreCase(o2.getTitle());
        return 1;
    }

    public interface Predicate<T> { boolean apply(T type); }

    public static <T> Collection<T> Filter(Collection<T> col, Predicate<T> predicate) {
        Collection<T> result = new ArrayList<T>();
        for (T element: col) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }

}
