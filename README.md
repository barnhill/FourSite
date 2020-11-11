# FourSite ![Android CI](https://github.com/barnhill/FourSite/workflows/Android%20CI/badge.svg) [![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)

This application was created as an example application that will allow you to search for venues close to your current location.  It is an example of how to use a Room database for persistance, navigation, calling and parsing from an API using Retrofit and GSON, permission request handling, and last but not least using Google maps to show locations to a user.

## To Run the application
This project uses the Gradle build system. To build this project, use the gradlew build command or use "Import Project" in Android Studio.

There are two Gradle tasks for testing the project:

`installDebug` - for installing the project on a connected device

`connectedAndroidTest` - for running tests on a connected device (still in the process of adding more tests)

# Screens

The first screen will allow you to search for venues near your current location and displays them in a list.  It includes some information about the place and how far it is from you.  Users are allowed to favorite places from here by tapping on the star icon.  Tapping on a venue will take you to the detail screen.  Tapping the floating action button will take you to the full map screen.

<img src="/images/example.png" width="318" height="661" />

The full map screen will allow the user to get a visual of all the places in the search results mapped on the same map with a different colored pin for your current location.  Tapping on a pin will display the name.  Clicking on this name will navigate to the details screen.

<img src="/images/fullmap.png" width="318" height="661" />

The details screen will display a pin on the map indicating your current position on the top half along with venues details.  This screen also shows some information about the selected venue (rating, address and a button to navigate to their website if its available), phone call capability, and favoriting/defavoriting a venue.

<img src="/images/details.png" width="318" height="661" />

## Libraries Used
  * [Android Jetpack][0] - Components for core system capabilities
  * [Test][4] - An Android testing framework for unit and runtime UI tests.
  * [LiveData][13] - Build data objects that notify views when the underlying database changes.
  * [Room][16] - Access your app's SQLite database with in-app objects and compile-time checks.
  * [ViewModel][17] - Store UI-related data that isn't destroyed on app rotations.
  * [Google Maps][18] - static and dynamic maps, Street View imagery, and 360° views
  * [Glide][90] - for asynchronous image loading
  * [Retrofit][91] - for making requests and recieveing data from a RESTful API
  * [OKHttp][92] - for use with Retrofit as the HTTP client
  * [RxJava][93] - a library for composing asynchronous and event-based programs using observable sequences

[0]: https://developer.android.com/jetpack/foundation/
[4]: https://developer.android.com/training/testing/
[13]: https://developer.android.com/topic/libraries/architecture/livedata
[16]: https://developer.android.com/topic/libraries/architecture/room
[17]: https://developer.android.com/topic/libraries/architecture/viewmodel
[18]: https://developers.google.com/maps/documentation/
[90]: https://bumptech.github.io/glide/
[91]: https://square.github.io/retrofit/
[92]: https://square.github.io/okhttp/
[93]: https://github.com/ReactiveX/RxJava
