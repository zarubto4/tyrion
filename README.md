﻿
  # Tyrion #
  
  Back-end server for Becki, managing **Code,** **Blocko** and **Grid.** Server is used for interaction with **PostgreSQL** DB.
  
  Server also keeps user information and does the billing.
  
  This server knows all wanted states that **Hardware** or **Instances** should be in and gives directives if it is needed.
  
  ### DEV mode ###
  #### Prerequisites ####
  
  - [Java JDK or SDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
  - [Java Cryptography Extension - Unlimited Strength](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) 
  - [Play! Framework 2.4 (activator minimal)](https://playframework.com/documentation/2.4.x/Installing)
  - [PostgreSQL](https://www.postgresql.org/download/)
  
  #### Setup ####
  
  - Install *Java* and *JCE - Unlimited Strength* policy files (only replace files in your_java/lib/security folder)
  - (You might have to add location of java.exe and javac.exe to JAVA_HOME or PATH variable)
  - Install PostgreSQL and create password "admin" for superuser "postgres" and create DB "ByzanceERP"
  - In root of the application run terminal command "activator run" (might have to add path of your activator/bin to PATH variable)
  
  #### Tips ####
  
  - Command "activator clean" removes the compiled application. (can be helpful if the application is somehow badly compiled)
  - For building the production version, use command "activator dist"