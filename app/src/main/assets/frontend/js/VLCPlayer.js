function VlcPlayerPlugin() {

    var self = this;

    window.VlcPlayer = this;

    self.name = 'Vlc Player';
    self.type = 'mediaplayer';
    self.id = 'vlcplayer';
    self.subtitleStreamIndex = -1;
    self.audioStreamIndex = -1;
    self.cachedDeviceProfile = null;

    // Prioritize first
    self.priority = -2;
    //self.supportsProgress = false;
    self.isLocalPlayer = true;
    // Disable orientation lock
    self.isExternalPlayer = true;
    self._currentTime = 0;
    self._paused = true;
    self._volume = 100;
    self._currentSrc = null;

    self.canPlayMediaType = function (mediaType) {
        mediaType = (mediaType || '').toLowerCase();
        return mediaType === 'audio' || mediaType === 'video';
    };

    self.canPlayItem = function (item, playOptions) {
        return true;
    };

    // self.supportsPlayMethod = function (playMethod, item) {
    //     return playMethod === 'DirectStream';
    // };

    self.currentSrc = function () {
        return self._currentSrc;
    };

    self.play = function (options) {
        return new Promise(function (resolve) {
            self._currentTime = (options.playerStartPositionTicks || 0) / 10000;
            self._paused = false;
            self._currentSrc = options.url;
            console.log('开始播放：' + options.url);
            window.ExternalPlayer.initPlayer(JSON.stringify(options));
            resolve();
            //this.loading.hide();
        });
    };

    self.setSubtitleStreamIndex = function (index) {
    };

    self.setAudioStreamIndex = function (index) {
    };

    self.canSetAudioStreamIndex = function () {
        return false;
    };

    self.setAudioStreamIndex = function (index) {
    };

    // Save this for when playback stops, because querying the time at that point might return 0
    self.currentTime = function (val) {
        return null;
    };

    self.currentTime = function () {
        self._currentTime = window.ExternalPlayer.getPostion() || self._currentTime ;
        return self._currentTime * 1000;
        //return (self._currentTime || 0) * 1000;
    };

    self.duration = function (val) {
        return null;
    };

    self.destroy = function () {
    };

    self.pause = function () {
    };

    self.unpause = function () {
    };

    self.paused = function () {
        return self._paused;
    };

    self.stop = function (destroyPlayer) {
        return new Promise(function (resolve) {
            if (destroyPlayer) {
                self.destroy();
            }
            resolve();
        });
    };

    self.volume = function (val) {
        return self._volume;
    };

    self.setMute = function (mute) {
    };

    self.isMuted = function () {
        return self._volume == 0;
    };

    self.notifyEnded = function () {
        new Promise(function () {
            let stopInfo = {
                src: self._currentSrc
            };

            events.trigger(self, 'stopped', [stopInfo]);
            self._currentSrc = self._currentTime = null;
        });
    };

    self.notifyTimeUpdate = function (currentTime) {
        // if no time provided handle like playback completed
        currentTime = currentTime || playbackManager.duration(self);
        new Promise(function () {
            currentTime = currentTime / 1000;
            self._timeUpdated = self._currentTime != currentTime;
            self._currentTime = currentTime;
            events.trigger(self, 'timeupdate');
        });
    };

    self.notifyCanceled = function () {
        // required to not mark an item as seen / completed without time changes
        let currentTime = self._currentTime || 0;
        self.notifyTimeUpdate(currentTime - 1);
        if (currentTime > 0) {
            self.notifyTimeUpdate(currentTime);
        }
        self.notifyEnded();
    };

    self.changeSubtitleStream = function (index) {
        // detach from the main ui thread
        new Promise(function () {
            var innerIndex = Number(index);
            self.subtitleStreamIndex = innerIndex;
        });
    };

    self.changeAudioStream = function (index) {
        // detach from the main ui thread
        new Promise(function () {
            var innerIndex = Number(index);
            self.audioStreamIndex = innerIndex;
        });
    }

    self.getDeviceProfile = function () {
        var dpf = {
            Name: 'VLC Player Stub',
            MaxStreamingBitrate: 100000000000,
            MaxStaticBitrate: 100000000000,
            MusicStreamingTranscodingBitrate: 320000000,
            DirectPlayProfiles: [{ Type: 'Video' }, { Type: 'Audio' }],
            CodecProfiles: [],
//            TranscodingProfiles: [{
//                Container: 'mkv', Type: 'Video', AudioCodec: 'mp3,ac3,aac', VideoCodec: 'h264', Context: 'Streaming', MaxAudioChannels: '2'
//            }, {
//                Container: 'mp3', Type: 'Audio', AudioCodec: 'mp3', Context: 'Streaming', Protocol: 'http'
//            }],
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
        return Promise.resolve(dpf);
    }
}
