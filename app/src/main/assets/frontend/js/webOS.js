/* 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
*/

(function (AppInfo) {
    'use strict';

    console.log('WebOS adapter');

    function postMessage(type, data) {
        window.top.postMessage({
            type: type,
            data: data
        }, '*');
    }

    // List of supported features
    var SupportedFeatures = [
        'exit',
        'externallinkdisplay',
        'htmlaudioautoplay',
        'htmlvideoautoplay',
        'imageanalysis',
        'physicalvolumecontrol',
        'displaylanguage',
        'otherapppromotions',
        'targetblank',
        'screensaver',
        'subtitleappearancesettings',
        'subtitleburnsettings',
        'chromecast',
        'multiserver'
    ];

    class ijkplayer {
        constructor() {
            this.priority = -2;
            this.name = 'IJK Player';
            this.type = 'mediaplayer';
            this.id = 'ijkplayer';
            this.isLocalPlayer = true;
        }

        play(options) {
            // Sanitize input
            return new Promise(function (resolve, reject) {
                var re = /^(http.*?)\?/;
                var url = re.exec(options.url);
                if (url) {
                    if (url.length == 2) {
                        var videoUrl = url[1] + '?static=true';
                        window.NativeInterface.toPlay(videoUrl);
                    } else {
                        console.log('播放链接错误!');
                    }
                } else {
                    console.log('没有获取到播放链接');
                }
                this.loading.hide();
            });
        }

        stop(destroyPlayer) {
            return Promise.resolve();
        }

        destroy() {
            console.log('销毁destroy');
        }
        canPlayMediaType(mediaType) {
            mediaType = (mediaType || '').toLowerCase();
            return mediaType === 'audio' || mediaType === 'video';
        }

        canPlayItem(item) {
            // Does not play server items
            return true;
        }

        canPlayUrl(url) {
            return true;
        }

        getDeviceProfile() {
            return deviceprofile;
        }

        currentSrc() {
            return '';
        }

        // Save this for when playback stops, because querying the time at that point might return 0
        currentTime(val) {
            if (val != null) {
                window.NativeInterface.seekTo(val);
                return;
            } else {
                var ct = 0;
                ct = window.NativeInterface.getPostion();
                return ct
            }
        }

        duration() {
            return window.NativeInterface.getDuration();;
        }
        pause() {

        }
        unpause() {

        }
        paused() {
            return false;
        }
        volume(val) {
            //             if (val != null) {
            //                 return this.setVolume(val);
            //             }
            //             return this.getVolume();
        }

        setMute(mute) {

        }

        isMuted() {

        }
    }

    var ijkplayerPlugin = new Promise(function (resolve) {
        return resolve(function () {
            return new ijkplayer();
        });
    });

    var deviceprofile = {
        "Name": "Jellyfin for IJKPlayer",
        "MaxStreamingBitrate": 90000000000,
        "MaxStaticBitrate": 90000000000,
        "MusicStreamingTranscodingBitrate": 1280000000,
        "TimelineOffsetSeconds": 5,
        "TranscodingProfiles": [],
        "DirectPlayProfiles": [{ "Type": "Video",'container':'mp4' }, { "Type": "Audio" }, { "Type": "Photo" }],
        "ResponseProfiles": [{'Type': 'Video', 'MimeType': 'video/mp4'}],
        "ContainerProfiles": [],
        "CodecProfiles": [],
        "SubtitleProfiles": [
            { "Format": "srt", "Method": "External" },
            { "Format": "srt", "Method": "Embed" },
            { "Format": "ass", "Method": "External" },
            { "Format": "ass", "Method": "Embed" },
            { "Format": "sub", "Method": "Embed" },
            { "Format": "sub", "Method": "External" },
            { "Format": "ssa", "Method": "Embed" },
            { "Format": "ssa", "Method": "External" },
            { "Format": "smi", "Method": "Embed" },
            { "Format": "smi", "Method": "External" },
            { "Format": "pgssub", "Method": "Embed" },
            { "Format": "dvdsub", "Method": "Embed" },
            { "Format": "pgs", "Method": "Embed" },
        ],
    }

    window.NativeShell = {
        AppHost: {
            init: function () {
                postMessage('AppHost.init', AppInfo);
                return Promise.resolve(AppInfo);
            },

            appName: function () {
                postMessage('AppHost.appName', AppInfo.appName);
                return AppInfo.appName;
            },

            appVersion: function () {
                postMessage('AppHost.appVersion', AppInfo.appVersion);
                return AppInfo.appVersion;
            },

            deviceId: function () {
                postMessage('AppHost.deviceId', AppInfo.deviceId);
                return AppInfo.deviceId;
            },

            deviceName: function () {
                postMessage('AppHost.deviceName', AppInfo.deviceName);
                return AppInfo.deviceName;
            },

            exit: function () {
                postMessage('AppHost.exit');
                console.log('退出APP');
            },

            getDefaultLayout: function () {
                postMessage('AppHost.getDefaultLayout', 'tv');
                return 'tv';
            },

            getDeviceProfile: function (profileBuilder) {
                postMessage('AppHost.getDeviceProfile');
                return deviceprofile;
            },

            getSyncProfile: function (profileBuilder) {
                postMessage('AppHost.getSyncProfile');
                return deviceprofile;
            },

            supports: function (command) {
                var isSupported = command && SupportedFeatures.indexOf(command.toLowerCase()) != -1;
                postMessage('AppHost.supports', {
                    command: command,
                    isSupported: isSupported
                });
                return isSupported;
            }
        },

        selectServer: function () {
            postMessage('selectServer');
        },

        downloadFile: function (url) {
            postMessage('downloadFile', { url: url });
        },

        enableFullscreen: function () {
            postMessage('enableFullscreen');
        },

        disableFullscreen: function () {
            postMessage('disableFullscreen');
        },

        getPlugins: function () {
            postMessage('getPlugins');
            return [ijkplayerPlugin];
        },

        openUrl: function (url, target) {
            postMessage('openUrl', {
                url: url,
                target: target
            });
        },

        updateMediaSession: function (mediaInfo) {
            postMessage('updateMediaSession', { mediaInfo: mediaInfo });
        },

        hideMediaSession: function () {
            postMessage('hideMediaSession');
        }
    };
})(window.AppInfo);
