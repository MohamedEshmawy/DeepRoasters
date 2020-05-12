"""
    Code used to run the pc client, that will handle the core module of the project.
"""

import traceback
import sys, os
# sys.path.insert(1, "E:/Other/College/4thYear/Image_Processing/CVC/DeepRoasters") 
import json
import time
from threading import Thread
import torch

from PyQt5.QtWidgets import QApplication
from GUI.ui import Ui_MainWindow

from streamer.streamer_v2 import Streamer
from core.siammask.run import *
from core.siammask.models.custom import Custom







class main:
    def __init__(self, ui):

        self.stop_thread = False
        self.ui = ui
        ui.bindSignals(self.onConnect, self.onExit, self.onRadioBtn)
        self.enable_mask=False

    def main_thread(self, streamer):

        weights_path = "./core/siammask/weights"
        resume = os.path.join(weights_path,"SiamMask_DAVIS.pth")
        config = os.path.join(weights_path,"config_davis.json")
        
        # initialize model
        device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        torch.backends.cudnn.benchmark = True
        cfg = json.load(open(config))
        siammask = Custom(anchors=cfg['anchors'])
        siammask = load_pretrain(siammask, resume)
        siammask.eval().to(device)
        track = False

        while self.stop_thread == False:
            frame = streamer.fetch_frame()
            data = streamer.fetch_data()
            try:
                if data != None :
                    if all(value != -1 for value in data.values()):
                        # Select ROI
                        beginX, beginY, endX, endY = data["beginX"], data["beginY"], data["endX"], data["endY"]
                        beginX, beginY = max(beginX, 0), max(beginY, 0)
                        endX, endY = min(endX, frame.shape[0]), min(endY, frame.shape[1])
                        x, y, w, h = (beginX+endX)//2, (beginY+endY)//2, abs(beginX-endX), abs(beginY-endY)
                        target_pos = np.array([x, y])
                        target_sz = np.array([w, h])
                        state = siamese_init(frame, target_pos, target_sz, siammask, cfg['hp'], device=device)  # init tracker
                        track = True
                    else:
                        track = False
                if track:
                    
                    if self.enable_mask:
                        state = siamese_track(state, frame, mask_enable=True, refine_enable=True, device=device, debug=False)  # track
                        mask = state['mask'] > state['p'].seg_thr
                        beginX, beginY, endX, endY = *(state['target_pos']-state['target_sz']//2).astype(np.int), *(state['target_pos']+state['target_sz']//2).astype(np.int)
                        frame[:, :, 2] = (mask > 0) * 255 + (mask == 0) * frame[:, :, 2]
                    else:
                        state = siamese_track(state, frame, mask_enable=False, refine_enable=False, device=device, debug=False)  # track
                        beginX, beginY, endX, endY = *(state['target_pos']-state['target_sz']//2).astype(np.int), *(state['target_pos']+state['target_sz']//2).astype(np.int)
                        cv2.rectangle(frame, (beginX, beginY),(endX,endY) , (255, 255, 0) , 3)
                    data_to_send = dict(beginX=int(beginX),beginY=int(beginY),endX=int(endX),endY=int(endY))
                    streamer.send_data(data_to_send)
                frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                self.ui.draw(frame)
            except Exception as e:
                log = open("log.txt", "w")
                traceback.print_exc(file=log)
                continue

        streamer.release()

    def onConnect(self, ip, port):
        # initialize streamer
        # self.ui.status_label.setText("Connecting")
        streamer = Streamer(ip, port)
        
        if (streamer.sock is not None):
            Thread(target=self.main_thread, args=([streamer])).start()
            # self.ui.status_label.setText("Connected")
            return True
        else:
            self.ui.status_label.setText("Failed to connect")
            return False

    def onExit(self):
        self.stop_thread = True

    def onRadioBtn(self, enable_mask):
        self.enable_mask = enable_mask
        

def run():
    # initialize ui
    app = QApplication([])
    ui = Ui_MainWindow()
    ui.show()
    # start the main class
    main(ui)
    app.exec_()


if __name__ == "__main__":
    run()


