# Launcher modification (for Windows)

1. Install [Apktool](https://apktool.org/docs/install/) and [Python](https://www.python.org/downloads/).

2. Go to the folder with your `Launcher.apk`, press Shift + right click and open `PowerShell` from the context menu.

3. In `PowerShell` paste this command: `apktool d Launcher.apk` and press enter. Wait till Apktool finishes the job.

4. Now you should see the folder `Launcher` containing disassembled launcher app. Go to the directory `Launcher\smali\com\fyt\car` and open `MusicService.smali` with text editor (i.e. Notepad++).

5. Compare the code to the following examples. If it looks like on this [screenshot](./images/smali1.png) then you can proceed to the next step. If it looks like [that](./images/smali2.png) then it means that the code in your launcher is [obfuscated](https://www.google.com/search?q=obfuscated+code) and there's little we can do. Simply choose another launcher.

6. Download and unpack [launcher_cooker.rar](https://github.com/vasyl91/DisplayMediaTitles/releases/download/launchers/launcher_cooker.rar). Copy and paste `MusicService.smali` to the unpacked `launcher_cooker` folder (it should be in the same folder with `additional_method.smali` and `launcher_cooker.py`).

7. Open `PowerShell` in that folder the same way as in the step 2. and run the command `py .\launcher_cooker.py`.

8. In the newly created `output` folder you will find the modified `MusicService.smali`, use it to replace the original file in the directory `Launcher\smali\com\fyt\car`.

9. Repeat step 2. and 3. but this time use the command `apktool b Launcher` which will assembly the app back to the `.apk` file.

10. Go to the directory `Launcher\dist` where you'll find modified `Launcher.apk`. 

11. Now sign your `Launcher.apk` with proper keystore. That might be tricky for users that don't have installed Android SDK.

	Here is the exemple of Android SDK directory `C:\Users\<your_user-name>\AppData\Local\Android\Sdk\build-tools\34.0.0`. First steps will happen there.

	1. If you have installed android SDK then your Environment is probably already created, so just copy and paste `apksigner.jar` from `\AppData\Local\Android\Sdk\build-tools\<build_number>\lib` to `AppData\Local\Android\Sdk\build-tools\<build_number>` and proceed to the step 12.

	2. If it's your first time with Android then you can either install [Android Studio](https://developer.android.com/studio?hl=en) and do previous step or just set smaller packege available to download [here](https://androidsdkmanager.azurewebsites.net/build_tools.html) without downloading Android Studio.

	3. If you chose to download smaller package then create such directory `C:\Users\<your_user-name>\AppData\Local\Android\Sdk\build-tools\<build_number>` and unpack it's contents there so it looks like on this [screenshot](./images/buildtools.png). Copy-paste `apksigner.jar` as in subpoint 1.

	4. [Create or edit](https://docs.oracle.com/cd/E83411_01/OREAD/creating-and-modifying-environment-variables-on-windows.htm#OREAD158) environment variable `PATH` for user and for system using this dir `%LOCALAPPDATA%\Android\Sdk\build-tools\<build_number>` axactly as on this [screenshot](./images/variablepath.png).

	5. If the substeps above don't work then you'll have to google it and solve proper Android SDK installation by yourself.

		<code style="color : red">!!! Remember to replace **<your_user-name>** and **<build_number>** with proper content (see the example screenshots) !!!</code>


12. Copy and paste `Launcher.apk` from step 10. to the `launcher_signer` folder (it should be in the same folder with `platform.jks`).

13. Open `PowerShell` in that folder the same way as in the step 2. and try to run the command:
	 
	 `apksigner sign --ks <your_directory>\launcher_signer\platform.jks Launcher.apk ` 

	If it doesn't work try this command:

	`C:\Users\<your_user-name>\AppData\Local\Android\Sdk\build-tools\<build_number>\apksigner sign --ks <your_directory>\launcher_signer\platform.jks launcher.apk`

	`PowerShell` will ask you for the pasword, type `android` (it won't show up in the terminal) and press Enter. Here is the exemplary [screenshot](./images/powershell.png).

	<code style="color : red">!!! Remember to replace **<your_user-name>**, **<build_number>** and **<your_directory>** with proper content (see the example screenshots) !!!</code>

14. Now you should find in the `launcher_signer` folder `launcher.apk.idsig` which you can remove and signed `Launcher.apk` (it has the same name as before signing).

15. Install signed `Launcher.apk` on your device and have fun with working status bar titles from stock player.