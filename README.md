This repos is for our CVC competiotion project.

Tracking is a fundamental task in any video application requiring some degree of reasoning about objects of interest, our project aims to create an android app to demonstrate real-time object tracking by only clicking on an object in the app.

The project consists of three main modules:

1- Core: which includes the algorithms necessary for object tracking, for now we have two algorithms namely, siammask which is a deeplearning model and optical flow algorithm which
is a computer vision solution, this module might be placed on a desktop pc with good gpu.

2- Streamer: this module is about streaming rgb data between the android app and the Core algorithms which might reside on a desktop pc.

3- Android app: conatins the necessary code for the interactive gui on the android.

A fourth module is under work which is going to be called "Tracking storage" that handles saving already tracked objects for later detection and tracking without having to reindicate them again.
