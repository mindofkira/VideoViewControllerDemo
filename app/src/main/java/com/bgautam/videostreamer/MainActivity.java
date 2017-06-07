package com.bgautam.videostreamer;

import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
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

    private static final String FILE_URL = "http://www.androidbegin.com/tutorial/AndroidCommercial.3gp";
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
