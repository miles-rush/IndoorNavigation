package com.example.indoornavigation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bean.ResponseCode;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.example.tool.MusicService;

import java.io.File;
import java.io.IOException;

public class AddSpotActivity extends AppCompatActivity {
    private ImageView back;
    private ImageView done;
    private ImageView openFile;
    private ImageView recordVoice;
    private ImageView location;

    private EditText name;
    private EditText coordinate;
    private EditText introduce;
    private EditText voicePath;

    private TextView showFilePath;

    private ImageView mediaStart;
    private ImageView mediaStop;
    private ImageView voiceUnDone;
    private ImageView voiceDone;
    private TextView mediaText;

    private Integer sightId;
    private Integer spotId = -1;

    private MusicService.PlayMusicBinder playMusicBinder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);
        init();

        //音乐播放服务
        Intent intent = new Intent(AddSpotActivity.this, MusicService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    //设置音乐路径
    private void initMusicPlay() {
        playMusicBinder.stop();
        playMusicBinder.init(audioPath);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playMusicBinder = (MusicService.PlayMusicBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void init() {
        back = findViewById(R.id.add_spot_back);
        done = findViewById(R.id.add_spot_done);

        openFile = findViewById(R.id.open_voice_file);
        recordVoice = findViewById(R.id.voice_in);

        name = findViewById(R.id.input_spot_name);
        coordinate = findViewById(R.id.input_spot_coordinate);
        introduce = findViewById(R.id.input_spot_introduce);
        voicePath = findViewById(R.id.music_path_text);

        showFilePath = findViewById(R.id.file_show_path);

        mediaStart = findViewById(R.id.voice_start);
        mediaStop = findViewById(R.id.voice_stop);
        voiceUnDone = findViewById(R.id.voice_undone);
        voiceDone = findViewById(R.id.voice_done);
        mediaText = findViewById(R.id.media_text);
        location = findViewById(R.id.add_spot_location);


        sightId = getIntent().getIntExtra("sightId",0);
        //跳转到定位界面
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!spotIdJudge()) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AddSpotActivity.this);
                    dialog.setTitle("信息:");
                    dialog.setMessage("请先保存当前景点信息！");
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }else {
                    //跳转
                    Intent intent = new Intent(AddSpotActivity.this,IndoorLocationActivity.class);
                    intent.putExtra("spotId",spotId);
                    intent.putExtra("sightId",sightId);
                    startActivity(intent);
                }
            }
        });

        //关闭当前页面
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //释放资源
                playMusicBinder.stop();
                finish();
            }
        });
        //新增景点信息
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSpot();
            }
        });
        //打开资源管理器 选择音频文件
        //显示底部操作栏
        openFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVoiceFile();
            }
        });
        //上传当前音频文件
        voiceDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beforeVoiceSave();//加一个选定文件后是否上传的选项
            }
        });
        //清除当前保存的音频路径 隐藏底部栏
        voiceUnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPath = null;
                showFilePath.setText("");

                voiceDone.setVisibility(View.INVISIBLE);
                voiceUnDone.setVisibility(View.INVISIBLE);
                mediaStart.setVisibility(View.INVISIBLE);
                mediaStop.setVisibility(View.INVISIBLE);
                mediaText.setText("");

                playMusicBinder.stop();
            }
        });

        //初始化音乐操作
        initMusicPlayButtons();

    }

    private void initMusicPlayButtons() {
        mediaStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusicBinder.start();
                mediaStart.setVisibility(View.INVISIBLE);
                mediaStop.setVisibility(View.VISIBLE);
                mediaText.setText("状态:播放中");
            }
        });

        mediaStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusicBinder.pause();
                mediaStop.setVisibility(View.INVISIBLE);
                mediaStart.setVisibility(View.VISIBLE);
                mediaText.setText("状态:暂停");
            }
        });
    }

    //打开文件管理器 上传语音资源
    public static final int CHOOSE_AUDIO = 1;
    private void openVoiceFile() {
        if (!spotIdJudge()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(AddSpotActivity.this);
            dialog.setTitle("信息:");
            dialog.setMessage("请先保存当前景点信息！");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent,CHOOSE_AUDIO);
        }
    }

    //资源管理器响应函数
    private String audioPath;
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_AUDIO:
                if (resultCode == RESULT_OK) {
                    String path = null;
                    if (Build.VERSION.SDK_INT >= 19) {
                        path = handleImageOnKitKat(data);
                    } else {
                        path = handleImageBeforeKitKat(data);
                    }
                    audioPath = path;
                    showFilePath.setText("路径:" + audioPath);
                    initMusicPlay();
                    showBottomTools(); //路径获取后 显示底部操作栏
                }
                break;
            default:
                break;
        }
    }
    //选定音频文件后 解放底部操作栏
    private void showBottomTools() {
        mediaStart.setVisibility(View.VISIBLE);
        voiceUnDone.setVisibility(View.VISIBLE);
        voiceDone.setVisibility(View.VISIBLE);
    }

    //当前景点未保存前 禁止上传语音
    private boolean spotIdJudge() {
        if (spotId == -1) {
            return false;
        }
        return true;
    }

    //语音上传的确认
    private void beforeVoiceSave() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(AddSpotActivity.this);
        dialog.setTitle("信息:");
        dialog.setMessage("是否上传该语音文件！");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveVoice();
            }
        });//在这里上传语音
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //调用上传音乐接口
    private void saveVoice() {
        File file = new File(audioPath);
        String audioFileName = voicePath.getText().toString().trim();
        if (audioFileName.equals("")) {
            audioFileName = "NO_NAME";
        }
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name",audioFileName)
                .addFormDataPart("id",spotId.toString())
                .addFormDataPart("file", audioFileName + ".mp3", RequestBody.create(MediaType.parse("audio/*"),file))
                .build();
        HttpUtil.sendOkHttpPostRequest("/voice/upload", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddSpotActivity.this,responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(AddSpotActivity.this);
                            dialog.setTitle("信息:");
                            dialog.setMessage("介绍语音上传关联成功！");
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

    //新增景点 返回景点的ID
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
                            //设置提交按钮不可见
                            done.setVisibility(View.INVISIBLE);
                            //添加成功返回
                            AlertDialog.Builder dialog = new AlertDialog.Builder(AddSpotActivity.this);
                            dialog.setTitle("信息:");
                            dialog.setMessage("景点信息已保存，可以添加其他信息！");
                            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                            //保存返回的ID 用于音频添加
                            spotId = responseCode.getAdditionalId();
                            name.setEnabled(false);
                            introduce.setEnabled(false);
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
