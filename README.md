# Android-FollowMeProject
Follow Me project was my highschool final year project for the matriculation score of software engineering.

## prerequisites
- Android studio version Flamingo | 2022.2.1 patch 1 (last version the app was tested on).
- DJI drone physically nearby (preferably a Spark since it was tested on it extensively).
- Being in outside, with clear skies in order for the GPS sensors to be most accurate. 
- SDK key from DJI website.
- Firebase project and google-services.json downloaded.

## Installation
1. Clone the project
2. Setup a new project in Firebase, the app uses Firestore, Storage and Authentication features.
3. Paste the google-services.json file in the root folder of the project.
4. Paste the SHA-1 and SHA-256 keys in Firebase project setting in the website
5. Paste the SDK key you got from DJI website in AndroidManifest.XML
6. Update the gradle and dependencies as needed.

## Usage
While using the app the main feature is the Flight activity which include several settings and options for your usage.
![Flight Activity](https://user-images.githubusercontent.com/24898815/236634389-cc78c263-9db5-4ea1-bfe5-a1e69b24d4e9.png)


1. Settings gear will allow you to determine the flight speed and altitude of the drone while following you.
2. Press on the image button that says taking off/ landing the drone.
3. Press on the button between the settings gear and landing/taking off in order to start the following. In order to stop the following press again on it.
4. If for any reason you need the drone to land ASAP press on the red button in the bottom right of the screen.
- You can change the settings mid flight (check out for obstacles or people in the way)
- Instead of the black background a live feed from the drone's camera will show.
- The circular red button in the center of the lower part of the screen allows you to take a picture or film a video from the drone's camera.
5. You can check out your flight's media after landing your drone by pressing on the gallery tab

## Notes
- The app uses DJI SDK and therefore might be obselete at one point
- The drone might not recognize obstacles or people visually so make sure the sensors are working in order to prevent accidents
- There might be needed some changes if using any drone besides DJI Spark or Mavic 
