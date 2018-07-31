package org.duangsuse.attrtools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class ChangeAttr extends Activity {
    private static final String message = "%1$s\n\n%2$s\n\n" + "%3$s\n\n%4$s\n";
    private static Ext2Attr e2;
    private static AlertDialog a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(true);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        });

        Timer timer = new Timer();

        a = new AlertDialog.Builder(this)
                .setTitle(R.string.acquire_root)
                .setIcon(R.drawable.icon)
                .setMessage(R.string.wait_for_perm)
                .setCancelable(false)
                .setOnCancelListener((v) -> run()).show();

        e2 = new Ext2Attr(getExecPath());

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // noinspection StatementWithEmptyBody
                while (e2.not_connected());
                a.cancel();
            }
        }, 100);
    }

    private void run() {
        setContentView(R.layout.activity_change_attr);

        TextView readme = findViewById(R.id.readme);
        Button addi = findViewById(R.id.add_i);
        Button subi = findViewById(R.id.sub_i);

        readme.setTextIsSelectable(true);

        if (e2.not_connected() && !a.isShowing())
            toast(R.string.connect_failed);

        String path = path();
        if (path == null) {
            finish();
            return;
        }

        int m = 0;
        String mode;

        try {
            m = e2.query(path);
        } catch (RuntimeException e) {
            toast(e);
        } catch (Exception e1) {
            toast(e1);
        }

        switch (m) {
            case 0:
                mode = getString(R.string.mode_no);
                break;
            case 1:
                mode = getString(R.string.mode_imm);
                break;
            case 2:
                mode = getString(R.string.mode_app);
                break;
            case 3:
                mode = getString(R.string.mode_imap);
                break;
            case -1:
                mode = getString(R.string.mode_badfp);
                break;
            default:
                mode = getString(R.string.err_no_perm);
        }

        SpannableString text = SpannableString.valueOf(String.format(message, getString(R.string.file_path), path, getString(R.string.mode), mode));
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.GREEN);
        int start = 2 + getString(R.string.file_path).length();
        text.setSpan(colorSpan, start, start + path.length(), Spanned.SPAN_MARK_MARK);
        readme.setText(text);

        // Add immutable
        addi.setOnClickListener((v) -> {
            int i = 0;
            try {
                i = e2.addi(path);
            } catch (RuntimeException e) {
                toast(e);
            }
            if (i != 0)
                toast(R.string.unchanged);
        });

        // Remove immutable
        subi.setOnClickListener((v) -> {
            int i = 0;
            try {
                i = e2.subi(path);
            } catch (RuntimeException e) {
                toast(e);
            }
            if (i != 0)
                toast(R.string.unchanged);
        });
    }

    @NonNull
    private String getExecPath() {
        return getApplicationInfo().nativeLibraryDir + "/libe2im.so";
    }

    private void toast(@NonNull Exception e) {
        Toast.makeText(this, mapMessage(e.getMessage()), Toast.LENGTH_SHORT).show();
    }

    private void toast(int res_id) {
        Toast.makeText(this, res_id, Toast.LENGTH_SHORT).show();
    }

    private String mapMessage(String message) {
        if (message == null)
            return getString(R.string.no_perm);

        switch (message) {
            case "Function not implemented":
                return getString(R.string.err_func_no_imp);
            case "Not a typewriter":
                return getString(R.string.err_not_typewriter);
            case "Operation not supported":
                return getString(R.string.err_not_supp);
            case "Operation not supported on transport endpoint":
                return getString(R.string.err_not_supported);
            case "Inappropriate ioctl for device":
                return getString(R.string.err_bad_ioctl);
            case "Operation not permitted":
                return getString(R.string.err_no_perm);
        }
        return message;
    }

    @Nullable
    private String path() {
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data == null) {
            toast(R.string.no_path);
            return null;
        }

        return data.getPath();
    }

    @Override
    public void finish() {
        e2.close();
        super.finish();
    }
}
