package com.example.indoornavigation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bean.ResponseCode;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;

import java.io.IOException;

public class AddSpotActivity extends AppCompatActivity {
    private ImageView back;
    private ImageView done;
    private ImageView openFile;
    private ImageView recordVoice;

    private EditText name;
    private EditText coordinate;
    private EditText introduce;
    private EditText voicePath;

    private Integer sightId;
    private Integer spotId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);
        init();
    }

    private void init() {
        back = findViewById(R.id.add_spot_back);
        done = findViewById(R.id.add_spot_done);

        openFile = findViewById(R.id.open_voice_file);
        recordVoice = findViewById(R.id.voice_in);

        name = findViewById(R.id.input_spot_name);
        coordinate = findViewById(R.id.input_spot_coordinate);
        introduce = findViewById(R.id.input_spot_introduce);
        voicePath = findViewById(R.id.music_path_text);

        sightId = getIntent().getIntExtra("sightId",0);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSpot();
            }
        });

        openFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVoiceFile();
            }
        });
    }

    public static final int CHOOSE_AUDIO = 1;
    private void openVoiceFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent,CHOOSE_AUDIO);
    }

    private String audioPath;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_AUDIO:
                if (requestCode == RESULT_OK) {
                    String audioPath = null;
                    if (Build.VERSION.SDK_INT >= 19) {
                        audioPath = handleImageOnKitKat(data);
                    } else {
                        audioPath = handleImageBeforeKitKat(data);
                    }
                    Toast.makeText(AddSpotActivity.this, "" + audioPath, Toast.LENGTH_SHORT).show();
                    Log.d("test", "onActivityResult: " + audioPath);
                }
                break;
            default:
                break;
        }
    }

    private void saveSpot() {
        String nameText = name.getText().toString().trim();
        String coordinateText = coordinate.getText().toString().trim();
        String introduceText = introduce.getText().toString().trim();
        RequestBody requestBody = new FormBody.Builder()
                .add("id", sightId.toString())
                .add("name",nameText)
                .add("introduce",introduceText)
                .build();
        HttpUtil.sendOkHttpPostRequest("/spot/add", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddSpotActivity.this,responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {
                            //添加成功返回
                            AlertDialog.Builder dialog = new AlertDialog.Builder(AddSpotActivity.this);
                            dialog.setTitle("信息:");
                            dialog.setMessage("景点信息已保存，可以添加介绍语音！");
                            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddSpotActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    //文件路径解析
    @TargetApi(19)
    private String handleImageOnKitKat(Intent data) {
        String audioPath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Audio.Media._ID + "=" + id;
                audioPath = getAudioPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                audioPath = getAudioPath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            audioPath = getAudioPath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            audioPath = uri.getPath();
        }
        return audioPath; // 根据图片路径显示图片
    }

    private String handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String audioPath = getAudioPath(uri, null);
        return audioPath;
    }


    private String getAudioPath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor != null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                //path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
