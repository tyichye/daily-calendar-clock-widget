# Daily Calendar Clock Widget
An android widget designed for to visualize your google daily calendar in a round clock style visualization.
At any given moment, one look is enough for you to know your daily status.


<p align="center">
  <img src="docs/clock_screenshot.jpeg" width="200">
  <img src="docs/configuration_screenshot.jpeg" width="200">
</p>


## Project installation

**1. Install Android Studio** <br/>
Follow the instructions in https://developer.android.com/studio/install (version 4.1 or above)  <br/>

**2. Clone the project** <br/>
Open new folder and run ```git clone https://github.com/tyichye/daily-calendar-clock-widget.git```  <br/>

**3. Change Launch Options** <br/>
Open the project in android studio, go to "Run -> Edit Configuratins -> Launch Options" and change launch value to "Nothing" <br/>

**4. (Optional): Install Android Emulator and Android Virtual Device** <br/>
**Note:** This step is relevant to you if you want to run the widget on a virtual device instead on your real phone. <br/>
**4.1 Install Android Emulator** - follow the instructions in https://developer.android.com/studio/run/emulator  <br/>
**4.2 Install AVD** - follow the instructions in https://developer.android.com/studio/run/managing-avds <br/>

**5. Install widget on your device** <br/>
Connect your device or use your AVD and click **Run** on android studio. <br/>
**Note:** If you are using AVD you need to connect it to your google account in order to see events on the widget.


### Build
GUI way: in android studio go to "Build -> Rebuild Project"  
CLI way: ```./gradlew build```


<br/>


## Widget installation
- Download the apk file to your phone by [clicking here](https://github.com/tyichye/daily-calendar-clock-widget/raw/main/release_apk/round_calendar_v1.0.apk) or by scanning this QR code with your phone!<br/>
![5d2d02a21da9de5f3d5029228449430d](https://user-images.githubusercontent.com/67260757/121817330-6ad71b00-cc89-11eb-922d-077b80767f9b.png)



- After tapping on the apk file you downloaded android will ask permissions to install unknown apps - allow it and than click Install.
- Add the widget to your home screen


<br/>


## Analogues
* [Sectograph](https://play.google.com/store/apps/details?id=prox.lab.calclock) - 24-hours view is paid
* [Daily Time Planner With Clock Widget](https://play.google.com/store/apps/details?id=com.sectograph.planner.time.clock.manager.reminder) - paid application
* [Foraday](https://play.google.com/store/apps/details?id=com.compscieddy.foradayapp) - no connection to Google Calendar
* [Slice Planner](https://play.google.com/store/apps/details?id=com.evopaper.sliceplanner&hl=ru) - no widget
* [CloudCal Calendar Agenda Planner Organizer To Do](https://play.google.com/store/apps/details?id=net.cloudcal.cal) - non-informative widget
