define(['events', 'appSettings', 'loading', 'playbackManager'], function (events, appSettings, loading, playbackManager) {
    "use strict";

    //constructor({ events, playbackManager, loading }) {
    return function () {
        window['ExoPlayer'] = this;

        this.events = events;
        this.playbackManager = playbackManager;
        this.loading = loading;

        this.name = 'ExoPlayer';
        this.type = 'mediaplayer';
        this.id = 'exoplayer';

        // Prioritize first
        this.priority = -1;
        this.isLocalPlayer = true;

        // Current playback position in milliseconds
        this._currentTime = 0;
        this._paused = true;

        this._nativePlayer = window['NativePlayer'];

        this.play = function (options) {
            return new Promise(function (resolve) {
                self._paused = false;
                options.items = options.items.map(
                    function (item) {
                        return { 'id': item.Id, 'name': item.Name, 'startPositionTicks': item.playOptions.startPositionTicks };
                    }
                );
                console.log(JSON.stringify(options));
                this._nativePlayer.loadPlayer(window.location.href,JSON.stringify(options));
                loading.hide();
                resolve();
            });
        }

        this.shuffle = function (item) { }

        this.instantMix = function (item) { }

        this.queue = function (options) { }

        this.queueNext = function (options) { }

        this.canPlayMediaType = function (mediaType) {
            return mediaType === 'Video';
        }

        this.canQueueMediaType = function (mediaType) {
            return this.canPlayMediaType(mediaType);
        }

        this.canPlayItem = function (item, playOptions) {
            return this._nativePlayer.isEnabled();
        }

        this.stop = function (destroyPlayer) {
            return new Promise(function (resolve) {
                this._nativePlayer.stopPlayer();


                if (destroyPlayer) {
                    self.destroy();
                }

                resolve();
            });
        }

        this.nextTrack = function () { }

        this.previousTrack = function () { }

        this.seek = function (ticks) {
            this._nativePlayer.seek(ticks);
        }

        this.currentTime = function (ms) {
            if (ms !== undefined) {
                this._nativePlayer.seekMs(ms);
            }
            return this._currentTime;
        }

        this.duration = function (val) {
            return null;
        }

        /**
         * Get or set volume percentage as as string
         */
        this.volume = function (volume) {
            if (volume !== undefined) {
                this.setVolume(volume);
            }
            return null;
        }

        this.getVolume = function () { }

        this.setVolume = function (vol) {
            let volume = parseInt(vol);
            this._nativePlayer.setVolume(volume);
        }

        this.volumeUp = function () { }

        this.volumeDown = function () { }

        this.isMuted = function () {
            return false;
        }

        this.setMute = function (mute) {
            // Assume 30% as default when unmuting
            this._nativePlayer.setVolume(mute ? 0 : 30);
        }

        this.toggleMute = function () { }

        this.paused = function () {
            return this._paused;
        }

        this.pause = function () {
            this._paused = true;
            this._nativePlayer.pausePlayer();
        }

        this.unpause = function () {
            this._paused = false;
            this._nativePlayer.resumePlayer();
        }

        this.playPause = function () {
            if (this._paused) {
                this.unpause();
            } else {
                this.pause();
            }
        }

        this.canSetAudioStreamIndex = function () {
            return false;
        }

        this.setAudioStreamIndex = function (index) { }

        this.setSubtitleStreamIndex = function (index) { }

        this.changeAudioStream = function (index) {
            // detach from the main ui thread
            new Promise(function () {
                var innerIndex = Number(index);
                playbackManager.setAudioStreamIndex(innerIndex);
                self.audioStreamIndex = innerIndex;
            });
        }

        this.changeSubtitleStream = function (index) {
            // detach from the main ui thread
            new Promise(function () {
                var innerIndex = Number(index);
                playbackManager.setSubtitleStreamIndex(innerIndex);
                self.subtitleStreamIndex = innerIndex;
            });
        }

        this.getPlaylist = function () {
            return Promise.resolve([]);
        }

        this.getCurrentPlaylistItemId = function () { }

        this.setCurrentPlaylistItem = function () {
            return Promise.resolve();
        }

        this.removeFromPlaylist = function () {
            return Promise.resolve();
        }

        this.destroy = function () {
            this._nativePlayer.destroyPlayer();
        }

        this.getDeviceProfile = function () {
            // using native player implementations, check if item can be played
            // also check if direct play is supported, as audio is supported
            return new Promise(function (resolve) {

                var profile = {
                    Name: 'VLC Player Stub',
                    MaxStreamingBitrate: 100000000000,
                    MaxStaticBitrate: 100000000000,
                    MusicStreamingTranscodingBitrate: 320000000,
                    DirectPlayProfiles: [{ Type: 'Video' }, { Type: 'Audio' }],
                    CodecProfiles: [],
                    TranscodingProfiles: [{
                        Container: 'mp4', Type: 'Video', AudioCodec: 'mp3,ac3,aac', VideoCodec: 'h264', Context: 'Streaming', MaxAudioChannels: '2'
                    }, {
                        Container: 'mp3', Type: 'Audio', AudioCodec: 'mp3', Context: 'Streaming', Protocol: 'http'
                    }],
                    SubtitleProfiles: [
                        { "Format": "srt", "Method": "External" },
                        { "Format": "srt", "Method": "Embed" },
                        { "Format": "ass", "Method": "External" },
                        { "Format": "ass", "Method": "Embed" },
                        { "Format": "vtt", "Method": "Embed" },
                        { "Format": "vtt", "Method": "External" },
                        { "Format": "sub", "Method": "Embed" },
                        { "Format": "sub", "Method": "External" },
                        { "Format": "ssa", "Method": "Embed" },
                        { "Format": "ssa", "Method": "External" },
                        { "Format": "smi", "Method": "Embed" },
                        { "Format": "smi", "Method": "External" },
                        { "Format": "pgssub", "Method": "Embed" },
                        { "Format": "pgssub", "Method": "External" },
                        { "Format": "dvdsub", "Method": "Embed" },
                        { "Format": "dvdsub", "Method": "External" },
                        { "Format": "pgs", "Method": "Embed" },
                        { "Format": "pgs", "Method": "External" },
                        { "Format": "subrip", "Method": "Embed" },
                        { "Format": "subrip", "Method": "External" },
                    ]
                };
                resolve(profile);
            });
        }
    }
});