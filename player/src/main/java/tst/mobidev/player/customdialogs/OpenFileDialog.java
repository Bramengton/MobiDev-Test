package tst.mobidev.player.customdialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import tst.mobidev.player.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * @author by Bramengton
 * @date 15.02.14.
 */
public class OpenFileDialog extends DialogFragment
{
    private static boolean VisibilityBack;

    private static String currentPath = Environment.getExternalStorageDirectory().getPath();
    private List<File> files = new ArrayList<File>();
    private TextView title;
    private ListView listView;
    private static FilenameFilter filenameFilter;
    private static int selectedIndex = -1;
    private static OpenDialogFileListener listenerFile;
    private static OpenDialogDirListener listenerDirs;
    private static Drawable folderIcon;
    private static Drawable fileIcon;
    private static String accessDeniedMessage;
    private static Context context;


    private static int DROW_BCK_BTN_PX=60;
    private static int RES_TITLE_STYLE = R.style.OpenFileDialogWindowTitle;
    private static int RES_SELCTOR_STYLE = R.drawable.custom_selector;
    private static int RES_BCK_BTN= R.drawable.back;
    private static float MIN_LAYOUT_HEIGHT_DP=48;

    private static final String TAG = "OpenFileDialogFragment";
    public static String getFragmentTAG(){
        return TAG;
    }

    public interface OpenDialogFileListener {
        void OnSelectedFile(Context context, String fileName);
    }

    public interface OpenDialogDirListener {
        void OnSelectedDir(Context context, String dirPath);
    }

    private static class FileAdapter extends ArrayAdapter<File> {

        public FileAdapter(Context context, List<File> files) {
            super(context, android.R.layout.simple_list_item_1, files);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            File file = getItem(position);
            if (view != null) {
                view.setText(file.getName());
                if (file.isDirectory()) {
                    setDrawable(view, folderIcon);
                } else {
                    setDrawable(view, fileIcon);
                    if (selectedIndex == position)
                        view.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                    else
                        view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
                }
            }
            return view;
        }

        private void setDrawable(TextView view, Drawable drawable) {
            if (view != null) {
                if (drawable != null) {
                    drawable.setBounds(0, 0, 60, 60);
                    view.setCompoundDrawables(drawable, null, null, null);
                } else {
                    view.setCompoundDrawables(null, null, null, null);
                }
            }
        }
    }

    protected void inHelper(final Context context, final AlertDialog.Builder builder)
    {
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listenerDirs != null) {
                    listenerDirs.OnSelectedDir(context, currentPath);
                }
                if (selectedIndex > -1 && listenerFile != null) {
                    listenerFile.OnSelectedFile(context, listView.getItemAtPosition(selectedIndex).toString());
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        files.addAll(getFiles(currentPath));
        listView.setAdapter(new FileAdapter(context, files));
    }

    public static OpenFileDialog newInstance(Context cntx, String preset_path, boolean VisibilityBack_BTN){
        OpenFileDialog dialog = new OpenFileDialog();
        context=cntx;
        if(preset_path!=null){
            if(preset_path.length()>0){
                if(new File(preset_path).exists())
                    currentPath=preset_path;
            }
        }
        VisibilityBack=VisibilityBack_BTN;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        setCancelable(false);
        title = createTitle(context);
        changeTitle();
        LinearLayout HeaderLayout = createHeaderLayout(context);
        HeaderLayout.addView(createBackBtnItem(context, (VisibilityBack ? View.VISIBLE : View.GONE)));
        HeaderLayout.addView(title);
        builder.setCustomTitle(HeaderLayout);

        LinearLayout linearLayout = createMainLayout(context);
        linearLayout.addView(createDivider(context));
        listView = createListView(context);
        linearLayout.addView(listView);
        builder.setView(linearLayout);
        inHelper(OpenFileDialog.context, builder);
        return builder.create();
    }

