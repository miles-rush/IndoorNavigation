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
import com.example.bean.Spot;
import com.example.bean.Voice;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.example.tool.MusicService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;

public class SpotManagerActivity extends AppCompatActivity {
    private ImageView back;
    private ImageView done;

    private ImageView voiceEdit;
    private ImageView openFile;

    private EditText name;
    private EditText coordinate;
    private EditText introduce;
    private EditText voiceName;

    private FloatingActionButton updateSpot;
    private FloatingActionButton deleteSpot;

    private ImageView mediaStart;
    private ImageView mediaStop;
    private ImageView voiceUnDone;
    private ImageView voiceDone;
    private TextView mediaText;
    private TextView showFilePath;

    private Integer spotId;
    private Spot spot;
    private Voice voice;

    private String voiceOnlineUrl = "";//网络音频地址
    private String localAudioPath = "";//本地音频地址
    private String nowPlayMusicPath = "";//当前播放音频地址


    private MusicService.PlayMusicBinder playMusicBinder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_manager);
        init();
        getSightInfo();

        //音乐播放服务
        Intent intent = new Intent(SpotManagerActivity.this, MusicService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
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
        spotId = getIntent().getIntExtra("spotId",0);
        back = findViewById(R.id.manager_spot_back);
        done = findViewById(R.id.manager_spot_done);

        name = findViewById(R.id.manager_spot_name);
        coordinate = findViewById(R.id.manager_spot_coordinate);
        introduce = findViewById(R.id.manager_spot_introduce);
        voiceName = findViewById(R.id.manager_music_path_text);

        updateSpot = findViewById(R.id.update_spot);
        deleteSpot = findViewById(R.id.delete_spot);

        voiceEdit = findViewById(R.id.manager_voice_edit);
        openFile = findViewById(R.id.manager_open_voice_file);

        mediaStart = findViewById(R.id.manager_voice_start);
        mediaStop = findViewById(R.id.manager_voice_stop);
        voiceUnDone = findViewById(R.id.manager_voice_undone);
        voiceDone = findViewById(R.id.manager_voice_done);
        mediaText = findViewById(R.id.manager_media_text);
        showFilePath = findViewById(R.id.manager_local_voice_path);

        //打开文件
        openFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVoiceFile();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusicBinder.stop();
                finish();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSpot();
            }
        });

        updateSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入可编辑状态
                name.setEnabled(true);
                introduce.setEnabled(true);
                done.setVisibility(View.VISIBLE);
            }
        });

        deleteSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSpot();
            }
        });

        //初始化音乐播放的开始和暂停
        initMusicPlayButtons();


        //底部栏 提交按钮 这里是更新或是上传音频 或是单独的音频绑定名称修改
        voiceDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SpotManagerActivity.this);
                dialog.setTitle("信息:");
                dialog.setMessage("是否保存本次编辑内容！");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //判断本次操作是更新还是上传
                        if (voiceOnlineUrl.equals("")) {
                            //无资源调用上传
                            addSpotVoice();
                        }else {
                            //有资源调用更新 没有添加本地资源 但是名字变动 调用名字更新接口
                            if (localAudioPath.equals("")) {
                                String newName = voiceName.getText().toString().trim();
                                if (!newName.equals(voice.getName())) {//只有名字发生了变化
                                    beforeUpdateVoiceName();
                                } else {
                                    updateSpotVoice();
                                }
                            }else {
                                updateSpotVoice();
                            }
                        }
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        //底部栏取消按钮
        voiceUnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseBottomTools();
            }
        });

        //编辑按钮事件
        voiceEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SpotManagerActivity.this);
                dialog.setTitle("信息:");
                dialog.setMessage("是否唤起工具栏,进入音频编辑状态！");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        voiceName.setEnabled(true);
                        openFile.setVisibility(View.VISIBLE);//打开文件可见
                        voiceEdit.setVisibility(View.GONE);//编辑按钮不可见 这里使用GONE去除占用空间
                        showBottomTools();
                        initMusicPlay();//进入编辑状态后 初始化播放器
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

    }

    private void beforeUpdateVoiceName() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SpotManagerActivity.this);
        dialog.setTitle("信息:");
        dialog.setMessage("是否更新该音频的命名！");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateVoiceName();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void updateVoiceName() {
        final RequestBody requestBody = new FormBody.Builder()
                .add("id",voice.getId().toString())
                .add("name",voiceName.getText().toString().trim())
                .build();
        HttpUtil.sendOkHttpPostRequest("/voice/updateName", requestBody,new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SpotManagerActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {
                            //todo
                            AlertDialog.Builder dialog = new AlertDialog.Builder(SpotManagerActivity.this);
                            dialog.setTitle("信息:");
                            dialog.setMessage("当前音频命名更新成功！");
                            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                            //更新当前存储的信息
                            getSightInfo();
                            releaseBottomTools();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SpotManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void releaseBottomTools() {
        //隐藏底部栏所有UI 释放音乐播放器资源
        voiceDone.setVisibility(View.INVISIBLE);
        voiceUnDone.setVisibility(View.INVISIBLE);
        mediaStart.setVisibility(View.INVISIBLE);
        mediaStop.setVisibility(View.INVISIBLE);
        mediaText.setText("");
        showFilePath.setText("");
        playMusicBinder.stop();

        //退出编辑状态时 打开文件按钮隐藏 编辑按钮重现
        openFile.setVisibility(View.GONE);
        voiceEdit.setVisibility(View.VISIBLE);

        voiceName.setEnabled(false);
    }

    private void showBottomTools() {
        mediaStart.setVisibility(View.VISIBLE);
        voiceUnDone.setVisibility(View.VISIBLE);
        voiceDone.setVisibility(View.VISIBLE);
    }

    private void initMusicPlayButtons() {
        mediaStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowPlayMusicPath.equals("")) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SpotManagerActivity.this);
                    dialog.setTitle("信息:");
                    dialog.setMessage("当前无音频文件！");
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else {
                    playMusicBinder.start();
                    mediaStart.setVisibility(View.INVISIBLE);
                    mediaStop.setVisibility(View.VISIBLE);
                    mediaText.setText("状态:播放中");
                }
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

    //调用更新接口
    private void updateSpot() {
        final RequestBody requestBody = new FormBody.Builder()
                .add("id",spotId.toString())
                .add("name",name.getText().toString().trim())
                .add("introduce",introduce.getText().toString().trim())
                .build();
        HttpUtil.sendOkHttpPostRequest("/spot/update", requestBody,new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SpotManagerActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {
                            //更新成功 隐藏按钮 设置不可编辑
                            name.setEnabled(false);
                            introduce.setEnabled(false);
                            done.setVisibility(View.INVISIBLE);
                            //更新当前存储的信息
                            getSightInfo();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SpotManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }
    //加载景区信息
    private void getSightInfo() {
        HttpUtil.sendOkHttpGetRequest("/spot/query?id=" + spotId, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                spot = GsonUtil.getSpotJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //基础信息的显示
                        name.setText(spot.getName());
                        introduce.setText(spot.getIntroduce());
                        if (spot.getVoices() != null) {
                            if (spot.getVoices().size() > 0) {
                                voice = spot.getVoices().get(0);
                                voiceName.setText(voice.getName());
                                voiceOnlineUrl = HttpUtil.RESOURCE_URL + voice.getResourcesPath();
                                nowPlayMusicPath = voiceOnlineUrl;//显示景点信息后 初始化音乐播发器中的URL为在线资源
                            }
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SpotManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSpot() {
        HttpUtil.sendOkHttpGetRequest("/spot/delete?id=" + spotId, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SpotManagerActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {//删除后关闭
                            playMusicBinder.stop();
                            finish();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SpotManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //唤起资源管理器后续逻辑
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
                    localAudioPath = path;
                    showFilePath.setText("路径:" + localAudioPath);
                    nowPlayMusicPath = localAudioPath;
                    initMusicPlay();
                    //showBottomTools(); //路径获取后 显示底部操作栏
                }
                break;
            default:
                break;
        }
    }

    //无音频时上传音频
    private void addSpotVoice() {
        if (localAudioPath.equals("")) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(SpotManagerActivity.this);
            dialog.setTitle("信息:");
            dialog.setMessage("当前未选择本地音频文件可上传！");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }else {
            File file = new File(localAudioPath);
            String audioFileName = voiceName.getText().toString().trim();
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
                            Toast.makeText(SpotManagerActivity.this,responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                            if (responseCode.getCode().equals("1")) {
                                AlertDialog.Builder dialog = new AlertDialog.Builder(SpotManagerActivity.this);
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
                            releaseBottomTools();
                            //刷新当前景点信息
                            getSightInfo();
                        }
                    });
                }
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SpotManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    //有音频时更新该景点下的音频
    private void updateSpotVoice() {
        //无选择本地文件时
        if (localAudioPath.equals("")) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(SpotManagerActivity.this);
            dialog.setTitle("信息:");
            dialog.setMessage("当前未选择本地音频文件可供更新！");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }else {
            File file = new File(localAudioPath);
            String audioFileName = voiceName.getText().toString().trim();
            if (audioFileName.equals("")) {
                audioFileName = "NO_NAME";
            }
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("name",audioFileName)
                    .addFormDataPart("id",voice.getId().toString())
                    .addFormDataPart("file", audioFileName + ".mp3", RequestBody.create(MediaType.parse("audio/*"),file))
                    .build();
            HttpUtil.sendOkHttpPostRequest("/voice/update", requestBody, new okhttp3.Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseData = response.body().string();
                    final ResponseCode responseCode = GsonUtil.getResponseJson(responseData);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SpotManagerActivity.this,responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                            if (responseCode.getCode().equals("1")) {
                                AlertDialog.Builder dialog = new AlertDialog.Builder(SpotManagerActivity.this);
                                dialog.setTitle("信息:");
                                dialog.setMessage("更新景点下音频文件成功！");
                                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                                releaseBottomTools();
                                //更新当前存储的信息
                                getSightInfo();
                            }
                        }
                    });
                }
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SpotManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        }
    }

    //初始化音乐播放器
    private void initMusicPlay() {
        playMusicBinder.stop();
        playMusicBinder.init(nowPlayMusicPath);
    }

    //打开文件管理器 上传语音资源
    public static final int CHOOSE_AUDIO = 1;
    private void openVoiceFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,CHOOSE_AUDIO);
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
