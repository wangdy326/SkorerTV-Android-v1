package com.skorer.tv;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.comscore.analytics.comScore;
import com.milliyet.tv.utilities.ApplicationUtilities;
import com.milliyet.tv.utilities.FavouritesUtilities;
import com.milliyet.tv.utilities.GoogleAnalyticsUtilities;
import com.milliyet.tv.utilities.VideoClipUtilities;
import com.milliyet.tv.utilities.VideoListUtilities;
import com.mobilike.preroll.PreRollManager;
import com.skorer.tv.R;
import com.skorer.tv.listeners.OnJobDoneListener;
import com.skorer.tv.model.VideoClip;

public class VideoClipActivity extends ActionBarActivity {
	class Status {
		public static final int MINT_FRESH = 0;
		public static final int LOADEDMETADATA = 4;
		public static final int PLAYING = 2;
		public static final int PAUSED = 3;
		public static final int FINISHED = 1;
	}

	static class MediaControllerHolder {
		View mediaController = null;
		MediaControllerFooterHolder footerHolder = new MediaControllerFooterHolder();
		ImageView playButton = null;
		View progressView = null;

		static class MediaControllerFooterHolder {
			SeekBar seekBar = null;
			TextView timeTextView = null;
			View fullscreenButton = null;
			View footerView = null;
		}

		public MediaControllerHolder(Activity activity, int controllerResourceId) {
			if (activity != null) {
				this.mediaController = activity
						.findViewById(controllerResourceId);

				if (this.mediaController != null) {
					this.playButton = (ImageView) mediaController
							.findViewById(R.id.videoclip_controller_playbutton);

					this.footerHolder.footerView = mediaController
							.findViewById(R.id.videoclip_controller_footer);
					this.footerHolder.fullscreenButton = mediaController
							.findViewById(R.id.videoclip_controller_fullscreenbutton);
					this.footerHolder.timeTextView = (TextView) mediaController
							.findViewById(R.id.videoclip_controller_timetextview);
					this.footerHolder.seekBar = (SeekBar) mediaController
							.findViewById(R.id.videoclip_controller_seekbar);
					this.progressView = mediaController
							.findViewById(R.id.videoclip_controller_progressview);
				}
			}
		}
	}

	/***************************************
	 * Variables
	 */

	public static final boolean LOG = true;
	public static final String TAG = "VideoClipActivity";

	public static String TRACK_LABEL = "Video Detay";
	public boolean isVotedlike=false;
	public boolean isVotedDislike=false;
	public static String BUNDLE_PREROLLURL_KEY = "videoclipactivity.prerollurl";
	public static String BUNDLE_VIDEOCLIP_KEY = "videoclipactivity.videoclip";
	public static String SAVEDINSTANCE_PREROLLSTATE_KEY = "videoclipactivity.state.preroll";
	public static String SAVEDINSTANCE_VIDEOCLIP_KEY = "videoclipactivity.state.videoclip";

	private static final int UPDATEPERIOD_TIMEUI = 1000 / 60; // 60 frames/sec.

	private String videoClipId = null;
	private VideoClip videoClip = null;
	private WeakReference<SurfaceView> surfaceViewWeak = null;
	private WeakReference<View> videoContainerWeak = null;
	private WeakReference<View> footerViewWeak = null;
	private WeakReference<TextView> titleTextViewWeak = null;
	private WeakReference<TextView> descriptionTextViewWeak = null;
	private WeakReference<TextView> publishDateTextViewWeak = null;
	private WeakReference<TextView> impressionCountTextViewWeak = null;
	private WeakReference<LinearLayout> videoClipRelatedContainerWeak = null;
	private WeakReference<View> likeButtonWeak = null;
	private WeakReference<View> dislikeButtonWeak = null;
	private MediaControllerHolder mediaControllerHolder = null;
	private WeakReference<ImageView> dislikeImageWeak = null;
	private WeakReference<ImageView> likeImageWeak = null;
	private WeakReference<NetworkImageView> videoThumbnailImageViewWeak = null;

	private int currentBufferPercentage = 0;
	private int status = Status.MINT_FRESH;
	private Handler handler = null;
	private boolean timeTrackEnabled = false;
	private boolean preRollPresented = false;
	private String preRollUrl = null;

	private boolean surfaceAvailable = false;
	private boolean pendingPlayRequest = false;

	private MediaPlayer mediaPlayer = null;

	MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			if (LOG) {
				Log.d(TAG, "Info received but wtf!");
			}

