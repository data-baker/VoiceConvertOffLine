package com.baker.vc.offline.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baker.voiceconvert.offline.VoiceConvertOffLineManager;
import com.baker.voiceconvert.offline.callback.OffLineConvertCallBack;
import com.baker.voiceconvert.offline.util.ThreadPoolUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks  {
    //请填写您自己的clientId
    private final String clientId = "";
    //请填写您自己的clientSecret
    private final String clientSecret = "";

    private String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private String[] voiceName = new String[]{"Vc_luoli", "Vc_dashu", "Vc_gaoguai", "Vc_kongling", "Vc_bawanglong", "Vc_zhongjinshu"};
    private Spinner spinner;
    private ImageView imgRecord;
    private TextView tvResult;
    private Button btnSpeak;
    private VoiceConvertOffLineManager manager;
    /**
     * isFirst 的作用是控制第一次进页面，无原始录音音频，所以无法直接转换的情况。
     */
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("变声示例");

        //动态判断权限
        if (EasyPermissions.hasPermissions(this, permissions)) {
            initSdk();
        } else {
            //没有打开相关权限、申请权限
            EasyPermissions.requestPermissions(this, "需要获取您的录音和存储权限", 1, permissions);
        }

        initView();
    }

    /**
     * 初始化SDK，此方法尽量提前调用，给初始化留出一定时间。
     */
    private void initSdk() {
        manager = new VoiceConvertOffLineManager();
        manager.initVoiceConvertSDK(MainActivity.this, clientId, clientSecret, offLineConvertCallBack);
    }

    private OffLineConvertCallBack offLineConvertCallBack = new OffLineConvertCallBack() {
        @Override
        public void onReadyForConvert() {

        }

        @Override
        public void onResult(byte[] audioData, int endFlag) {

        }

        @Override
        public void onConvertCompleted() {
            if (manager != null) {
                manager.play();
            }
        }

        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onPlayCompleted() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void volumeChange(int volume) {
            Message message = Message.obtain();
            message.what = 1;
            message.obj = volume;
            handler.sendMessage(message);
        }

        @Override
        public void onError(String errorCode, String errorMsg) {
            Message message = Message.obtain();
            message.what = 2;
            message.obj = errorCode + ", " + errorMsg;
            handler.sendMessage(message);
        }
    };


    private void initView() {
        spinner = findViewById(R.id.spinner);
        //音色选择
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirst) {
                    return;
                }
                if (manager != null) {
                    //demo示例中是为了展示效果。故在选择完音色后实现了转换效果。真实需求中只需要完成声音选择设置即可。
                    manager.startConvertByPCM();

                    //设置声音音色
                    manager.setSpeakerId(voiceName[position]);

                    //开启音频流方式转换
                    sendData(manager.getOriginRecordFile());

                    //开启文件转换方式转换
//                    manager.convert(manager.getOriginRecordFile());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnSpeak = findViewById(R.id.btn_speak);
        btnSpeak.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (EasyPermissions.hasPermissions(MainActivity.this, permissions)) {
                    if (manager != null) {
                        isFirst = false;
                        //长按，开启SDK自带录音功能，实现录音、转换。结果见回调方法...
                        manager.startRecordAndConvert();
                    }
                    imgRecord.setVisibility(View.VISIBLE);
                } else {
                    //没有打开相关权限、申请权限
                    EasyPermissions.requestPermissions(MainActivity.this, "需要获取您的录音和存储权限", 1, permissions);
                }
                return true;
            }
        });

        btnSpeak.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (manager != null) {
                        //停止录音、转换。
                        manager.stopRecord();
                        imgRecord.setVisibility(View.INVISIBLE);
                    }
                }
                return false;
            }
        });
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toastText("长按说话");
            }
        });

        tvResult = findViewById(R.id.tv_result);
        tvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        imgRecord = findViewById(R.id.img_recording);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                //音量分贝UI展示
                changeVolumeImg((int) msg.obj);
            } else if (msg.what == 2) {
                //弹出提示信息
                toastText((String) msg.obj);
            }
        }
    };

    /**
     * 根据音量分贝展示UI，调用间隙是100ms
     *
     * @param volume
     */
    private void changeVolumeImg(int volume) {
        if (volume < 30) {
            imgRecord.setImageResource(R.mipmap.volume_1);
        } else if (volume < 40) {
            imgRecord.setImageResource(R.mipmap.volume_2);
        } else if (volume < 50) {
            imgRecord.setImageResource(R.mipmap.volume_3);
        } else if (volume < 60) {
            imgRecord.setImageResource(R.mipmap.volume_4);
        } else if (volume < 70) {
            imgRecord.setImageResource(R.mipmap.volume_5);
        } else if (volume < 80) {
            imgRecord.setImageResource(R.mipmap.volume_6);
        } else if (volume < 90) {
            imgRecord.setImageResource(R.mipmap.volume_7);
        } else {
            imgRecord.setImageResource(R.mipmap.volume_8);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //框架要求必须这么写
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //获得权限时
        initSdk();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        //拒绝权限时
        toastText("请同意相关权限，否则部分功能无法使用");
    }

    private void toastText(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 自增数据，默认值为-1.
     */
    private AtomicInteger mIdx = new AtomicInteger(-1);

    /**
     * 音频流转换示例，此处仅借用已有文件作为音频流来源而已。具体看方法内部数据发送逻辑。
     *
     * @param path
     */
    public void sendData(String path) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //设置默认值为-1；
                mIdx.set(-1);
                try {
                    //TODO 此处仅借用文件路径获取数据做展示而已，本方法内最重要的是manager.sendData这句代码。
                    File file = new File(path);
                    if (file.exists()) {
                        int readsize = 0;
                        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
                        byte[] audiodata;
                        FileInputStream inputStream = new FileInputStream(file);
                        while ((readsize = inputStream.read(audiodata = new byte[2048], 0, 2048)) != -1) {
                            if (readsize > 0) {
                                //每次自增1，从0开始发送数据。
                                mIdx.addAndGet(1);
                                if (manager != null) {
                                    manager.sendData(audiodata, mIdx.get());
                                }
                            }
                        }
                        //最后发一空包，表示最后一包。
                        mIdx.addAndGet(1);
                        if (manager != null) {
                            manager.sendData(null, mIdx.get() * -1);
                        }
                    } else {
                        Message message = Message.obtain();
                        message.what = 2;
                        message.obj = "当前无录音文件.";
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        ThreadPoolUtil.execute(runnable);
    }
}