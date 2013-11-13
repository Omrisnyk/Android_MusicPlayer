package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.content.{Context, BroadcastReceiver, Intent}
import android.os.IBinder
import android.media.MediaPlayer
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global


class MusicPlayService extends SService {
  var status = Constants.PLAY_STATUS_STOP
  var receiver: BroadcastReceiver = _

  def onBind(intent: Intent): IBinder = null

  var player: MediaPlayer = _
  var nowPlay: Song = _

  onCreate({
    // 注册音乐服务广播
    receiver = new MusicPlayServiceBroadcastReceiver
    registerReceiver(receiver, Constants.MUSIC_SERVICE_ACTION)

    future {
      while (true) {
        if (player != null && nowPlay != null) {
          nowPlay.curTime = player.getCurrentPosition
          sendBroadcast(new Intent(Constants.MUSIC_PLAYER_ACTION)
            .putExtra("song", nowPlay)
            .putExtra("status",status))

        }
        Thread.sleep(500)
      }
    }
  })


  onDestroy({
    //    注销音乐服务接收器
    unregisterReceiver(receiver)
  })

  def playPause(song: Song) {
    status match {
      case Constants.PLAY_STATUS_STOP =>
        play(song,true)
      case Constants.PLAY_STATUS_PLAY =>
        pause()
      case Constants.PLAY_STATUS_PAUSE =>
        play(song)
    }
  }

  def stopPlay() {
    status = Constants.PLAY_STATUS_STOP
    sendBroadcast(new Intent(Constants.MUSIC_PLAYER_ACTION).putExtra("status", status))
  }


  def play(song: Song,reset:Boolean = false) {
    if(reset)
      player = MediaPlayer.create(this, song.songId)
    player.start()

    status = Constants.PLAY_STATUS_PLAY
    nowPlay=song
    sendBroadcast(
      new Intent(Constants.MUSIC_PLAYER_ACTION)
        .putExtra("status", status)
        .putExtra("song", song))
  }

  def pause() {
    if (player != null && player.isPlaying) {
      player.pause()

      status = Constants.PLAY_STATUS_PAUSE
      sendBroadcast(
        new Intent(Constants.MUSIC_PLAYER_ACTION)
          .putExtra("status", status)
          .putExtra("song",nowPlay)
      )
    }
  }

  def seek(song: Song) {
    if (player != null&&nowPlay!=null)         {
      nowPlay.curTime = song.curTime
      player.seekTo(song.curTime)
    }

  }
}

class MusicPlayServiceBroadcastReceiver extends BroadcastReceiver {
  def onReceive(context: Context, intent: Intent) {
    val service = context.asInstanceOf[MusicPlayService]
    val song = intent.getSerializableExtra("song").asInstanceOf[Song]
    intent.getStringExtra("action") match {
      case Constants.PLAY_ACTION_PLAYPAUSE =>
        // 播放暂停实现
        service.playPause(song)
      case Constants.PLAY_ACTION_STOP =>
        // 实现停止
        service.stopPlay()
      case Constants.PLAY_ACTION_PREVIOUS =>
        // 实现上一首
        service.play(song)
      case Constants.PLAY_ACTION_NEXT =>
        // 实现下一首
        service.play(song)
      case Constants.PLAY_ACTION_SEEK =>
        // 跳到
        service.seek(song)

    }
  }
}