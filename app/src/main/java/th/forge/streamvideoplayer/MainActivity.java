package th.forge.streamvideoplayer;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {

    private PlayerView playerView;
    private SimpleExoPlayer player;

    private DeviceType deviceType;
    private boolean playWhenReady = true;
    private static final String URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/TearsOfSteel.m3u8";

    private enum DeviceType {
        SUPPORTED_TABLET, SUPPORTED_SMARTPHONE, UNSUPPORTED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerView = findViewById(R.id.video_view);
        getDeviceType();
        switch (deviceType) {
            case UNSUPPORTED:
                playWhenReady = false;
                playerView.setVisibility(View.GONE);
                showAlertDialog();
                break;
            case SUPPORTED_SMARTPHONE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    private void getDeviceType() {
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float screenW = dm.widthPixels;
        float screenH = dm.heightPixels;
        if (isTablet && ((screenW >= 800 && screenH >= 800))) {
            deviceType = DeviceType.SUPPORTED_TABLET;
        } else if (!isTablet && (screenW >= 1080 && screenH >= 1080)) {
            deviceType = DeviceType.SUPPORTED_SMARTPHONE;
        } else {
            deviceType = DeviceType.UNSUPPORTED;
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle(getResources().getString(R.string.ad_title));
        adb.setMessage(getResources().getString(R.string.ad_body));
        adb.setPositiveButton(getResources().getString(R.string.OK), (dialog, i) -> {
            dialog.dismiss();
            finish();
        });
        AlertDialog ad = adb.create();
        ad.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        playerInit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(playWhenReady);
        }
    }

    private void playerInit() {
        TrackSelection.Factory adaptiveTrackSelection = new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(adaptiveTrackSelection),
                new DefaultLoadControl()
        );
        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
                super.onTimelineChanged(timeline, manifest, reason);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                super.onPlayerStateChanged(playWhenReady, playbackState);
            }

        });
        playerView.setPlayer(player);

        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getResources().getString(R.string.app_name)), defaultBandwidthMeter);

        Uri uri = Uri.parse(URL);

        MediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
        player.prepare(mediaSource);
        playerView.requestFocus();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

}
