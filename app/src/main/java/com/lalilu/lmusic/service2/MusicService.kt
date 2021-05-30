package com.lalilu.lmusic.service2

import android.app.PendingIntent
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.ArrayMap
import androidx.media.MediaBrowserServiceCompat
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.entity.Song
import com.lalilu.lmusic.notification.NotificationUtils.Companion.playerChannelName
import com.lalilu.lmusic.notification.sendNotification
import com.lalilu.lmusic.utils.toMediaMeta
import java.util.*

class MusicService : MediaBrowserServiceCompat() {
    private val tag = MusicService::class.java.name

    companion object {
        const val Access_ID = "access_id"
        const val Empty_ID = "empty_id"
        const val Song_Type = "song_type"
    }

    private var prepared = false
    private lateinit var metadataCompat: MediaMetadataCompat
    private lateinit var playbackState: PlaybackStateCompat
    private lateinit var musicPlayer: MediaPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var musicSessionCallback: MusicSessionCallback
    private val resultList: ArrayMap<String, MediaBrowserCompat.MediaItem> = ArrayMap()

    override fun onCreate() {
        super.onCreate()

        musicSessionCallback = MusicSessionCallback()
        musicPlayer = MediaPlayer().also {
            it.setOnPreparedListener(musicSessionCallback)
            it.setOnCompletionListener(musicSessionCallback)
        }

        // 构造可跳转到 launcher activity 的 PendingIntent
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PAUSE
            ).build()

        mediaSession = MediaSessionCompat(this, tag).apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setSessionActivity(sessionActivityPendingIntent)
            setPlaybackState(playbackState)
            setCallback(musicSessionCallback)
            setSessionToken(sessionToken)
        }
    }

    override fun onDestroy() {
        musicSessionCallback.onStop()
    }

    inner class MusicSessionCallback : MediaSessionCompat.Callback(),
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

        override fun onPrepared(mp: MediaPlayer?) {
            println("[MusicSessionCallback]#onPrepared")
            prepared = true
            onPlay()
        }

        override fun onCompletion(mp: MediaPlayer?) {
            println("[MusicSessionCallback]#onCompletion")
            onSkipToNext()
        }

        override fun onPrepare() {
            musicPlayer.prepare()
        }

        override fun onPlay() {
            if (prepared) {
                this@MusicService.startService(Intent(this@MusicService, MusicService::class.java))
                mediaSession.isActive = true

                musicPlayer.start()
                playbackState = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f).build()
                mediaSession.setPlaybackState(playbackState)
                this@MusicService.sendNotification(
                    mediaSession,
                    mediaSession.controller,
                    metadataCompat.description,
                    playerChannelName + "_ID"
                )
            }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            when (playbackState.state) {
                PlaybackStateCompat.STATE_NONE,
                PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.STATE_PLAYING -> {
                    val metadata = resultList[mediaId] ?: return
                    println("[MusicService]#onPlayFromMediaId: [${metadata.description.mediaId}] ${metadata.description.title}")
                    musicPlayer.reset()
                    musicPlayer.setDataSource(
                        this@MusicService,
                        metadata.description.mediaUri ?: return
                    )
                    playbackState = PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_BUFFERING, 0, 1.0f)
                        .build()
                    metadataCompat =
                        extras?.toMediaMeta() ?: metadata.description.extras!!.toMediaMeta()
                    mediaSession.setPlaybackState(playbackState)
                    mediaSession.setMetadata(metadataCompat)
                    onPrepare()
                }
                else -> {
                    println("[onPlayFromMediaId]: no state compare.")
                }
            }
        }

        override fun onPause() {
            if (playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                musicPlayer.pause()
                playbackState = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f).build()
                mediaSession.setPlaybackState(playbackState)
                this@MusicService.stopForeground(false)
            }

        }

        override fun onSkipToNext() {
            val now = metadataCompat.description.mediaId
            var index = resultList.indexOfKey(now)
            index = if (index + 1 >= resultList.size) 0 else index + 1
            onPlayFromMediaId(resultList.keyAt(index), null)
        }

        override fun onStop() {
            println("[onStop]")
            stopSelf()
            mediaSession.isActive = false
            musicPlayer.stop()
            prepared = false
        }

        override fun onSeekTo(pos: Long) {
            musicPlayer.seekTo(pos.toInt())
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(Access_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
        if (parentId == Empty_ID) return
        val songList = MusicDatabase.getInstance(this).songDao().getAll()
        resultList.clear()
        for (song: Song in songList) {
            resultList[song.songId.toString()] = song.toMediaItem()
        }
        result.sendResult(resultList.values.toMutableList())
    }
}

