## This Branch
- dynamic chips on Facility Page, so that current day is the first chip
- updated files/locations:
  - added date to Facility Page layout
  - FacilityPage: dynamically adds the chips using for loop
  - FluxUtil: 2 new methods
    - `intOfDay` - get corresponding integers (0 for Sunday..6 for Saturday) 
    - `dateOfDay` - returns date of given day in a specified format (SimpleDateFormat)
  

# Flux Android

by [Cornell Design Tech Initiative](https://www.cornelldti.org/)

<a href='https://play.google.com/store/apps/details?id=org.cornelldti.density.density&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width='250px'/></a>

## Contents
  - [About](#about)
  - [Getting Started](#getting-started)
  - [Dependencies & Libraries](#dependencies--libraries)
  - [External Documentation](#external-documentation)
  - [Screenshots](#screenshots)
  - [Contributors](#contributors)
​
## About
An Android and iOS mobile app designed to illustrate real-time activity levels of dining halls and eateries on campus. This is the go-to tool for avoiding peak levels of crowdedness at your favorite dining facilities on Cornell's campus. This repository shows the project files for the Android app.
​
## Getting Started
You will need Android SDK Version 28 to run the latest version of this app, which uses the following. However this app is built to support a minimum Android SDK Version of 21. You will also need the Android Studio IDE to run the project.

_Last updated **02/08/2019**_.
​
## Dependencies & Libraries
 * [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - a 3rd party library that allows us to easily input data and generate various graphs and charts to visualize it. 
 * [Volley](https://developer.android.com/training/volley/) - an Android Library that allows us to make HTTP Requests in a more simplified and abstracted manner using library functions.
​
## External Documentation
* [Backend API Documentation](https://campusdensity.docs.apiary.io/) - this is an external Apiary documenting the endpoints for our application.
​
## Screenshots
​
_Screenshots showing major parts of app_
​

<img src="https://raw.githubusercontent.com/cornell-dti/campus-density-android/master/app-screenshots/screenshot1.png" width="250px" style="margin: 10px; border: 1px rgba(0,0,0,0.4) solid;"> <img src="https://raw.githubusercontent.com/cornell-dti/campus-density-android/master/app-screenshots/screenshot3.png" width="250px" style="margin: 10px; border: 1px rgba(0,0,0,0.4) solid;"> <img src="https://raw.githubusercontent.com/cornell-dti/campus-density-android/master/app-screenshots/screenshot2.png" width="250px" style="margin: 10px; border: 1px rgba(0,0,0,0.4) solid;">

​
## Contributors
​
**School Year**
### **2018-2019**

 * **Neha Rao** - Product Manager
 * **Andrew Xiao** - Product Manager
 
 * **Evan Welsh** - Backend Developer
 * **Raymone Radi** - Backend/iOS Developer
 * **Ashneel Das** - Backend Developer
 * **Matthew Coufal** - iOS Developer
 * **Kaushik Ravikumar** - Android Developer
 * **Andrew Gao** - Android Developer
 
 * **Kaitlyn Son** - Designer
 * **April Ye** - Designer
 * **Kathy Wang** - Designer
​

We are a team within **Cornell Design & Tech Initiative**. For more information, see our website [here](https://cornelldti.org/).
<img src="https://raw.githubusercontent.com/cornell-dti/design/master/Branding/Wordmark/Dark%20Text/Transparent/Wordmark-Dark%20Text-Transparent%403x.png">
​
