# Compiling and running the application with IntelliJ.

1. Download and install IntelliJ IDEA.  
You can download the free Community Edition.
https://www.jetbrains.com/idea/download

2. Download ArduinoImageCapture from Github.

3. Open IntellJ and click on "Import Project"  
![importproject](https://user-images.githubusercontent.com/1666508/48670176-bc43bb80-eb1b-11e8-95be-7bb644be81e1.png)  
Or if you already have an open project then "File"->"New"->"Project from Existing Sources..."  
![filenew](https://user-images.githubusercontent.com/1666508/48670252-640db900-eb1d-11e8-9ce7-36674f8d4a74.png)  

Next->Next->Next->Next->Next->  
(Add/Select your Java version. For me it was "C:\Program Files\Java\jdk1.8.0_131")  
Next -> Finish  

5. Add new run configuration  
  
- In the upper right corner click on the small arrow and then "Edit Configurations.."  
![editconfiguration](https://cloud.githubusercontent.com/assets/1666508/25311654/91ed3670-280f-11e7-98ca-6beaa1be4261.png)  

- Click on the "+" and select "Application"  
![addnew](https://cloud.githubusercontent.com/assets/1666508/25311665/d6772e7c-280f-11e7-99e0-222a3fbe2fcc.png)

- Name the configuration and select main java class
![selecttarget](https://cloud.githubusercontent.com/assets/1666508/25311682/20783886-2810-11e7-8ae7-6ecf5cee3a85.png)

- Add java external library path (for rxtx library dll file) to the VM options field:
![selecttarget](https://user-images.githubusercontent.com/1666508/48670234-f06bac00-eb1c-11e8-8a75-7b30c7bdf2e9.png)
Since I have 64bit Java for windows I had to add:  
-Djava.library.path=lib/rxtx-2.2pre2-bins/win64  
For MacOS:  
-Djava.library.path=lib/rxtx-2.2pre2-bins/mac-10.5  
  
  
- Now you should be able to run the application by clicking the green arrow in the upper right corner.
![run](https://cloud.githubusercontent.com/assets/1666508/25311701/6b3b2c0c-2810-11e7-8d70-1218eb92da40.png)

- Application window should open
![mainwindow](https://cloud.githubusercontent.com/assets/1666508/25311714/983cd91c-2810-11e7-9827-48360fd00de5.png)

<br>
<br>
<br>
<br>
<br>

----

# Building ArduinoImageCapture.jar
1. Select "File"->"Project Structure..."  
Add new Artifact "JAR":
![New Artifact](https://user-images.githubusercontent.com/1666508/54083596-38ae1480-432e-11e9-8082-f44c33d7e48f.png)
  
2. Select "MainWindow" as main class and click OK
![Main Class](https://user-images.githubusercontent.com/1666508/54083600-3ba90500-432e-11e9-8766-0b559ee70aa8.png)
  
3. Select "Build"->"Build Artifacts..."
Then select "ArduinoImageCapture:jar"->"Build"
![Build Jar](https://user-images.githubusercontent.com/1666508/54083601-3d72c880-432e-11e9-957e-9e78611b135c.png)
  
4. Now There should be "ArduinoImageCapture.jar" in "out/artifacts/ArduinoImageCapture_jar" folder
Copy your platform specific "rxtxSerial.dll" next to the "ArduinoImageCapture.jar".
![Copy DLL](https://user-images.githubusercontent.com/1666508/54083602-3ea3f580-432e-11e9-98ed-cc9c6866e9f1.png)
  
5. Now if you open "out/artifacts/ArduinoImageCapture_jar" in Explorer you should be able to double click on the "ArduinoImageCapture.jar" and program should open
![Double-Click](https://user-images.githubusercontent.com/1666508/54083689-d35b2300-432f-11e9-96f1-3772a4457e7e.png)




