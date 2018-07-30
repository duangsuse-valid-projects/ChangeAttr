package org.duangsuse.attrtools;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChangeAttr extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_attr);
        setFinishOnTouchOutside(true);

        TextView readme = findViewById(R.id.readme);
        Button addi = findViewById(R.id.add_i);
        Button subi = findViewById(R.id.sub_i);

        readme.setTextIsSelectable(true);

        String path = path();
        if (path == null) {
            finish();
            return;
        }

        Ext2Attr e2 = new Ext2Attr(getExecPath());

        if (!e2.connected())
            toast(R.string.connect_failed);

        int m = 0;
        String mode = "";

        try {
            m = e2.query(path);
        } catch (RuntimeException e) {
            toast(e);
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
        }

        SpannableString text = SpannableString.valueOf(String.format("%1$s\n\n%2$s\n\n" + "%3$s\n\n%4$s\n", getString(R.string.file_path), path, getString(R.string.mode), mode));
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
        return Environment.getDataDirectory().getAbsolutePath() + "/data/" + getPackageName() + "/lib/libe2im.so";
    }

    private void toast(@NonNull RuntimeException e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void toast(int res_id) {
        Toast.makeText(this, res_id, Toast.LENGTH_SHORT).show();
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
}
