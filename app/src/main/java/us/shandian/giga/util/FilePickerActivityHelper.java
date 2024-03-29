package us.shandian.giga.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.eztool.mysimpleapp.R;
import com.nononsenseapps.filepicker.AbstractFilePickerFragment;
import com.nononsenseapps.filepicker.FilePickerFragment;

import java.io.File;

public class FilePickerActivityHelper extends com.nononsenseapps.filepicker.FilePickerActivity {

    private CustomFilePickerFragment currentFragment;

    public static Intent chooseSingleFile(@NonNull Context context) {
        return new Intent(context, FilePickerActivityHelper.class)
                .putExtra(FilePickerActivityHelper.EXTRA_ALLOW_MULTIPLE, false)
                .putExtra(FilePickerActivityHelper.EXTRA_ALLOW_CREATE_DIR, false)
                .putExtra(FilePickerActivityHelper.EXTRA_SINGLE_CLICK, true)
                .putExtra(FilePickerActivityHelper.EXTRA_MODE, FilePickerActivityHelper.MODE_FILE);
    }

    public static Intent chooseFileToSave(@NonNull Context context, @Nullable String startPath) {
        return new Intent(context, FilePickerActivityHelper.class)
                .putExtra(FilePickerActivityHelper.EXTRA_ALLOW_MULTIPLE, false)
                .putExtra(FilePickerActivityHelper.EXTRA_ALLOW_CREATE_DIR, true)
                .putExtra(FilePickerActivityHelper.EXTRA_ALLOW_EXISTING_FILE, true)
                .putExtra(FilePickerActivityHelper.EXTRA_START_PATH, startPath)
                .putExtra(FilePickerActivityHelper.EXTRA_MODE, FilePickerActivityHelper.MODE_NEW_FILE);
    }

    public static boolean isOwnFileUri(@NonNull Context context, @NonNull Uri uri) {
        if (uri.getAuthority() == null) return false;
        return uri.getAuthority().startsWith(context.getPackageName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        if (ThemeHelper.isLightThemeSelected(this)) {
//            this.setTheme(R.style.FilePickerThemeLight);
//        } else {
        this.setTheme(R.style.FilePickerThemeDark);
//        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        // If at top most level, normal behaviour
        if (currentFragment.isBackTop()) {
            super.onBackPressed();
        } else {
            // Else go up
            currentFragment.goUp();
        }
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(@Nullable String startPath, int mode, boolean allowMultiple, boolean allowCreateDir, boolean allowExistingFile, boolean singleClick) {
        final CustomFilePickerFragment fragment = new CustomFilePickerFragment();
        fragment.setArgs(startPath != null ? startPath : Environment.getExternalStorageDirectory().getPath(),
                mode, allowMultiple, allowCreateDir, allowExistingFile, singleClick);
        return currentFragment = fragment;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Internal
    //////////////////////////////////////////////////////////////////////////*/

    public static class CustomFilePickerFragment extends FilePickerFragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);

            final View view = viewHolder.itemView.findViewById(android.R.id.text1);
            if (view instanceof TextView) {
                ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.file_picker_items_text_size));
            }

            return viewHolder;
        }

        @Override
        public void onClickOk(@NonNull View view) {
            if (mode == MODE_NEW_FILE && getNewFileName().isEmpty()) {
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(getActivity(), R.string.file_name_empty_error, Toast.LENGTH_SHORT);
                mToast.show();
                return;
            }

            super.onClickOk(view);
        }

        @Override
        protected boolean isItemVisible(@NonNull File file) {
            if (file.isDirectory() && file.isHidden()) return true;
            return super.isItemVisible(file);
        }

        public File getBackTop() {
            if (getArguments() == null) return Environment.getExternalStorageDirectory();

            final String path = getArguments().getString(KEY_START_PATH, "/");
            if (path.contains(Environment.getExternalStorageDirectory().getPath())) {
                return Environment.getExternalStorageDirectory();
            }

            return getPath(path);
        }

        public boolean isBackTop() {
            return compareFiles(mCurrentPath, getBackTop()) == 0 || compareFiles(mCurrentPath, new File("/")) == 0;
        }

        @Override
        public void onLoadFinished(Loader<SortedList<File>> loader, SortedList<File> data) {
            super.onLoadFinished(loader, data);
            layoutManager.scrollToPosition(0);
        }
    }
}
