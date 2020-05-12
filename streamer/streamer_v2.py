import cv2
import numpy as np
import urllib.request
from threading import Thread
import socket
import time
import requests
import json


class Streamer:
    '''
    description:-
        Class responsible for connecting to the anrdroid app and managing the data communication.
        How it works:
            - every massege from and to the app are encapsulated by a starting tag and an ending tag
            - the sending side (either android or pc side) first turn the massege to a byte array
                then appends to the start and end of that array with a tag.
            - for example when sending frame masseges from the app, the massege is as follows:
                [FRAME START TAG] [BYTE STREAM] [FRAME END TAG]
    Inputs:
        src: string, ip address of the android
        port: int, port of the app on the android
        buffer_size: int, amount of incoming frames to buffer
        f_st: string, specify the frame start tag
        f_en: string, specify the frame end tag
        d_st: string, specify the data start tag
        d_en: string, specify the data end tag
    '''

    def __init__(self, src, port, buffer_size=5, f_st="frame_start", f_en="frame_end",
                d_st="data_start", d_en="data_end"):
        self.src = src
        self.port = port
        self.buffer_size = buffer_size
        self.f_st, self.f_en, self.d_st, self.d_en =f_st, f_en, d_st, d_en

        # initialize the socket and connect
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.setblocking(True)
        self.sock.settimeout(3)
        try:
            self.sock.connect((src, port))
        except:
            self.sock = None
            self.stop_threads = True
            return None
  
        # initialize the buffers

        # frame buffer (circular buffer)
        self.frame_insert_idx = 0
        self.frame_output_idx = 0
        self.frames = [None] * buffer_size

        self.data = None # data buffer (1 slot buffer)

        # start the thread responsible for receiving and buffering the incoming masseges
        self.stop_threads = False
        Thread(target=self.thread).start()
    
    def thread(self):
        '''
        Main thread that recives and extracts masseges from the app
        '''
        frame_conversion, data_conversion = False, False
        recv_size = 1024 # initial byte buffer size for the socket
        buffer = b'' # general byte buffer
        frame_buffer, data_buffer = b'', b'' # byte buffer for the frame and data masseges

        while self.stop_threads == False: 
            if(self.sock._closed): # stop if socket is closed
                self.stop_threads = self.sock._closed 
                break
            try:
                r = self.sock.recv(recv_size) # receive the byte stream
                if len(r) == 0:
                    exit(0)

                buffer += r # add the received byte stream to the general buffer

                # Extract frame masseges============================================
                if frame_conversion == False:
                    s = buffer.find(bytearray(self.f_st, encoding ='utf-8'))
                    if s != -1:
                        frame_conversion = True
                        frame_buffer = b''
                
                if frame_conversion:
                    e = buffer.find(bytearray(self.f_en, encoding ='utf-8'))
                    if e != -1:
                        frame_conversion = False
                        frame_buffer = buffer[s+len(self.f_st):e]
                        buffer = buffer[:s] +buffer[e+len(self.f_en):]
                        recv_size = 512 + len(frame_buffer)
                    else:
                        continue
                        
                ####################################################################
                # Extract data masseges=============================================
                if data_conversion == False:
                    s = buffer.find(bytearray(self.d_st, encoding ='utf-8'))
                    if s != -1:
                        data_conversion = True
                        data_buffer = b''
                
                if data_conversion:
                    e = buffer.find(bytearray(self.d_en, encoding ='utf-8'))
                    if e != -1:
                        data_conversion = False
                        data_buffer = buffer[s+len(self.d_st):e]
                        buffer = buffer[:s] +buffer[e+len(self.d_en):]
                        self.data = data_buffer.decode('ascii')
                    else:
                        continue
                ####################################################################

            except Exception as e:
                print(e)
                continue

            try:
                # if frame buffer is not full
                if (self.frame_insert_idx+1) % self.buffer_size != self.frame_output_idx:
                    # decode the byte frame massege to a numpy array
                    nparr = np.fromstring(frame_buffer, np.uint8)
                    frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
                    if type(frame) is type(None):
                        print("frame dropped")
                        pass

                    # store the frame in the frame buffer
                    self.frames[self.frame_insert_idx] = frame
                    # increment the input index of the ring buffer
                    self.frame_insert_idx = (self.frame_insert_idx+1) % self.buffer_size
                
            except Exception as e:
                print(e)
                pass
        self.sock.close()
                 
    def fetch_frame(self):
        '''
        Blocking loop until a frame is available
        '''
        while(self.frame_insert_idx == self.frame_output_idx and self.stop_threads == False ):
            continue
        frame = self.frames[self.frame_output_idx].copy()

        # increment the output index of the ring buffer
        self.frame_output_idx = (self.frame_output_idx+1) % self.buffer_size
        return frame
 
    def fetch_data(self):
        '''
        fetch received data
        note: data is in json format and needs to be converted to json object first
        '''
        try:
            if type(self.data) is not type(None) and self.data != "":
                data = self.data[self.data.find("{"):]
                data = json.loads(data)
                self.data= None
                return data
        except json.JSONDecodeError as e:
            print("fetch_data error:" +str(e))
            self.data = None
        return None

    def send_data(self, data):
        '''
        converts data to json format and encapsulates with start and end tags before sendong
        input:
            data: dictionary, data to be sent
        '''
        try:
            data = "START" + json.dumps(data) + "END"
            self.sock.send(data.encode('utf-8'))
            # self.sock.send("START".encode('utf-8'))
            # self.sock.send(json.dumps(data).encode('utf-8'))
            # self.sock.send("END".encode('utf-8'))
        except ConnectionAbortedError as e:
            print("send_data error:" + str(e))

    def release(self):
        self.stop_threads = True
        

        
# testing
if __name__ == "__main__":
    src = "172.16.17.188"
    port = 8888
    streamer = Streamer(src, port)

    key = ' '
    while key != ord("q"):
        frame = streamer.fetch_frame()
        cv2.imshow("show", frame)
        data = streamer.fetch_data()
        if type(data) is not type(None):
            # streamer.send_data(data)
            print(data)

        key = cv2.waitKey(1)
    streamer.release()

