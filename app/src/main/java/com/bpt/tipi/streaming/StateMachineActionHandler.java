package com.bpt.tipi.streaming;

import com.bpt.tipi.streaming.service.RecorderService;

/**
 * Created by jpujolji on 16/03/18.
 */

public class StateMachineActionHandler {
    static void manageState(RecorderService recorderService, StateAction... actions) {
        for (StateAction action : actions) {
            switch (action) {
                case CAMERA_START:
                    recorderService.initPhotoCamera();
                    break;
                case CAMERA_STOP:
                    recorderService.finishPrimaryCamera();
                    break;
                case LR_START:
                    recorderService.initLocalVideoCounter();
                    recorderService.startLocalRecorder(true);
                    recorderService.startLocalVideoCounter();
                    break;
                case LR_START_WITHOUT_SOUND:
                    recorderService.initLocalVideoCounter();
                    recorderService.startLocalRecorder(false);
                    recorderService.startLocalVideoCounter();
                    break;
                case LR_STOP:
                    recorderService.stopLocalRecorder(true);
                    recorderService.pauseLocalVideoCounter();
                    break;
                case LR_STOP_WITHOUT_SOUND:
                    recorderService.stopLocalRecorder(false);
                    recorderService.pauseLocalVideoCounter();
                    break;
                case STREAMING_START:
                    recorderService.startStreamingRecorder();
                    break;
                case STREAMING_STOP:
                    recorderService.stopStreamingRecorder();
                    break;
                case PR_START:
                    recorderService.pauseLocalVideoCounter();
                    recorderService.initPostVideoCounter();
                    recorderService.startPostVideoCounter();
                    recorderService.sendBusStopRecorder();
                    break;
                case PR_STOP:
                    recorderService.pausePostVideoCounter();
                    recorderService.stopLocalRecorder(false);
                    break;
                case TAKE_PHOTO:
                    recorderService.takePhoto();
                    break;
                case TAKE_PHOTO_DIRECT:
                    recorderService.takePhotoDirect();
                    break;
                case FORCE_STOP:
                    recorderService.setForceDestroy();
                    break;
            }
        }
    }

}
