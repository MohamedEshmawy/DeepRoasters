import cv2
import numpy as np

cap = cv2.VideoCapture(0)

# Create old frame
ret, frame = cap.read()
old_gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

# Lucas kanade params
## winSize is the window size ,we increase this area to find bigger movement
## maxLevel = 4 => level of pyramids so it can detect faster movements
lk_params = dict(winSize = (20, 20),
            maxLevel = 4,
            criteria = (cv2.TERM_CRITERIA_EPS | cv2.TERM_CRITERIA_COUNT, 10, 0.03))

# Mouse function
def select_point(event, x, y, flags, params):
    #we made point global so we can use it later
    global point, point_selected, old_points
    if event == cv2.EVENT_LBUTTONDOWN:
        #get the x , y of the click
        point = (x, y)
        point_selected = True
        #old points will be the first point that we detect
        old_points = np.array([[x, y]], dtype=np.float32)

#we want to run select_point function on the window,
#we need first to create window
cv2.namedWindow("Frame")
#when click on mouse ,call function select_point , all of this inside the window "Frame"
cv2.setMouseCallback("Frame", select_point)


#we want to add a switch button that means when we detect  point then only then we can start tp track the optical
#flow of such a point ,before there was no point to do that
point_selected = False
point = ()
old_points = np.array([[]])

while True:
    ret, frame = cap.read()
    #we need to work with grayscale frame
    gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    #when we detect the point , draw a circle around it
    if point_selected is True:
        #start_point = (point[0] - 50, point[1] - 50)
        #end_point = (point[0] + 50, point[1] + 50)
        #color = (255, 0, 0)
        #thickness = 2
        #cv2.rectangle(frame, start_point,end_point, color , thickness)
        cv2.circle(frame, point, 5, (0,0, 255), 2)

        #We are working with old_frame and then with new frame ,when we process the new frame so we detect the new
        #points then the new frame becomes the old frame and then we start again the loop
        new_points, status, error = cv2.calcOpticalFlowPyrLK(old_gray, gray_frame, old_points, None, **lk_params)
        old_gray = gray_frame.copy()
        old_points = new_points

        #extracting x, y from new_points using ravel() function
        x, y = new_points.ravel()
        # draw the news_points
        cv2.circle(frame, (x, y), 5, (0, 255, 0), -1)

    #we will make a image pyramid: it is just a representation of the same image making it smaller
    # and each image is quarter of the size of the first one
    #first_level = cv2.pyrDown(frame)
    #second_level = cv2.pyrDown(first_level)

    cv2.imshow("Frame", frame)
    #the fast movement are easier to detect in this smaller image because
    # in the bigger image it moves much more pixels but in the smaller image it moves the same movement looking
    #much shorter
    #cv2.imshow("First level", first_level)
    #cv2.imshow("Second level", second_level)

    key = cv2.waitKey(1)
    #27 is the ESC character
    if key == 27:
    #if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()


