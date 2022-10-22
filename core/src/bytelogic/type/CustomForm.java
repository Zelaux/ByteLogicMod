package bytelogic.type;

import arc.func.*;

public class CustomForm{
    int[] blocks;
    public final int width;
    public final int height;
    int centerX, centerY;
public final int nonNothingAmount;
public final int otherBlocksAmount;
    public CustomForm(int width, int height, int[] blocks){
        this.blocks = blocks;
        this.width = width;
        this.height = height;
        setCenter();
        int nonNothingAmount=0;
        int otherBlocksAmount=0;
        for(int block : blocks){
            if(block == 1){
                nonNothingAmount++;
                otherBlocksAmount++;
            }
            if(block == -1){
                nonNothingAmount++;
            }
        }
        this.nonNothingAmount=nonNothingAmount;
        this.otherBlocksAmount=otherBlocksAmount;
    }

    public static int[] mapForm(char voidChar, char blockChar, char selfChar, String... lines){
        int[] ints = new int[lines.length * lines[0].length()];
        int i = 0;
        for(String line : lines){
            for(char c : line.toCharArray()){
                if(c == voidChar){
                    ints[i] = 0;
                }else if(c == blockChar){
                    ints[i] = 1;
                }else if(c == selfChar){
                    ints[i] = -1;
                }else{
                    throw new IllegalArgumentException("Illegal character \"" + c + "\"");
                }
                i++;
            }
        }
        return ints;
    }

    private void setCenter(){
        for(int i = 0; i < blocks.length; i++){
            if(blocks[i] == -1){
                centerX = unpackX(i);
                centerY = unpackY(i);
                return;
            }
        }
        throw new RuntimeException("Cannot find center");
    }

    public int unpackX(int index){
        return index % width;
    }

    public int unpackY(int index){
        return index / width;
    }

    public int get(int x, int y){
        return blocks[x + y * width];
    }

    public void eachRelativeCenter(Intc2 consumer){
        for(int i = 0; i < blocks.length; i++){
            consumer.get(unpackX(i) - centerX, unpackY(i) - centerY);
        }
    }

    public void eachRelativeCenter(boolean includeNothing, boolean includeOther, boolean includeCenter, Intc2 consumer){
        for(int i = 0; i < blocks.length; i++){
            if(blocks[i] == 0 && includeNothing || blocks[i] == 1 && includeOther || blocks[i] == -1 && includeCenter){
                consumer.get(unpackX(i) - centerX, unpackY(i) - centerY);
            }
        }
    }

    public int getCenter(int x, int y){
        return get(x + centerX, y + centerY);
    }
}