			return false;
		}
	};

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			if (LOG) {
				Log.d(TAG, "Video size changed!");
			}
		}
	};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			if (LOG) {
				Log.d(TAG, "Media player prepared!");
			}

			VideoClipActivity activity = VideoClipActivity.this;

			if (ApplicationUtilities.sharedInstance().isActivityAlive(activity)) {
				activity.status = Status.PLAYING;

				if (mediaControllerHolder.progressView != null) {
					mediaControllerHolder.progressView.setVisibility(View.GONE);
				}

				View surfaceView = activity.getSurfaceView();

				if (surfaceView != null) {
					surfaceView
							.setOnClickListener(mediaControllerOnClickListener);
				}

				// activity.getSurfaceView().requestFocus();
				activity.resumeVideo(getSurfaceView());
			}
		}
	};

	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			if (LOG) {
				Log.d(TAG, "Media player completed!");
			}

			VideoClipActivity activity = VideoClipActivity.this;

			if (ApplicationUtilities.sharedInstance().isActivityAlive(activity)) {
				if (activity.mediaControllerHolder.playButton != null) {
					activity.mediaControllerHolder.playButton
							.setImageResource(R.drawable.play);
					activity.mediaControllerHolder.playButton
							.setVisibility(View.VISIBLE);
				}

				activity.status = Status.FINISHED;
			}
		}
	};

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			Log.d(TAG, "Error: " + framework_err + "," + impl_err);

			showStatusView("Video şu anda mevcut değil", false,
					"Diğer videolara göz atın", new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							finish();
						}
					}, false);

			return true;
		}
	};

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			currentBufferPercentage = percent;

			if (LOG) {
				Log.d(TAG, "Buffered: " + currentBufferPercentage);
			}

			VideoClipActivity activity = VideoClipActivity.this;

			if (ApplicationUtilities.sharedInstance().isActivityAlive(activity)) {
				activity.updateBufferPercentage(percent);
			}
		}
	};

	private View.OnClickListener mediaControllerOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "Clicked media controller");

			VideoClipActivity activity = VideoClipActivity.this;

			if (ApplicationUtilities.sharedInstance().isActivityAlive(activity)) {
				activity.toggleMediaControllers();
			}
		}
	};

	private View.OnClickListener playButtonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "Clicked play");

			synchronized (this) {
				VideoClipActivity activity = VideoClipActivity.this;

				if (ApplicationUtilities.sharedInstance().isActivityAlive(
						activity)) {
					if (preRollPresented) {
						switch (activity.status) {
						case Status.PLAYING: {
							activity.pauseVideo();

							break;
						}
						case Status.PAUSED: {
							activity.resumeVideo(activity.getSurfaceView());

							break;
						}
						case Status.FINISHED: {
							activity.restartVideo(activity.getSurfaceView());

							break;
						}
						case Status.LOADEDMETADATA: {
							activity.playVideo(activity.getSurfaceView(),
									activity.videoClip.getVideoUrl());
							// activity.playVideo(getSurfaceView(),
							// "http://video.milliyet.com.tr/d/h/IosMobile.ashx?VideoCode="
							// + activity.videoClip.getCode());

							break;
						}
						case Status.MINT_FRESH:
						default: {
							// No action defined
							Log.d(TAG,
									"No action defined for the play button, yet!");

							break;
						}
						}
					} else {
						// Try presenting PreRoll
						PreRollManager.sharedInstance().showPreRoll(activity,
								preRollUrl);

						// Assume pre-roll presented
						preRollPresented = true;
					}
				}
			}
		}
	};

	private View.OnClickListener fullscreenButtonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "Clicked full screen");

			VideoClipActivity activity = VideoClipActivity.this;

			if (ApplicationUtilities.sharedInstance().isActivityAlive(activity)) {
				activity.toggleFullscreen();
			}
		}
	};

	private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Log.d(TAG, "Seek ended");

			VideoClipActivity activity = VideoClipActivity.this;

			if (ApplicationUtilities.sharedInstance().isActivityAlive(activity)) {
				activity.resumeVideo(activity.getSurfaceView());
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.d(TAG, "Seek started");

			VideoClipActivity activity = VideoClipActivity.this;

			if (ApplicationUtilities.sharedInstance().isActivityAlive(activity)) {
				activity.pauseVideo();
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser) {
				VideoClipActivity activity = VideoClipActivity.this;

				if (ApplicationUtilities.sharedInstance().isActivityAlive(
						activity)) {
					activity.seekVideo(progress);

					if (activity.mediaPlayer != null) {
						int position = activity.mediaPlayer
								.getCurrentPosition();

						activity.updateTimeUI(position, true, false);
					}
				}
			}
		}
	};

	private Runnable timeUpdater = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "Will check time");

			VideoClipActivity activity = VideoClipActivity.this;

			if (ApplicationUtilities.sharedInstance().isActivityAlive(activity)
					&& activity.timeTrackEnabled) {
				activity.updateTimeUI();

				activity.handler.postDelayed(this, UPDATEPERIOD_TIMEUI);
			}
		}
	};

	private View.OnClickListener likeButtonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (LOG) {
				Log.d(TAG, "Clicked like button!");
			}
			if(!isVotedlike){
			VideoClipActivity activity = VideoClipActivity.this;

			if (ApplicationUtilities.sharedInstance().isActivityAlive(activity)
					&& activity.videoClip != null) {
				VideoClipUtilities.sharedInstance().voteVideoClipAsynch(
						activity.videoClip.getId(), true,
						SkorerTVApplication.requestQueue,
						new OnJobDoneListener<String>() {
							@Override
							public void onJobDone(int status,
									final String result) {
								final VideoClipActivity activity = VideoClipActivity.this;

								if (ApplicationUtilities.sharedInstance()
										.isActivityAlive(activity)) {
									switch (status) {
									case JobStatus.SUCCEED: {
										activity.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Toast.makeText(
														activity,
														"Oyunuz başarıyla kaydedilmiştir",
														Toast.LENGTH_SHORT)
														.show();
												isVotedlike=true;
												isVotedDislike=false;
												dislikeImageWeak.get().setImageResource(R.drawable.unlike);
												likeImageWeak.get().setImageResource(R.drawable.like_red);
											}
										});

										break;
									}
									default: {
										activity.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Toast.makeText(
														activity,
														"Oyunuz başarısız oldu. Birazdan tekrar deneyin.",
														Toast.LENGTH_SHORT)
														.show();
											}
										});

										break;
									}
									}
								}
							}
						});
			}
			}
		}
	};

	private View.OnClickListener dislikeButtonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (LOG) {
				Log.d(TAG, "Clicked dislike button!");
			}
			if(!isVotedDislike){
			VideoClipActivity activity = VideoClipActivity.this;

			if (ApplicationUtilities.sharedInstance().isActivityAlive(activity)
					&& activity.videoClip != null) {
				VideoClipUtilities.sharedInstance().voteVideoClipAsynch(
						activity.videoClip.getId(), false,
						SkorerTVApplication.requestQueue,
						new OnJobDoneListener<String>() {
							@Override
							public void onJobDone(int status,
									final String result) {
								final VideoClipActivity activity = VideoClipActivity.this;

								if (ApplicationUtilities.sharedInstance()
										.isActivityAlive(activity)) {
									switch (status) {
									case JobStatus.SUCCEED: {
										activity.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Toast.makeText(
														activity,
														"Oyunuz başarıyla kaydedilmiştir",
														Toast.LENGTH_SHORT)
														.show();
												isVotedDislike=true;
												isVotedlike=false;
												dislikeImageWeak.get().setImageResource(R.drawable.unlike_red);
												likeImageWeak.get().setImageResource(R.drawable.like);
											}
										});

										break;
									}
									default: {
										activity.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Toast.makeText(
														activity,
														"Oyunuz başarısız oldu. Birazdan tekrar deneyin.",
														Toast.LENGTH_SHORT)
														.show();
											}
										});

										break;
									}
									}
								}
							}
						});
			}
			}
		}
	};

	/***************************************
	 * Activity state handlers
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videoclip);
		
		comScore.setAppName("Skorer TV");
		comScore.setAppContext(this.getApplicationContext());

		// enable ActionBar app icon to behave as action to navigate up button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		// Harvest variables
		this.videoClipId = getIntent().getExtras().getString(
				BUNDLE_VIDEOCLIP_KEY);
		this.preRollUrl = getIntent().getExtras().getString(
				BUNDLE_PREROLLURL_KEY);

		setSurfaceView((SurfaceView) findViewById(R.id.videoclip_video_surfaceview));
		setVideoContainer(findViewById(R.id.videoclip_videocontainer));
		setFooterView(findViewById(R.id.videoclip_footer));
		setTitleTextView((TextView) findViewById(R.id.videoclip_titletextview));
		setDescriptionTextView((TextView) findViewById(R.id.videoclip_descriptiontextview));
		setPublishDateTextView((TextView) findViewById(R.id.videoclip_publishdatetextview));
		setImpressionCountTextView((TextView) findViewById(R.id.videoclip_impressioncounttextview));
		setLikeButton(findViewById(R.id.videoclip_likebutton));
		setDislikeButton(findViewById(R.id.videoclip_dislikebutton));
		setVideoClipRelatedContainer((LinearLayout) findViewById(R.id.videoclip_relatedvideocontainer));
		setVideoThumbnailImageView((NetworkImageView) findViewById(R.id.videoclip_video_thumbnail));
		this.mediaControllerHolder = new MediaControllerHolder(this,
				R.id.videoclip_video_controller);

		// Restore instance state
		if (savedInstanceState != null) {
			this.videoClip = savedInstanceState
					.getParcelable(SAVEDINSTANCE_VIDEOCLIP_KEY);
			this.preRollPresented = savedInstanceState
					.getBoolean(SAVEDINSTANCE_PREROLLSTATE_KEY);
		}

		// // Assign event handlers
		// SurfaceView
		SurfaceView surfaceView = getSurfaceView();
		if (surfaceView != null) {
			surfaceView.setKeepScreenOn(true);
			surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
				public void surfaceDestroyed(SurfaceHolder holder) {
					VideoClipActivity activity = VideoClipActivity.this;

					if (ApplicationUtilities.sharedInstance().isActivityAlive(
							activity)) {
						// Mark as surface not available
						activity.surfaceAvailable = false;
						
//						Toast.makeText(activity, "Surface destroyed", Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					final VideoClipActivity activity = VideoClipActivity.this;

					if (ApplicationUtilities.sharedInstance().isActivityAlive(
							activity)) {
						// Mark as surface available
						activity.surfaceAvailable = true;
						
//						Toast.makeText(activity, "Surface created", Toast.LENGTH_SHORT).show();

						if (activity.mediaPlayer != null) {
							activity.mediaPlayer.setDisplay(holder);

							// If there exist pending play request
							if (activity.pendingPlayRequest) {
								// Clear pending flag
								activity.pendingPlayRequest = false;

								// Play video
								activity.playVideo(getSurfaceView(),
										activity.videoClip.getVideoUrl());
							}
						}
						else
						{
							// Just hope that we queued operation on Looper after onCreate()
							activity.runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									// If there exist pending play request
									if (activity.pendingPlayRequest) {
										// Clear pending flag
										activity.pendingPlayRequest = false;

										// Play video
										activity.playVideo(getSurfaceView(),
												activity.videoClip.getVideoUrl());
									}
								}
							});
						}
					}
				}

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format,
						int width, int height) {
				}
			});
		}

		// Play button
		if (mediaControllerHolder.playButton != null) {
			mediaControllerHolder.playButton
					.setOnClickListener(playButtonOnClickListener);
		}

		// Full-screen button
		if (mediaControllerHolder.footerHolder.fullscreenButton != null) {
			mediaControllerHolder.footerHolder.fullscreenButton
					.setOnClickListener(fullscreenButtonOnClickListener);
		}

		// SeekBar
		if (mediaControllerHolder.footerHolder.seekBar != null) {
			mediaControllerHolder.footerHolder.seekBar
					.setOnSeekBarChangeListener(onSeekBarChangeListener);
		}

		// Like button
		View likeButton = getLikeButton();
		if (likeButton != null) {
			likeImageWeak=new WeakReference<ImageView>((ImageView) likeButton.findViewById(R.id.votebutton_imageview));
			likeImageWeak.get().setImageResource(R.drawable.like);
			likeButton.setOnClickListener(likeButtonOnClickListener);
		}

		// Dislike button
		View dislikeButton = getDislikeButton();
		if (dislikeButton != null) {
			dislikeImageWeak=new WeakReference<ImageView>((ImageView) dislikeButton.findViewById(R.id.votebutton_imageview));
			dislikeImageWeak.get().setImageResource(R.drawable.unlike);
			dislikeButton.setOnClickListener(dislikeButtonOnClickListener);
		}

		displayOrientationUI(getResources().getConfiguration().orientation);

		// Clean title (will be replaced with video title once initialized???!)
		setTitle("");

		// Initialize new handler
		this.handler = new Handler();

		GoogleAnalyticsUtilities.sharedInstance(this).onCreate(TRACK_LABEL,
				savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();

		showStatusView(null, true, null, null, false);

		loadVideoMetadata();

		GoogleAnalyticsUtilities.sharedInstance(this).onStart(TRACK_LABEL);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (status == Status.PLAYING) {
			resumeVideo(getSurfaceView());
		}
		comScore.onEnterForeground();
	}

	@Override
	protected void onPause() {
		super.onPause();

		pauseVideo();
		comScore.onExitForeground();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		stopVideo();
		releaseMedia();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(SAVEDINSTANCE_PREROLLSTATE_KEY, preRollPresented);
		outState.putParcelable(videoClipId, videoClip);

		GoogleAnalyticsUtilities.sharedInstance(this).onSaveInstanceState(
				outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.videoclip, menu);

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Defines a default (dummy) share intent to initialize the action provider.
	 * However, as soon as the actual content to be used in the intent is known
	 * or changes, you must update the share intent by again calling
	 * mShareActionProvider.setShareIntent()
	 */
	private Intent getShareIntent() {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(Intent.EXTRA_TEXT, generateShareText());
		intent.putExtra(Intent.EXTRA_TITLE, generateShareTitle());
		return intent;
	}

	private static final String SHARE_BASEURL = "http://www.milliyet.tv/video-izle/m-";

	private String generateShareText() {
		String tagSafeSpot = Html.fromHtml(videoClip.getSpot()).toString();

		StringBuffer buffer = new StringBuffer(tagSafeSpot);

		if (buffer.length() > 0) {
			buffer.append(" - ");
		}

		buffer.append(SHARE_BASEURL).append(videoClip.getCode())
				.append(".html");

		return buffer.toString();
	}

	private String generateShareTitle() {
		return videoClip.getTitle();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = false;

		switch (item.getItemId()) {
		case android.R.id.home: {
			// Navigate up!
			finish();

			handled = true;

			break;
		}
		case R.id.action_add_favourites: {
			if (FavouritesUtilities.sharedInstance().containsVideoId(this,
					videoClipId)) {
				if (FavouritesUtilities.sharedInstance().removeFavourite(this,
						videoClipId)) {
					Toast.makeText(this, "Favorilerden çıkarıldı",
							Toast.LENGTH_SHORT).show();

					supportInvalidateOptionsMenu();
				} else {
					Toast.makeText(this,
							"Favorilerden çıkartırken bir sorun oluştu",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				if (FavouritesUtilities.sharedInstance().addFavourite(this,
						this.videoClip.getId())) {
					Toast.makeText(this, "Favorilere eklendi",
							Toast.LENGTH_SHORT).show();

					supportInvalidateOptionsMenu();
				} else {
					Toast.makeText(this,
							"Favorilere eklerken bir sorun oluştu",
							Toast.LENGTH_SHORT).show();
				}
			}

			break;
		}
		default: {
			handled = super.onOptionsItemSelected(item);
			break;
		}
		}

		return handled;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem shareItem = menu.findItem(R.id.action_share);

		if (shareItem != null) {
			if (this.videoClip == null) {
				shareItem.setVisible(false);
			} else {
				// Set up ShareActionProvider's default share intent
				ShareActionProvider provider = (ShareActionProvider) MenuItemCompat
						.getActionProvider(shareItem);
				provider.setShareIntent(getShareIntent());

				shareItem.setVisible(true);
			}
		}

		MenuItem favouriteItem = menu.findItem(R.id.action_add_favourites);

		if (favouriteItem != null) {
			favouriteItem.setVisible(this.videoClip != null);

			if (FavouritesUtilities.sharedInstance().containsVideoId(this,
					videoClipId)) {
				favouriteItem.setIcon(R.drawable.favourites_remove);
			} else {
				favouriteItem.setIcon(R.drawable.favourites_add);
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		displayOrientationUI(newConfig.orientation);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			android.content.Intent data) {
		switch (requestCode) {
		case PreRollManager.REQUESTCODE_PREROLLACTIVITY: {
			if (surfaceAvailable) {
				// Play video
				playVideo(getSurfaceView(), videoClip.getVideoUrl());
			} else {
				this.pendingPlayRequest = true;
				
//				Toast.makeText(this, "Queued pending play request", Toast.LENGTH_SHORT).show();
			}

			break;
		}
		default: {
			super.onActivityResult(requestCode, resultCode, data);

			break;
		}
		}
	}

	private void displayOrientationUI(int orientation) {
		switch (orientation) {
		case Configuration.ORIENTATION_LANDSCAPE: {
			// Hide non-video content
			View footerView = getFooterView();

			if (footerView != null) {
				footerView.setVisibility(View.GONE);
			}

			View videoContainer = getVideoContainer();

			if (videoContainer != null) {
				videoContainer.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
			}

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

			getSupportActionBar().hide();

			Log.d("TAG", "Orientation changed to landscape");

			break;
		}
		case Configuration.ORIENTATION_PORTRAIT:
		default: {
			// Show non-video content
			View footerView = getFooterView();

			if (footerView != null) {
				footerView.setVisibility(View.VISIBLE);
			}

			View videoContainer = getVideoContainer();

			if (videoContainer != null) {
				int height = (int) ApplicationUtilities.sharedInstance()
						.convertDpToPixel(240, this);

				videoContainer.getLayoutParams().height = height;
			}

			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

			getSupportActionBar().show();

			Log.d("TAG", "Orientation changed to portrait");

			break;
		}
		}
	}

	private void loadVideoMetadata() {
		VideoClipUtilities.sharedInstance().fetchVideoClipAsynch(videoClipId,
				SkorerTVApplication.requestQueue,
				new OnJobDoneListener<VideoClip>() {
					@Override
					public void onJobDone(final int status,
							final VideoClip result) {
						// If invalid VideoView instance provided
						if (result == null) {
							showStatusView(
									"Video yüklenirken bir sorun oluştu",
									false, "Tekrar dene",
									new View.OnClickListener() {

										@Override
										public void onClick(View v) {
											loadVideoMetadata();
										}
									}, false);
						} else {
							final VideoClipActivity activity = VideoClipActivity.this;

							if (ApplicationUtilities.sharedInstance()
									.isActivityAlive(activity)) {
								activity.videoClip = result;

								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// // Fill contents of UI components

										// // Title
										TextView titleTextView = activity
												.getTitleTextView();
										// If found TextView instance of title
										if (titleTextView != null) {
											// Update text with VideoClip's
											// title
											Spanned spannedContent = Html
													.fromHtml(activity.videoClip
															.getTitle());
											titleTextView.setText(
													spannedContent,
													BufferType.SPANNABLE);
										}

										// // Description
										TextView descriptionTextView = activity
												.getDescriptionTextView();
										// If found TextView instance of
										// description
										if (descriptionTextView != null) {
											// Update text with VideClip's
											// description
											Spanned spannedContent = Html
													.fromHtml(activity.videoClip
															.getSpot());
											descriptionTextView.setText(
													spannedContent,
													BufferType.SPANNABLE);
										}

										// Publish date
										TextView publishDateTextView = activity
												.getPublishDateTextView();

										if (publishDateTextView != null) {
											// Create an instance of
											// SimpleDateFormat used for
											// formatting
											// the string representation of date
											// (month/day/year)
											DateFormat df = new SimpleDateFormat(
													"MM.dd.yyyy HH:mm",
													new Locale("tr", "TR"));

											// Using DateFormat format method we
											// can create a string
											// representation of a date with the
											// defined format.
											String publishDateString = df
													.format(activity.videoClip
															.getPublishTime());

											publishDateTextView
													.setText(publishDateString);
										}

										// Impression count
										TextView impressionCountTextView = activity
												.getImpressionCountTextView();

										if (impressionCountTextView != null) {
											impressionCountTextView.setText(String
													.valueOf(activity.videoClip
															.getViewCount()));
										}

										// Positive vote count
										View likeButton = activity
												.getLikeButton();

										if (likeButton != null) {
											TextView voteTextView = (TextView) likeButton
													.findViewById(R.id.votebutton_textview);

											if (voteTextView != null) {
												voteTextView.setText(String
														.valueOf(activity.videoClip
																.getPositiveVoteCount()));
											}
										}

										// Negative vote count
										View dislikeButton = activity
												.getDislikeButton();

										if (dislikeButton != null) {
											TextView voteTextView = (TextView) dislikeButton
													.findViewById(R.id.votebutton_textview);

											if (voteTextView != null) {
												voteTextView.setText(String
														.valueOf(activity.videoClip
																.getNegativeVoteCount()));
											}
										}

										// // Media Controller
										// Display play button
										if (mediaControllerHolder.playButton != null) {
											mediaControllerHolder.playButton
													.setVisibility(View.VISIBLE);
										}
										
										// Video thumbnail
										NetworkImageView thumbnailImageView = getVideoThumbnailImageView();
										
										if(thumbnailImageView != null)
										{
											thumbnailImageView.setImageUrl(videoClip.getImageUrl(), SkorerTVApplication.imageLoader);
										}

										VideoClipUtilities
												.sharedInstance()
												.fetchVideoClipUrlAsynch(
														result.getCode(),
														SkorerTVApplication.requestQueue,
														new OnJobDoneListener<String>() {
															@Override
															public void onJobDone(
																	int status,
																	final String result) {
																final VideoClipActivity activity = VideoClipActivity.this;

																if (ApplicationUtilities
																		.sharedInstance()
																		.isActivityAlive(
																				activity)) {
																	activity.runOnUiThread(new Runnable() {
																		@Override
																		public void run() {
																			// Update
																			// activity
																			// status
																			activity.status = Status.LOADEDMETADATA;

																			// UI
																			// ready
																			// for
																			// action
																			// Hide
																			// progress
																			// view
																			activity.hideStatusView(true);

																			activity.supportInvalidateOptionsMenu();

																			// Update
																			// video
																			// url
																			activity.videoClip
																					.setVideoUrl(result);

																			// If
																			// preroll
																			// played
																			// Auto
																			// start
																			// content
																			if (activity.preRollPresented) {
																				activity.playVideo(
																						getSurfaceView(),
																						result);
																			}
																		}
																	});
																}
															}
														});
									}
								});
							} else {
								Log.d("VideoClipActivity",
										"Activity is rotten!");
							}
						}
					}
				});

		VideoListUtilities.fetchRelatedVideoClipsAsynch(videoClipId,
				SkorerTVApplication.requestQueue,
				new OnJobDoneListener<List<VideoClip>>() {
					@Override
					public void onJobDone(final int status,
							final List<VideoClip> result) {
						final VideoClipActivity activity = VideoClipActivity.this;

						if (ApplicationUtilities.sharedInstance()
								.isActivityAlive(activity)) {
							activity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									switch (status) {
									case JobStatus.SUCCEED: {
										if (result != null) {
											for (VideoClip videoClip : result) {
												addRelatedVideo(videoClip);
											}
										}

										break;
									}
									default:
										break;
									}
								}
							});
						}
					}
				});
	}

	private void playVideo(SurfaceView surfaceView, String videoUrl) {
		
		ImageView imageView = getVideoThumbnailImageView();
		
		if(imageView != null)
		{
			imageView.setVisibility(View.GONE);
		}
		
		if (mediaControllerHolder.playButton != null) {
			mediaControllerHolder.playButton.setVisibility(View.GONE);
		}

		if (mediaControllerHolder.progressView != null) {
			mediaControllerHolder.progressView.setVisibility(View.VISIBLE);
		}

		openVideo(videoUrl, surfaceView.getHolder());
	}

	private void pauseVideo() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			this.status = Status.PAUSED;

			if (this.mediaControllerHolder.playButton != null) {
				this.mediaControllerHolder.playButton
						.setImageResource(R.drawable.play);
				this.mediaControllerHolder.playButton
						.setVisibility(View.VISIBLE);
			}

			mediaPlayer.pause();
		}

		stopTrackingVideoTime();
	}

	private void resumeVideo(final SurfaceView surfaceView) {
		if (this.mediaPlayer != null && !mediaPlayer.isPlaying()) {
			this.status = Status.PLAYING;

			if (this.mediaControllerHolder.playButton != null) {
				this.mediaControllerHolder.playButton
						.setImageResource(R.drawable.pause);
				this.mediaControllerHolder.playButton
						.setVisibility(View.VISIBLE);
			}

			if (this.mediaControllerHolder.footerHolder.footerView != null) {
				this.mediaControllerHolder.footerHolder.footerView
						.setVisibility(View.VISIBLE);
			}

			this.mediaPlayer.setDisplay(surfaceView.getHolder());
			this.mediaPlayer.start();

			startTrackingVideoTime();
		}
	}

	private void restartVideo(final SurfaceView surfaceView) {
		if (this.mediaPlayer != null) {
			this.mediaPlayer.seekTo(0);
			resumeVideo(surfaceView);
		}
	}

	private void stopVideo() {
		if (this.mediaPlayer != null) {
			this.mediaPlayer.stop();
		}
	}

	private void seekVideo(final int percent) {
		if (this.mediaPlayer != null) {
			int duration = this.mediaPlayer.getDuration();
			int seekTo = (int) (duration * ((float) percent / 100.0f));

			this.mediaPlayer.seekTo(seekTo);
		}
	}

	private void openVideo(String uri, SurfaceHolder holder) {
		if (uri == null || holder == null) {
			// not ready for playback just yet, will try again later
			return;
		}

		// Tell the music playback service to pause
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		sendBroadcast(i);

		releaseMedia();
		try {
			mediaPlayer = new MediaPlayer();

			mediaPlayer.setOnPreparedListener(mPreparedListener);
			mediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mediaPlayer.setOnCompletionListener(mCompletionListener);
			mediaPlayer.setOnErrorListener(mErrorListener);
			mediaPlayer.setOnInfoListener(mInfoListener);
			mediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			currentBufferPercentage = 0;
			mediaPlayer.setDataSource(uri);
			mediaPlayer.setDisplay(holder);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setScreenOnWhilePlaying(true);
			mediaPlayer.prepareAsync();
		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + uri, ex);

			mErrorListener.onError(mediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + uri, ex);

			// mErrorListener.onError(mediaPlayer,
			// MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
	}

	/*
	 * release the media player in any state
	 */
	private void releaseMedia() {
		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	private synchronized void toggleMediaControllers() {
		if (mediaControllerHolder.mediaController != null) {
			switch (mediaControllerHolder.mediaController.getVisibility()) {
			case View.VISIBLE: {
				mediaControllerHolder.mediaController
						.setVisibility(View.INVISIBLE);

				break;
			}
			default: {
				mediaControllerHolder.mediaController
						.setVisibility(View.VISIBLE);

				break;
			}
			}
		}
	}

	private synchronized void toggleFullscreen() {
		switch (getRequestedOrientation()) {
		case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE: {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

			break;
		}
		default: {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

			break;
		}
		}
	}

	private synchronized void updateBufferPercentage(final int percent) {
		this.currentBufferPercentage = percent;

		if (mediaControllerHolder.footerHolder.seekBar != null) {
			mediaControllerHolder.footerHolder.seekBar
					.setSecondaryProgress(percent);
		}
	}

	private synchronized void updateTimeUI() {
		if (mediaPlayer != null) {
			int position = mediaPlayer.getCurrentPosition();

			updateTimeUI(position);
		}
	}

	private synchronized void updateTimeUI(final int position) {
		updateTimeUI(position, true, true);
	}

	private synchronized void updateTimeUI(final int position,
			final boolean updateTimeLabel, final boolean updateSeekBar) {
		// Time TextView
		if (updateTimeLabel) {
			long seconds = TimeUnit.MILLISECONDS.toSeconds(position) % 60;
			long minutes = TimeUnit.MILLISECONDS.toMinutes(position);

			StringBuffer buffer = new StringBuffer();

			if (minutes < 10) {
				buffer.append('0');
			}

			buffer.append(minutes);
			buffer.append(':');

			if (seconds < 10) {
				buffer.append('0');
			}

			buffer.append(seconds);

			String timeString = buffer.toString();

			if (mediaControllerHolder.footerHolder.timeTextView != null) {
				mediaControllerHolder.footerHolder.timeTextView
						.setText(timeString);
			}
		}

		// SeekBar
		if (updateSeekBar) {
			if (this.mediaPlayer != null) {
				int progress = (int) (((float) position / (float) mediaPlayer
						.getDuration()) * 100);

				if (mediaControllerHolder.footerHolder.seekBar != null) {
					mediaControllerHolder.footerHolder.seekBar
							.setProgress(progress);
				}
			}
		}
	}

	private synchronized void startTrackingVideoTime() {
		if (!this.timeTrackEnabled) {
			this.timeTrackEnabled = true;

			this.handler.post(timeUpdater);
		}
	}

	private void stopTrackingVideoTime() {
		this.timeTrackEnabled = false;

		this.handler.removeCallbacks(timeUpdater);
	}

	private void addRelatedVideo(final VideoClip videoClip) {
		LinearLayout layout = getVideoClipRelatedContainer();

		if (videoClip != null && layout != null) {
			LayoutInflater inflator = LayoutInflater.from(this);

			ViewGroup rootView = (ViewGroup) inflator.inflate(
					R.layout.videoclip_related_row, layout, false);

			// Fill UI components
			// ImageView
			NetworkImageView imageView = (NetworkImageView) rootView
					.findViewById(R.id.videoclip_related_imageview);

			if (imageView != null) {
				imageView.setImageUrl(videoClip.getImageUrl(),
						SkorerTVApplication.imageLoader);
			}

			// Title
			TextView titleTextView = (TextView) rootView
					.findViewById(R.id.videoclip_related_titletextview);

			if (titleTextView != null) {
				titleTextView.setText(videoClip.getTitle());
			}

			// // Duration
			// TextView durationTextView = (TextView)
			// rootView.findViewById(R.id.videoclip_related_durationtextview);
			//
			// if(durationTextView != null)
			// {
			// durationTextView.setText("");
			// }
			//
			// // Impression
			// TextView impressionTextView = (TextView)
			// rootView.findViewById(R.id.videoclip_related_impressiontextview);
			//
			// if(impressionTextView != null)
			// {
			// impressionTextView.setText(videoClip.getViewCount() + "");
			// }

			rootView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					VideoClipActivity activity = VideoClipActivity.this;

					if (ApplicationUtilities.sharedInstance().isActivityAlive(
							activity)) {
						Intent intent = activity.getIntent();
						intent.putExtra(BUNDLE_VIDEOCLIP_KEY, videoClip.getId());
						activity.startActivity(intent);
						activity.finish();
					}
				}
			});

			layout.addView(rootView);
		}
	}

	void showStatusView(final String message, final boolean showProgressBar,
			final String actionButtonText,
			final View.OnClickListener actionButtonClickListener,
			final boolean animated) {
		final View statusView = findViewById(R.id.statusview);

		if (statusView != null) {
			statusView.post(new Runnable() {
				@Override
				public void run() {
					// Progress bar
					ProgressBar progressBar = (ProgressBar) statusView
							.findViewById(R.id.statusview_progressbar);

					if (progressBar != null) {
						progressBar
								.setVisibility((showProgressBar) ? (View.VISIBLE)
										: (View.INVISIBLE));
					}

					// Message
					TextView textView = (TextView) statusView
							.findViewById(R.id.statusview_textview);

					if (textView != null) {
						textView.setText(message);
					}

					// Action button
					Button actionButton = (Button) statusView
							.findViewById(R.id.statusview_button);

					if (actionButton != null) {
						actionButton.setText(actionButtonText);
						actionButton
								.setOnClickListener(actionButtonClickListener);

						actionButton
								.setVisibility((actionButtonClickListener == null) ? (View.INVISIBLE)
										: (View.VISIBLE));
					}

					crossfade(null, statusView, (animated) ? (2000) : (0));

					statusView.bringToFront();
				}
			});
		}
	}

	void hideStatusView(final boolean animated) {
		final View statusView = findViewById(R.id.statusview);

		if (statusView != null) {
			statusView.post(new Runnable() {
				@Override
				public void run() {
					crossfade(statusView, null, (animated) ? (2000) : (0));
				}
			});
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void crossfade(final View ancestorView, final View successorView,
			final int animationDuration) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
					if (successorView != null
							&& successorView.getVisibility() != View.VISIBLE
							&& successorView.getAlpha() < 1.0f) {
						// Set the content view to --0%-- !!!not 0 but
						// current!!! opacity but visible, so that it is visible
						// (but fully transparent) during the animation.
						// successorView.setAlpha(successorView.getAlpha());
						successorView.setVisibility(View.VISIBLE);

						// Animate the content view to 100% opacity, and clear
						// any animation
						// listener set on the view.
						successorView.animate().alpha(1f)
								.setDuration(animationDuration)
								.setListener(null);
					}

					if (ancestorView != null
							&& ancestorView.getVisibility() == View.VISIBLE) {
						// Animate the loading view to 0% opacity. After the
						// animation ends,
						// set its visibility to GONE as an optimization step
						// (it won't
						// participate in layout passes, etc.)
						ancestorView.animate().alpha(0f)
								.setDuration(animationDuration)
								.setListener(new AnimatorListenerAdapter() {

									@Override
									public void onAnimationEnd(
											Animator animation) {
										ancestorView.setVisibility(View.GONE);
									}
								});
					}
				} else {
					if (successorView != null) {
						successorView.setVisibility(View.VISIBLE);
					}

					if (ancestorView != null) {
						ancestorView.setVisibility(View.GONE);
					}
				}
			}
		});
	}

	/***************************************
	 * Acccessors
	 */

	private void setSurfaceView(SurfaceView view) {
		if (view == null) {
			this.surfaceViewWeak = null;
		} else {
			this.surfaceViewWeak = new WeakReference<SurfaceView>(view);
		}
	}

	private SurfaceView getSurfaceView() {
		return (this.surfaceViewWeak == null) ? (null) : (this.surfaceViewWeak
				.get());
	}

	private void setVideoContainer(View view) {
		if (view == null) {
			this.videoContainerWeak = null;
		} else {
			this.videoContainerWeak = new WeakReference<View>(view);
		}
	}

	private View getVideoContainer() {
		return (this.videoContainerWeak == null) ? (null)
				: (this.videoContainerWeak.get());
	}

	private void setFooterView(View view) {
		if (view == null) {
			this.footerViewWeak = null;
		} else {
			this.footerViewWeak = new WeakReference<View>(view);
		}
	}

	private View getFooterView() {
		return (this.footerViewWeak == null) ? (null) : (this.footerViewWeak
				.get());
	}

	private void setTitleTextView(final TextView textView) {
		if (textView == null) {
			this.titleTextViewWeak = null;
		} else {
			this.titleTextViewWeak = new WeakReference<TextView>(textView);
		}
	}

	private TextView getTitleTextView() {
		return (this.titleTextViewWeak == null) ? (null)
				: (this.titleTextViewWeak.get());
	}

	private void setDescriptionTextView(final TextView textView) {
		if (textView == null) {
			this.descriptionTextViewWeak = null;
		} else {
			this.descriptionTextViewWeak = new WeakReference<TextView>(textView);
		}
	}

	private TextView getDescriptionTextView() {
		return (this.descriptionTextViewWeak == null) ? (null)
				: (this.descriptionTextViewWeak.get());
	}

	private void setPublishDateTextView(final TextView textView) {
		if (textView == null) {
			this.publishDateTextViewWeak = null;
		} else {
			this.publishDateTextViewWeak = new WeakReference<TextView>(textView);
		}
	}

	private TextView getPublishDateTextView() {
		return (this.publishDateTextViewWeak == null) ? (null)
				: (this.publishDateTextViewWeak.get());
	}

	private void setImpressionCountTextView(final TextView textView) {
		if (textView == null) {
			this.impressionCountTextViewWeak = null;
		} else {
			this.impressionCountTextViewWeak = new WeakReference<TextView>(
					textView);
		}
	}

	private TextView getImpressionCountTextView() {
		return (this.impressionCountTextViewWeak == null) ? (null)
				: (this.impressionCountTextViewWeak.get());
	}

	private void setVideoClipRelatedContainer(final LinearLayout view) {
		if (view == null) {
			this.videoClipRelatedContainerWeak = null;
		} else {
			this.videoClipRelatedContainerWeak = new WeakReference<LinearLayout>(
					view);
		}
	}

	private LinearLayout getVideoClipRelatedContainer() {
		return (this.videoClipRelatedContainerWeak == null) ? (null)
				: (this.videoClipRelatedContainerWeak.get());
	}

	private void setLikeButton(final View view) {
		if (view == null) {
			this.likeButtonWeak = null;
		} else {
			this.likeButtonWeak = new WeakReference<View>(view);
		}

	}

	private View getLikeButton() {
		return (this.likeButtonWeak == null) ? (null) : (this.likeButtonWeak
				.get());
	}

	private void setDislikeButton(final View view) {
		if (view == null) {
			this.dislikeButtonWeak = null;
		} else {
			this.dislikeButtonWeak = new WeakReference<View>(view);
		}
	}

	private View getDislikeButton() {
		return (this.dislikeButtonWeak == null) ? (null)
				: (this.dislikeButtonWeak.get());
	}
	
	private NetworkImageView getVideoThumbnailImageView()
	{
		return (this.videoThumbnailImageViewWeak == null)?(null):(this.videoThumbnailImageViewWeak.get());
	}
	
	private void setVideoThumbnailImageView(NetworkImageView imageView)
	{
		if(imageView == null)
		{
			this.videoThumbnailImageViewWeak = null;
		}
		else
		{
			this.videoThumbnailImageViewWeak = new WeakReference<NetworkImageView>(imageView);
		}
	}
}
