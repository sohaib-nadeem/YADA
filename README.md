# YADA

## Description
YADA (Yet Another Drawing App) is an android app that allows multiple users to draw on a canvas together in real-time, making sharing ideas easier and more interactive.

Significant features include:
- Drawing and erasing on the canvas, with different strokes width and colors
- Clearing the entire canvas
- Undo and Redo actions
- Import an image and edit on it
- Export the canvas as a pdf
- Online sessions to collaborate with others in real-time 
- Zoom and pan (drag) on the canvas
- Draw shapes (rectangles, circles, and straight lines) and move them around

## Team Members
Akshita Choudhury (a49choud@uwaterloo.ca),
David Mo (d7mo@uwaterloo.ca),
Sirius Hou (y45hou@uwaterloo.ca),
Sohaib Nadeem (s26nadee@uwaterloo.ca)

## Installation Instructions
To install the app on your android device or emulator, go to the releases directory and download the latest apk file (directly from the link or transfer it to your device). Then, to install it on your physical android device, simply select the apk file in your Files app to open a prompt to install the device and select "Install" (If not already done, you will have to change your settings first to allow your device to install unknown apps from this source). To install it on your emulator in Android Studio, you can drag the apk file from your file explorer onto the virtual device in Android Studio while the virtual device is running.
The server has been deployed on GCP (Google Cloud Platform) and the server's IP address has been set on the application so the app should automatically be able to communicate with the server (without any additional work) once it is installed.

**Note:** The app was tested on the following devices:
- Virtual Devices:
    - Google Pixel 3a, Android 14 (API 34)
- Physical Devices:
    - OnePlus 9R, Android 11
    - Samsung S22 ultra, Android 13

## Software Releases
* version 0.1 ([release notes](releases/v0.1-release-notes.md), [apk](releases/v0.1-build.apk))
* version 0.2 ([release notes](releases/v0.2-release-notes.md), [apk](releases/v0.2-build.apk))
* version 0.3 ([release notes](releases/v0.3-release-notes.md), [apk](releases/v0.3-build.apk))
* version 0.4 ([release notes](releases/v0.4-release-notes.md), [apk](releases/v0.4-build.apk))
* version 1.0 ([release notes](releases/v1.0-release-notes.md), [apk](releases/v1.0-build.apk))

## Project Links
- [Project Proposal](https://git.uwaterloo.ca/s26nadee/cs346-project/-/wikis/Project-Proposal)
- Meeting minutes: [Sprint 1](https://git.uwaterloo.ca/s26nadee/cs346-project/-/wikis/Sprint-1-Meeting-Minutes), 
[Sprint 2](https://git.uwaterloo.ca/s26nadee/cs346-project/-/wikis/Sprint-2-Meeting-Minutes), [Sprint 3](https://git.uwaterloo.ca/s26nadee/cs346-project/-/wikis/Sprint-3-Meeting-Minutes), [Sprint 4](https://git.uwaterloo.ca/s26nadee/cs346-project/-/wikis/Sprint-4-Meeting-Minutes)
- [Requirements](https://git.uwaterloo.ca/s26nadee/cs346-project/-/wikis/Requirements)
- [Architecture & Design](https://git.uwaterloo.ca/s26nadee/cs346-project/-/wikis/Architecture-and-Design)
- [Reflections on practices](https://git.uwaterloo.ca/s26nadee/cs346-project/-/wikis/Reflections-on-practices)
- Software Releases and Installation Instructions  are mentioned above

## Acknowledgment
Thanks to:
- [Google Fonts](https://fonts.google.com/icons) for the icons used in the Toolbar and UpperBar
- [The Android Open Source Project](https://source.android.com/) for the source code of PointerInputScope.detectTransformGestures
- [Patil Shreyas](https://github.com/PatilShreyas) for the [Capturable](https://github.com/PatilShreyas/Capturable) composable

## [License](LICENSE.txt)

## Project status
See the Project Plan section in the [Project Proposal](https://git.uwaterloo.ca/s26nadee/cs346-project/-/wikis/Project-Proposal) for planned features and current status. Development from the original team (under the Team Members section) is planned to stop on Dec 5, 2023.
