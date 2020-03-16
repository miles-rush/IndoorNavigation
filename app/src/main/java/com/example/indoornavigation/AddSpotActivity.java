package com.example.indoornavigation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.DialogInterface;
import android.os.Bundle;
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
                        if (responseCode.getCode() == "1") {
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
}
