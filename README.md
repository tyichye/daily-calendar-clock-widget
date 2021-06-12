# Daily Calendar Clock Widget
An android widget designed for to visualize your daily Google Calendar events in a round, clock-style visualization.
One look is all you need to know your daily status.


<p align="center">
  <img src="docs/clock_screenshot.jpeg" width="200">
  <img src="docs/configuration_screenshot.jpeg" width="200">
</p>


## Project installation

**1. Install Android Studio** <br/>
Follow the instructions on https://developer.android.com/studio/install  <br/>

**2. Clone the project** <br/>
Upon launching Android Studio, select `Project from version control` and provide the URL of this repo (or of your fork, for better GitHub integration)

**3. Change Launch Options** <br/>
Open the project in android studio, go to `Run` -> `Edit Configuratins` -> `Launch Options` and change `Launch` to select `Nothing` <br/>

**4. (Optional): Install Android Emulator and Android Virtual Device** <br/>
**Note:** This step is relevant to you if you want to run the widget on a virtual device instead on your real phone. <br/>
**4.1 Install Android Emulator** - follow the instructions in https://developer.android.com/studio/run/emulator  <br/>
**4.2 Install AVD** - follow the instructions in https://developer.android.com/studio/run/managing-avds <br/>

**5. Install widget on your device** <br/>
Connect your device or use your AVD and click **Run** on android studio. <br/>
**Note:** A Google account is required to show events on the widget.


### Build 
On Android Studio, click `Build -> Rebuild Project`.

Alternatively, execute `./gradlew build` from terminal.


<br/>


## Widget installation
- Download the apk file to your phone from release_apk folder (Alternatively, `Run` via android studio and confirm installation on the device)
- Confirm installing an application of "unknown source".
- Add the widget to your screen (This is usually done by long-clicking an empty slot in the home screen)


<br/>


## Analogues
* [Sectograph](https://play.google.com/store/apps/details?id=prox.lab.calclock) - 24-hours view is paid
* [Daily Time Planner With Clock Widget](https://play.google.com/store/apps/details?id=com.sectograph.planner.time.clock.manager.reminder) - paid application
* [Foraday](https://play.google.com/store/apps/details?id=com.compscieddy.foradayapp) - no connection to Google Calendar
* [Slice Planner](https://play.google.com/store/apps/details?id=com.evopaper.sliceplanner&hl=ru) - no widget
* [CloudCal Calendar Agenda Planner Organizer To Do](https://play.google.com/store/apps/details?id=net.cloudcal.cal) - non-informative widget