    public OpenFileDialog setFilter(final String... filter) {
        filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String fileName) {
                for (String ext : filter) {
                    File tempFile = new File(String.format("%s/%s", file.getPath(), fileName));
                    return !tempFile.isFile() || tempFile.getName().matches(ext);
                }
                return false;
//                File tempFile = new File(String.format("%s/%s", file.getPath(), fileName));
//                return !tempFile.isFile() || tempFile.getName().matches(filter);
            }
        };
        return this;
    }

    public OpenFileDialog setOpenDialogListener(OpenDialogFileListener listener) {
        listenerFile = listener;
        return this;
    }

    public OpenFileDialog setOpenDialogListener(OpenDialogDirListener listenerDir) {
        listenerDirs = listenerDir;
        return this;
    }

    public OpenFileDialog setFolderIcon(Drawable drawable) {
        folderIcon = drawable;
        return this;
    }

    public OpenFileDialog setFileIcon(Drawable drawable) {
        fileIcon = drawable;
        return this;
    }

    public OpenFileDialog setAccessDeniedMessage(String message) {
        accessDeniedMessage = message;
        return this;
    }

    private static Display getDefaultDisplay(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    @SuppressWarnings("deprecation")
    private static int getScreenSizeX (Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1)
        {
            return getDefaultDisplay(context).getWidth();
        }
        else
        {
            Point screenSize = new Point();
            getDefaultDisplay(context).getSize(screenSize);
            return screenSize.x;
        }
    }

    @SuppressWarnings("deprecation")
    private static int getScreenSizeY (Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1)
        {
            return getDefaultDisplay(context).getHeight();
        }
        else
        {
            Point screenSize = new Point();
            getDefaultDisplay(context).getSize(screenSize);
            return screenSize.y;
        }
    }

    private View createDivider(Context context)
    {
        View divider= new View(context);
        divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
        return divider;
    }

    private static int getLinearLayoutMinHeight(Context context) {
        return getScreenSizeY(context);
    }

    @SuppressWarnings("deprecation")
    private ImageView createBackBtnItem(Context context, int visibility)
    {
        ImageView image = new ImageView(context);
        Drawable drawable = context.getResources().getDrawable(RES_BCK_BTN);
        if(drawable!=null){
            drawable.setBounds(0, 0, DROW_BCK_BTN_PX, DROW_BCK_BTN_PX);
            image.setImageDrawable(drawable);
            image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            image.setVisibility(visibility);
            image.setClickable(true);
            image.setBackgroundResource(RES_SELCTOR_STYLE);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File file = new File(currentPath);
                    File parentDirectory = file.getParentFile();
                    if (parentDirectory != null) {
                        currentPath = parentDirectory.getPath();
                        RebuildFiles(((FileAdapter) listView.getAdapter()));
                    }
                }
            });
        }
        return image;
    }



    private LinearLayout createMainLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setMinimumHeight(getLinearLayoutMinHeight(context));
        return linearLayout;
    }

    private LinearLayout createHeaderLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setMinimumHeight(getItemHeight(context));
        return linearLayout;
    }

    private static int getItemHeight(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        getDefaultDisplay(context).getMetrics(metrics);
        //Get 48 dp to pix in our display metrics
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIN_LAYOUT_HEIGHT_DP, metrics);
    }

    private TextView createTextView(Context context, int style) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, style);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(15, 0, 0, 0);
        textView.setLines(1);
        return textView;
    }


    private TextView createTitle(Context context){
        return createTextView(context, RES_TITLE_STYLE);
    }

    public static int getTextWidth(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.left + bounds.width() + 80;
    }

    private void changeTitle() {
        String titleText = currentPath;
        int screenWidth = getScreenSizeX(context);
        int maxWidth = (int) (screenWidth * 0.99) - DROW_BCK_BTN_PX;
        if (getTextWidth(titleText, title.getPaint()) > maxWidth) {
            while (getTextWidth("..." + titleText, title.getPaint()) > maxWidth) {
                int start = titleText.indexOf("/", 2);
                if (start > 0)
                    titleText = titleText.substring(start);
                else
                    titleText = titleText.substring(2);
            }
            title.setText(String.format("... %1$s",titleText));
        }
        else
        {
            title.setText(String.format("%1$s",titleText));
        }
    }

    private static List<File> getFiles(String directoryPath)
    {
        File directory = new File(directoryPath);
        List<File> fileList = new ArrayList<File>();
        if(directory.listFiles(filenameFilter)!=null)
        {
            fileList = Arrays.asList(directory.listFiles(filenameFilter));
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File file, File file2) {
                    if (file.isDirectory() && file2.isFile())
                        return -1;
                    else if (file.isFile() && file2.isDirectory())
                        return 1;
                    else
                        return file.getPath().compareTo(file2.getPath());
                }
            });
        }
        return fileList;
    }

    private void RebuildFiles(ArrayAdapter<File> adapter) {
        try {
            List<File> fileList = getFiles(currentPath);
            files.clear();
            selectedIndex = -1;
            files.addAll(fileList);
            adapter.notifyDataSetChanged();
            changeTitle();
        } catch (NullPointerException e) {
            String message = context.getResources().getString(android.R.string.unknownName);
            if (!accessDeniedMessage.equals(""))
                message = accessDeniedMessage;
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    private ListView createListView(Context context) {
        ListView listView = new ListView(context);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                final ArrayAdapter<File> adapter = (FileAdapter) adapterView.getAdapter();
                File file = adapter.getItem(index);
                if (file.isDirectory())
                {
                    currentPath = file.getPath();
                    RebuildFiles(adapter);
                }
                else
                {
                    if (index != selectedIndex)
                        selectedIndex = index;
                    else
                        selectedIndex = -1;
                    adapter.notifyDataSetChanged();
                }
            }
        });
        return listView;
    }
}
