package com.bgautam.videostreamer;

import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private static final String FILE_URL = "https://r2---sn-5jucgv5qc5oq-jwce.googlevideo.com/videoplayback?ratebypass=yes&expire=1496872298&lmt=1485241602738874&mime=video%2Fmp4&key=cms1&pl=24&ipbits=0&requiressl=yes&ei=CiE4WcvODNOG1gKk_4GABA&itag=18&dur=307.014&id=o-ABJFmsfYzmsYFMq7GkrUmkcJYC90y3JcAFxLRy7OTKb6&gir=yes&sparams=clen,dur,ei,expire,gir,id,ip,ipbits,ipbypass,itag,lmt,mime,mip,mm,mn,ms,mv,pcm2cms,pl,ratebypass,requiressl,source&source=youtube&clen=18697299&ip=159.253.144.86&signature=3C6A0C2A1D9530914E36D698AF9899F0E63565CF.08CF07702B9F969B4AA03D17E6BCD96CE8DAD352&title=Guzarish+%28Full+Song%29+Ghajini+feat.+Aamir+Khan&redirect_counter=1&req_id=ae86d92edbd3a3ee&cms_redirect=yes&ipbypass=yes&mip=49.205.140.24&mm=31&mn=sn-5jucgv5qc5oq-jwce&ms=au&mt=1496850643&mv=m&pcm2cms=yes";
    private static final String TAG = MainActivity.class.getName();
    private MediaPlayer mMediaPlayer;
    private LinearLayout mLinear_layout;
    private TextureView mTextureView;
    private ToggleButton mToggleButton;
    private Thread t;
    int length = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(this);
        mLinear_layout = (LinearLayout) findViewById(R.id.linear_layout);
        mToggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        mToggleButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            mToggleButton.setEnabled(true);
            mToggleButton.setClickable(true);
            adjustAspectRatio(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        Surface surface = new Surface(surfaceTexture);
        mLinear_layout.setVisibility(View.VISIBLE);

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(FILE_URL));
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setLooping(false);
            mToggleButton.setEnabled(true);
            mToggleButton.setClickable(true);

            mMediaPlayer.prepareAsync();

            // Play video when the media source is ready for playback.
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mLinear_layout.setVisibility(View.INVISIBLE);
                    mToggleButton.setVisibility(View.VISIBLE);
                    mediaPlayer.start();
                    adjustAspectRatio(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
                }
            });

            mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                    System.err.println("isChecked:" + isChecked);
                    if (isChecked) {
                        mMediaPlayer.pause();
                        length = mMediaPlayer.getCurrentPosition();
                    } else {
                        mMediaPlayer.seekTo(length);
                        mMediaPlayer.start();
                    }
                }

            });

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mToggleButton.setEnabled(false);
                    mToggleButton.setClickable(false);
                }
            });



        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.d(TAG, e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "Texture Size changed");

    }

    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);

        Matrix txform = new Matrix();
        mTextureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        mTextureView.setTransform(txform);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "Texture Destroyed");
        return false;
    }


    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.d(TAG, "Texture Updated");
    }
}
