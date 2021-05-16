package com.prakriti.mediaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

// UI Thread - sequence of programmed instructions managed independently, Main Thread handles layout component interactions
// heavy operations to be done on separate threads to avoid thread blocking & crashes
    // too many new threads consume device resources

    private VideoView myVideo;
    private Button btnPlayVideo;
    private ImageButton btnPlayMusic, btnPauseMusic;

    private MediaController mediaController;
    private MediaPlayer mediaPlayer;

    private SeekBar sbAudioVolume, sbAudioPlayer;
    private AudioManager audioManager; // a system service

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myVideo = findViewById(R.id.myVideo);
        btnPlayVideo = findViewById(R.id.btnPlayVideo);
        btnPlayMusic = findViewById(R.id.btnPlayMusic);
        btnPauseMusic = findViewById(R.id.btnPauseMusic);

        btnPlayVideo.setOnClickListener(this);
        btnPlayMusic.setOnClickListener(this);
        btnPauseMusic.setOnClickListener(this);

        mediaController = new MediaController(this);
        mediaPlayer = MediaPlayer.create(this, R.raw.audio); // file name

        sbAudioVolume = findViewById(R.id.sbAudioVolume);
        sbAudioPlayer = findViewById(R.id.sbAudioPlayer);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE); // cast Service object to AudioManager
            // access max volume of device
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // stream type is Stream Music
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        sbAudioVolume.setMax(maxVolume); // set max value
        sbAudioVolume.setProgress(currentVolume); // set current value
            // add seekbar listener & notify for changes on seekbar

        sbAudioVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                   // Toast.makeText(MainActivity.this, "Volume: " + Integer.toString(progress), Toast.LENGTH_SHORT).show();
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbAudioPlayer.setOnSeekBarChangeListener(this);
        sbAudioPlayer.setMax(mediaPlayer.getDuration()); // set duration of music as max value of seekbar

            // responsible for the music
        mediaPlayer.setOnCompletionListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnPlayVideo:
                Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video); // file name
                // Uri of android.net package
                myVideo.setVideoURI(videoUri);
                myVideo.setMediaController(mediaController);
                mediaController.setAnchorView(myVideo);
                myVideo.start();
                break;

            case R.id.btnPlayMusic:
                mediaPlayer.start();
                timer = new Timer(); // initialising timer that exists throughout lifetime of program
                // now create new thread
                // seekbar is to be updated along with the music playing every second
                timer.scheduleAtFixedRate(new TimerTask() { // on new thread
                    @Override
                    public void run() {
                        // executed on Main thread, so update UI here
                        sbAudioPlayer.setProgress(mediaPlayer.getCurrentPosition());
                    }
                }, 0, 1000);
                // delay = 0 means starts timer as soon as button is tapped
                // period over which to execute run() task in ms
                break;
                // stop the new thread when not in use
                // cancel timer when music ends, implement OnCompletionListener for audio

            case R.id.btnPauseMusic:
                mediaPlayer.pause();
                timer.cancel();
                break;

        }
    }

    // works for sbAudioPlayer because MainActivity implements this listener; anonymous inner class works for sbAudioVolume
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // we use a timer on a new thread, & send responses to UI thread
        // seekbar is to be updated along with the music playing every second
        if(fromUser) {
            // upon interaction with seekbar
            mediaPlayer.seekTo(progress);
                // play the audio file at specified progress time
        }
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mediaPlayer.pause();
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mediaPlayer.start();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        timer.cancel();
        // thread doesn't need to run when not in use
    }
}