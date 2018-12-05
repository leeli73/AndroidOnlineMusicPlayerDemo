package com.example.leeli.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private int NowMusic;
    private Boolean isFirst = true;
    private Button Start;
    private Button Stop;
    private Button Last;
    private Button Next;
    private ListView MusicList;
    private BaseAdapter adapter;
    private LocalBroadcastManager MusicListBroad;
    private List<MusicInfo> AllMusic = new ArrayList<MusicInfo>();
    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitData();
        InitList();
        Start = (Button)findViewById(R.id.Start);
        Stop = (Button)findViewById(R.id.Stop);
        Last = (Button)findViewById(R.id.Last);
        Next = (Button)findViewById(R.id.Next);
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Start.getText().toString().equals("开始"))
                {
                    if(isFirst)
                    {
                        PlayMusic(NowMusic);
                        isFirst = false;
                    }
                    else
                    {
                        mediaPlayer.start();
                    }
                    Start.setText("暂停");
                    Toast.makeText(MainActivity.this, "开始播放:"+AllMusic.get(NowMusic).Name, Toast.LENGTH_LONG).show();
                }
                else if(Start.getText().toString().equals("暂停"))
                {
                    mediaPlayer.pause();
                    Start.setText("开始");
                    Toast.makeText(MainActivity.this, "暂停播放:"+AllMusic.get(NowMusic).Name, Toast.LENGTH_LONG).show();
                }
            }
        });
        Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                Start.setText("开始");
                Toast.makeText(MainActivity.this, "停止播放:"+AllMusic.get(NowMusic).Name, Toast.LENGTH_LONG).show();
            }
        });
        Last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NowMusic = NowMusic - 1;
                if(NowMusic < 0)
                {
                    NowMusic = AllMusic.size()-1;
                }
                PlayMusic(NowMusic);
                Toast.makeText(MainActivity.this, "上一曲:"+AllMusic.get(NowMusic).Name, Toast.LENGTH_LONG).show();
            }
        });
        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NowMusic = NowMusic + 1;
                if(NowMusic >= AllMusic.size())
                {
                    NowMusic = 0;
                }
                PlayMusic(NowMusic);
                Toast.makeText(MainActivity.this, "下一曲:"+AllMusic.get(NowMusic).Name, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void InitData()
    {
        NowMusic = 0;
        String url = "https://code-1251175805.cos.ap-chengdu.myqcloud.com/List.txt";
        final OkHttpClient okHttpClient=new OkHttpClient();
        final Request request=new Request.Builder().url(url).build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response=okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()){
                        String body=response.body().string();
                        Intent intent = new Intent("com.example.leeli.musicplayer");
                        intent.putExtra(Intent.EXTRA_TEXT,body);
                        MusicListBroad.sendBroadcast(intent);
                    }else {
                        Log.e("E", "run: "+response.code()+response.message());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void InitList()
    {
        IntentFilter filter = new IntentFilter("com.example.leeli.musicplayer");
        MusicListBroad = LocalBroadcastManager.getInstance(this);
        MusicListBroad.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String ListBody = intent.getStringExtra(Intent.EXTRA_TEXT);
                Log.i("list1",ListBody);
                String Item[] = ListBody.split("\r\n");
                for(int i=0;i<Item.length;i++)
                {
                    String temp[] = Item[i].split("@");
                    MusicInfo t = new MusicInfo();
                    t.Name = temp[0];
                    t.Url = temp[1];
                    t.Position = i;
                    AllMusic.add(t);
                }
                MusicList = (ListView) findViewById(R.id.MusicList);
                adapter = new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return AllMusic.size();
                    }

                    @Override
                    public Object getItem(int position) {
                        return null;
                    }

                    @Override
                    public long getItemId(int position) {
                        return 0;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView MusicItem = new TextView(MainActivity.this);
                        MusicItem.setText(AllMusic.get(position).Name);
                        MusicItem.setTextSize(30);
                        return MusicItem;
                    }
                };
                MusicList.setAdapter(adapter);
                MusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(MainActivity.this, "开始播放:"+AllMusic.get(position).Name, Toast.LENGTH_LONG).show();
                        PlayMusic(position);
                    }
                });
            }
        },filter);
    }
    private void PlayMusic(int position)
    {
        if(mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
        }
        try
        {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(AllMusic.get(position).Url);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Start.setText("暂停");
            NowMusic = position;
            isFirst = false;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
class MusicInfo
{
    public String Name;
    public String Url;
    public int Position;
}
