from PyQt5.QtWidgets import QApplication, QWidget, QPushButton, QSlider, QScrollBar, QVBoxLayout, QHBoxLayout, QScrollArea, QLabel, QGroupBox, QFormLayout, QFileDialog
from PyQt5.QtCore import Qt
from PyQt5.QtGui import QPixmap, QImage
from PyQt5 import QtCore, QtGui, QtWidgets


# class Ui_MainWindow(QtWidgets.QMainWindow):
#     def __init__(self):
#         super(Ui_MainWindow, self).__init__()
#         self.setupUi(self)

#     def setupUi(self, MainWindow):
#         MainWindow.setObjectName("MainWindow")
#         _translate = QtCore.QCoreApplication.translate
#         MainWindow.setWindowTitle(_translate("MainWindow", "MainWindow"))
#         self.centralwidget = QtWidgets.QWidget(MainWindow)
#         self.centralwidget.setObjectName("centralwidget")
#         self.gridLayout = QtWidgets.QGridLayout(self.centralwidget)
#         self.gridLayout.setObjectName("gridLayout")

#         # camera
#         self.camera_view = QLabel(self)

#         # exit button
#         self.exitButton = QtWidgets.QPushButton(self.centralwidget)
#         self.exitButton.setObjectName("exitButton")
#         self.exitButton.setText(_translate("MainWindow", "Exit"))

#         self.gridLayout.addWidget(self.exitButton, 1, 0, 1, 3)




#         MainWindow.setCentralWidget(self.centralwidget)
#         self.menubar = QtWidgets.QMenuBar(MainWindow)
#         self.menubar.setGeometry(QtCore.QRect(0, 0, 800, 22))
#         self.menubar.setObjectName("menubar")
#         MainWindow.setMenuBar(self.menubar)
#         self.statusbar = QtWidgets.QStatusBar(MainWindow)
#         self.statusbar.setObjectName("statusbar")
#         MainWindow.setStatusBar(self.statusbar)


#         QtCore.QMetaObject.connectSlotsByName(MainWindow)


from PyQt5 import QtCore, QtGui, QtWidgets

