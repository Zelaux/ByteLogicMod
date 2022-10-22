# Byte-Logic Mod
Byte Logic is a mod that allows you to create logic circuits from Mindustry blocks.  With these blocks, you can manage your factory the way you would with processors.  This mod contains the content of the original version of Mindustry (byte-logic v5), created by Anuken as the first version of current processors.  This content has been redesigned, improved and adapted for Mindustry v7.
### Mod content
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
### Future plans
Here are plans and ideas for future versions of the mod
- **Signal extractor** - a block that extracts a signal from a block standing behind
- **Signal manipulator** - sets a fixed signal length (A signal that will disappear after X ticks on the current block)