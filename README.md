This repo is for our CVC competition project.

Tracking is a fundamental task in any video application requiring some degree of reasoning about objects of interest, our project aims to create an android app to demonstrate real-time object tracking by only clicking on an object in the app.

The project consists of three main modules:

1- Core: which includes the algorithms necessary for object tracking, for now we have two algorithms namely, siammask which is a deeplearning model and optical flow algorithm which is a computer vision solution, this module might be placed on a desktop pc with good gpu.

2- Streamer: this module is about streaming rgb data between the android app and the Core algorithms which might reside on a desktop pc.

3- Android app: conatins the necessary code for the interactive gui on the android.

A fourth module is under investigation and is called "Tracking storage" that handles saving already tracked objects for later detection and tracking without having to reindicate them again.


UPDATES:
- our core use siammask(deeplearning model) for both tracking and segmentation!!
- Opticalflow is canceled but we left the files.
- Added a ui module for the Core.

How to use:
- clone the repo
- cd into the repo
- run   pip install -r requirements.txt
- download the model weights from http://www.robots.ox.ac.uk/~qwang/SiamMask_DAVIS.pth into core/siammask/weights
- run   python main.py

Packaging:
- run python setyp.py build
- copy the core folder into your newly created build folder that contains main.exe
- run main.exe

Android App:
Build using android studio or wait until we upload the apk
