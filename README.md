# Byte-Logic Mod
<img src="https://raw.githubusercontent.com/Zelaux/ByteLogicMod/master/core/assets-raw/sprites/logo/logo-gif.gif" alt="Mod logo" width='25%'></img><br>
Byte Logic is a mod that allows you to create logic circuits from Mindustry blocks.  With these blocks, you can manage your factory the way you would with processors.  This mod contains the content of the original version of Mindustry (byte-logic v5), created by Anuken as the first version of current processors.  This content has been redesigned, improved and adapted for Mindustry v7.
## Mod content
| Block         | Description                                                                                                                                                                           |
|:--------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Signal timer  | Passes the signal after a certain number of ticks.                                                                                                                                    |
| Switch block  | A button that the player can toggle. Outputs a constant signal equal to 0 or 1.                                                                                                       |
| Signal block  | Outputs a constant signal, the value of which can be adjusted.                                                                                                                        |
| Signal node   | Sends a signal to another node.                                                                                                                                                       |
| Signal router | Receives a signal and sends it out to other parties.                                                                                                                                  |
| Analyzer      | Reads block information (number of items, liquids, energy, etc.) and converts it into a signal.                                                                                       |
| Controller    | Turns the block in front of you on and off depending on the input signal.                                                                                                             |
| Relay         | Transmits a signal                                                                                                                                                                    |
| Not gate      | Logical operation "NOT". Inverts the signal.                                                                                                                                          |
| And gate      | Logical operation "AND" (Logical multiplication). Gives 1 if both inputs are 1.                                                                                                       |
| Or gate       | Logical operation "OR" (Logical addition). Gives 1 if at least one of the input signals is 1.                                                                                         |
| Xor gate      | Logical operation "EXCLUSIVE OR". Gives 1 if the input signals are different.                                                                                                         |
| Adder         | Adds the values of the input signals.                                                                                                                                                 |
| Subtractor    | Subtracts the value of one input signal from another. Changes by clicking on the block.                                                                                               |
| Divider       | Divides the value of one input signal by another. Changes by clicking on the block.                                                                                                   |
| Remainder     | Gives the remainder of dividing the value of one input signal by another. Changes by clicking on the block.                                                                           |
| Multiplier    | Multiplies the values of incoming signals.                                                                                                                                            |
| Equalizer     | Checks for equality of the values of incoming signals.                                                                                                                                |
| Comparator    | Tests whether the value of one input signal is greater or less than another. Changes by clicking on the block.                                                                        |
| Font signal   | Depending on the value of the input signal, it displays the character corresponding to the code on the display.                                                                       |
| Display block | 5x5 display. Can be controlled by regular signal or font signal. In the case of control by a normal signal, displays the pixels as the value of the signal would look in binary code. |
## Building jar
### Building for Desktop Testing
1. Install JDK **16**.
2. Run `gradlew jar` [1].
3. Your mod jar will be in the `build/libs` directory. **Only use this version for testing on desktop. It will not work with Android.**
To build an Android-compatible version, you need the Android SDK. You can either let Github Actions handle this, or set it up yourself. See steps below.
### Auto move to mod folders after build (Desktop)
1. Create `modsDirectories.txt` file in mod root
2. add your pathes(you can write `classic` and mod will be moved to the mindustry mods folder) for example:
```
classic
C:/someFolder1/someFolrder2/modsFolder
```
4. Repeat steps from `Building for Desktop Testing`, but use `gradlew mjar` instead of `gradlew jar`
### Building through Github Actions
This repository is set up with Github Actions CI to automatically build the mod for you every commit. This requires a Github repository, for obvious reasons.
To get a jar file that works for every platform, do the following:
1. Make a Github repository with your mod name, and upload the contents of this repo to it. Perform any modifications necessary, then commit and push. 
2. Check the "Actions" tab on your repository page. Select the most recent commit in the list. If it completed successfully, there should be a download link under the "Artifacts" section. 
3. Click the download link (should be the name of your repo). This will download a **zipped jar** - **not** the jar file itself [2]! Unzip this file and import the jar contained within in Mindustry. This version should work both on Android and Desktop.
### Building Locally
Building locally takes more time to set up, but shouldn't be a problem if you've done Android development before.
1. Download the Android SDK, unzip it and set the `ANDROID_HOME` environment variable to its location.
2. Make sure you have API level 30 installed, as well as any recent version of build tools (e.g. 30.0.1)
3. Add a build-tools folder to your PATH. For example, if you have `30.0.1` installed, that would be `$ANDROID_HOME/build-tools/30.0.1`.
4. Run `gradlew deploy`. If you did everything correctlly, this will create a jar file in the `build/libs` directory that can be run on both Android and desktop.
## Future plans
Here are plans and ideas for future versions of the mod
- **Signal extractor** - a block that extracts a signal from a block standing behind
- **Signal manipulator** - sets a fixed signal length (A signal that will disappear after X ticks on the current block)

--- 

*[1]* *On Linux/Mac it's `./gradlew`, but if you're using Linux I assume you know how to run executables properly anyway.*  
*[2]: Yes, I know this is stupid. It's a Github UI limitation - while the jar itself is uploaded unzipped, there is currently no way to download it as a single file.*
