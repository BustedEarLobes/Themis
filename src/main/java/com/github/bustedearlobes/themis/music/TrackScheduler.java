package com.github.bustedearlobes.themis.music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private AudioTrack currentlyPlaying = null;

    /**
     * @param player
     *            The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the
     * queue.
     *
     * @param track
     *            The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the
        // track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case
        // the player was already playing so this
        // track goes to the queue instead.
        if(!player.startTrack(track, true)) {
            queue.offer(track);
        } else {
            currentlyPlaying = track;
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing
        // or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply
        // stop the player.
        currentlyPlaying = queue.poll();
        player.startTrack(currentlyPlaying, false);
    }
    
    public void clear() {
        queue.clear();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it
        // (FINISHED or LOAD_FAILED)
        if(endReason.mayStartNext) {
            nextTrack();
        }
    }
    
    @Override
    public String toString() {
       List<AudioTrack> listQueue = new ArrayList<>(queue);
       String listString;
       if(currentlyPlaying == null) {
           listString = "Currently playing: Nothing\n";
       } else {
           listString = "Currently playing: *"
                      + currentlyPlaying.getInfo().title
                      + "* by "
                      + currentlyPlaying.getInfo().author
                      + "\n";
       }
       listString += "Next 5 songs:\n";
       for(int i = 0; i < 5; i ++) {
           listString += "    " + (i+1) + ".) ";
           if(i < listQueue.size()) {
               listString += "*" + listQueue.get(i).getInfo().title
                           + "* by "
                           + listQueue.get(i).getInfo().author
                           + "\n";
           } else {
               listString += "Nothing" + "\n";
           }
       }
       return listString;
    }
}
