package th.forge.streamvideoplayer;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity implements Player.EventListener {

    private PlayerView playerView;
    private SimpleExoPlayer player;

    private DeviceType deviceType;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private static final String URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/TearsOfSteel.m3u8";

    private enum DeviceType {
        SUPPORTED_TABLET, SUPPORTED_SMARTPHONE, UNSUPPORTED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean allowLand = getResources().getBoolean(R.bool.allowLand);
        boolean allowPort = getResources().getBoolean(R.bool.allowPort);
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        Configuration configuration = getResources().getConfiguration();
        int smallestScreenWidthDp = configuration.smallestScreenWidthDp;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float screenW = dm.widthPixels;
        float screenH = dm.heightPixels;
        float density = dm.density;
        float densDpi = dm.densityDpi;

        /*if (allowLand && (screenW < 1080 || screenH < 1080)) {
            playWhenReady = false;
            buildErrorAlertDialog();
        }
        if (allowLand && !allowPort) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }*/
        isDeviceSupported();
        switch (deviceType) {
            case UNSUPPORTED:
                playWhenReady = false;
                buildErrorAlertDialog();
                break;
            case SUPPORTED_SMARTPHONE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.text_view);
        playerView = findViewById(R.id.video_view);
//

        /*if(screenH < 1080|| screenW <1080){
            throw new IllegalStateException();
        }*/
        tv.setText(String.valueOf(smallestScreenWidthDp) + "\n" + screenW + " " + screenH + " " + density + " " + densDpi);

    }

    private void isDeviceSupported() {
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        Configuration configuration = getResources().getConfiguration();
        int smallestScreenWidthDp = configuration.smallestScreenWidthDp;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float screenW = dm.widthPixels;
        float screenH = dm.heightPixels;
        float density = dm.density;
        float densDpi = dm.densityDpi;
        if (isTablet && ((screenW >= 800 && screenH >= 800))) {
            deviceType = DeviceType.SUPPORTED_TABLET;
//            return true;
        } else if (!isTablet && (screenW >= 1080 && screenH >= 1080)) {
            deviceType = DeviceType.SUPPORTED_SMARTPHONE;
//            return true;
        } else {
            deviceType = DeviceType.UNSUPPORTED;
        }
//        return false;
    }

    private void buildErrorAlertDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("Unsupported Device");
        adb.setMessage("Allowed Tablets 1280x800, 1920x1200 and SmartPhones 1920x1080");
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog ad = adb.create();
        ad.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                Util.getUserAgent(this, "Stream Video Player"), defaultBandwidthMeter);

        Uri uri = Uri.parse(URL);

        MediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
        player.prepare(mediaSource);
        playerView.requestFocus();
        player.setPlayWhenReady(playWhenReady);
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

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }
}