class Ui_MainWindow(QtWidgets.QMainWindow):
    def __init__(self):
        super(Ui_MainWindow, self).__init__()
        self.setupUi(self)
        # self.bindSignals()


    def setupUi(self, MainWindow):
        MainWindow.setObjectName("MainWindow")
        MainWindow.resize(800, 600)
        sizePolicy = QtWidgets.QSizePolicy(QtWidgets.QSizePolicy.Preferred, QtWidgets.QSizePolicy.Preferred)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(MainWindow.sizePolicy().hasHeightForWidth())
        MainWindow.setSizePolicy(sizePolicy)
        MainWindow.setToolButtonStyle(QtCore.Qt.ToolButtonTextOnly)
        self.centralwidget = QtWidgets.QWidget(MainWindow)
        self.centralwidget.setObjectName("centralwidget")
        self.gridLayoutWidget = QtWidgets.QWidget(self.centralwidget)
        self.gridLayoutWidget.setGeometry(QtCore.QRect(10, 10, 800, 600))
        self.gridLayoutWidget.setObjectName("gridLayoutWidget")
        self.gridLayout = QtWidgets.QGridLayout(self.gridLayoutWidget)
        self.gridLayout.setContentsMargins(0, 0, 0, 0)
        self.gridLayout.setObjectName("gridLayout")
        self.exit_BP = QtWidgets.QPushButton(self.gridLayoutWidget)
        self.exit_BP.setObjectName("exit_BP")
        self.gridLayout.addWidget(self.exit_BP, 1, 5, 1, 1)

        self.frame = QLabel(self.gridLayoutWidget)
        self.frame.setObjectName("frame")
        self.gridLayout.addWidget(self.frame, 2, 1, 9, 6)

        self.label = QtWidgets.QLabel(self.gridLayoutWidget)
        self.label.setFrameShadow(QtWidgets.QFrame.Raised)
        self.label.setAlignment(QtCore.Qt.AlignCenter)
        self.label.setObjectName("label")
        self.gridLayout.addWidget(self.label, 1, 0, 1, 1)
        self.port_line = QtWidgets.QLineEdit(self.gridLayoutWidget)
        self.port_line.setObjectName("port_line")
        self.port_line.setText("8888")
        self.gridLayout.addWidget(self.port_line, 1, 3, 1, 1)
        self.connect_PB = QtWidgets.QPushButton(self.gridLayoutWidget)
        self.connect_PB.setObjectName("connect_PB")
        self.gridLayout.addWidget(self.connect_PB, 1, 4, 1, 1)
        self.label_2 = QtWidgets.QLabel(self.gridLayoutWidget)
        self.label_2.setObjectName("label_2")
        self.gridLayout.addWidget(self.label_2, 1, 2, 1, 1)
        self.IP_line = QtWidgets.QLineEdit(self.gridLayoutWidget)
        self.IP_line.setObjectName("IP_line")
        self.IP_line.setText("172.16.17.188")
        self.gridLayout.addWidget(self.IP_line, 1, 1, 1, 1)
        self.stop_tracking_PB = QtWidgets.QPushButton(self.gridLayoutWidget)
        self.stop_tracking_PB.setEnabled(False)
        self.stop_tracking_PB.setObjectName("stop_tracking_PB")
        self.gridLayout.addWidget(self.stop_tracking_PB, 5, 0, 1, 1)
        self.status_label = QtWidgets.QLabel(self.gridLayoutWidget)
        sizePolicy = QtWidgets.QSizePolicy(QtWidgets.QSizePolicy.Preferred, QtWidgets.QSizePolicy.Fixed)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.status_label.sizePolicy().hasHeightForWidth())
        self.status_label.setSizePolicy(sizePolicy)
        self.status_label.setAlignment(QtCore.Qt.AlignCenter)
        self.status_label.setObjectName("status_label")
        self.gridLayout.addWidget(self.status_label, 4, 0, 1, 1)
        self.groupBox = QtWidgets.QGroupBox(self.gridLayoutWidget)
        sizePolicy = QtWidgets.QSizePolicy(QtWidgets.QSizePolicy.Preferred, QtWidgets.QSizePolicy.Preferred)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.groupBox.sizePolicy().hasHeightForWidth())
        self.groupBox.setSizePolicy(sizePolicy)
        self.groupBox.setObjectName("groupBox")
        self.mask_RB = QtWidgets.QRadioButton(self.groupBox)
        self.mask_RB.setGeometry(QtCore.QRect(10, 30, 61, 17))
        self.mask_RB.setChecked(False)
        self.mask_RB.setObjectName("mask_RB")
        self.box_RB = QtWidgets.QRadioButton(self.groupBox)
        self.box_RB.setGeometry(QtCore.QRect(10, 60, 41, 17))
        self.box_RB.setChecked(True)
        self.box_RB.setObjectName("box_RB")
        self.gridLayout.addWidget(self.groupBox, 2, 0, 2, 1)
        MainWindow.setCentralWidget(self.centralwidget)
        self.menubar = QtWidgets.QMenuBar(MainWindow)
        self.menubar.setGeometry(QtCore.QRect(0, 0, 800, 21))
        self.menubar.setObjectName("menubar")
        MainWindow.setMenuBar(self.menubar)
        self.statusbar = QtWidgets.QStatusBar(MainWindow)
        self.statusbar.setObjectName("statusbar")
        MainWindow.setStatusBar(self.statusbar)

        self.retranslateUi(MainWindow)
        QtCore.QMetaObject.connectSlotsByName(MainWindow)

    

import numpy as np
if __name__ == "__main__":  

    data = np.ones((1024,1024,3), np.uint8)*80
    data.data
    app = QApplication([])
    ui = Ui_MainWindow()
    ui.draw(data)
    ui.show()
    app.exec_()

