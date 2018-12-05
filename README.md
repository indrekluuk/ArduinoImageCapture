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


