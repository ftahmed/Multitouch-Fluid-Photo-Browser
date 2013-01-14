Multitouch-Fluid-Photo-Browser
==============================

The fluid photo browser is a prototype of multi-device application for sharing pictures. The goal is to offer the possibility to move photos from a smart phone holding personal information, to a shared/public tabletop where many people can browse the photos all together. Additionally, application supports log-in and payment functions using NFC.

User Scenario
-------------

"Let’s imagine a future photo printing agency located at Nørreport, specializing on high-quality printing and manual post-processing of digital images. Customers can come in with WLAN equipped photo devices (WiFi-enabled dedicated cameras or cellular phones with embedded camera), easily push selected photos from their personal device onto a tabletop device (”fluid photo browser”) which allows the professional photo expert and the customer to discuss and preview possible enhancements of the selected photos before printing them. After having placed the order, the customers get notified through email when their prints are ready and can pay and pick them up using their NFC enabled phone.

Max just came back from a crazy week-end. He wants to get a few of his pictures printed so that he can frame them and send others to his friends. But he also has some pictures he definitely wants to keep for himself. Max goes to the print photo print shop by Nørreport. When he gets in, he fires his fluid photo browser application on his NFC-enabled cell phone, places the phone on the NFC active area of the cashiers’ counter to associate the phone with the counter which then turns on, waiting to receive pictures. Max browses the pictures on his phone, and sends the ones he wants to get printed to the counter with a gesture. He discusses the printing details with a technician such as cropping, filtering, or printing quality.

… the technician applies a few tricks to the pictures before sending them to print …

Max sends the edited pictures to his cell phone and checks out. The check-out procedure means in practice that Max identifies himself and associate himself with the printing order using his NFC-enabled phone by placing it on the NFC-enabled area on the fluid photo browser. He will get an email once the photo printouts are ready and can then pick them up anytime (24/7) in the NFC-enabled “photo retrieval box” hanging outside the store by just holding up her/his NFC enabled phone to the box and agree to pay for the photos. The box automatically withdraws money from his NFC connected bank account corresponding to the number of photos he has got and spits out the printed photos." [Source] [1]

Implementation
--------------

* Multitouch, tabletop application was created using [MT4j framework](http://www.mt4j.org).
* The client for tabletop application was implemented for Android 2.2 or highier.

Implemented features
-------------------
Client:
- Display an image gallery on the Android phone.
- Listen to flick gestures on the images (vertical scroll event).
- Send the image data to tabletop application (uses a fixed IP on a local network).
- Receive image data from tabletop

Tabletop application:
- Receive images from Android and display them after having identified the sender device through NFC (log-in to app through NFC).
- Define your own gesture to send image data back to Android device.
- Check out by putting NFC phone (or card/tag) on the NFC area of the tabletop device. When this happens, the "coins" on the NFC tag are reduced with 1 coin per picture sent to the Android phone, simulating payment operation.

How to run Fluid Photo Browser?
-------------------------------
In order to run this project on your computer you need:
- NFC reader/writer that is connected to your computer. The project is set up for [ACR122U](http://www.acs.com.hk/index.php?pid=product&id=ACR122U) but it will probably support most of the available USB NFC devices.
- matching tag/card. My card was written so that 1. block of 1. sector was holding my name: 'WIKTOR' and second block of the same sector was holding integer representing the amount of coins used for payment (for example: 100). You can write to your card the same way or you can modify 'NFCUtil.java' (line: 160) to match your own name.
- set up IP's in Android project and tabletop application to allow communication.
- deploy Android application on your device.
- run tabletop application by launching mt4-desktop/FluidPhotoBrowser/itu.assignments.fluidphotobrowser.FluidPhotoStart.java

[1]: https://blog.itu.dk/SPCT-F2012/lab-classes/assignment-2/ "Pervasive Computing Assignment"
