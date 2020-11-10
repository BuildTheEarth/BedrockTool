package cubicchunks.converter.lib.util;

import com.google.common.base.Preconditions;

public class NibbleArray {
    private final byte[] data;

    public NibbleArray(int length) {
        this.data = new byte[length / 2];
    }

    public NibbleArray(byte[] array) {
        this.data = array;
    }

    public byte get(int index) {
        Preconditions.checkElementIndex(index, this.data.length << 1);
        byte val = this.data[index >> 1];
        if ((index & 1) == 0) {
            return (byte) (val & 0x0F);
        } else {
            return (byte) ((val & 0xF0) >>> 4);
        }
    }

    public void set(int index, int value) {
        Preconditions.checkArgument(value >= 0 && value < 16, "Nibbles must have a value between 0 and 15.");
        Preconditions.checkElementIndex(index, this.data.length << 1);
        int half = index >> 1;
        byte previous = this.data[half];
        if ((index & 1) == 0) {
            this.data[half] = (byte) (previous & 0xF0 | value);
        } else {
            this.data[half] = (byte) (previous & 0x0F | value << 4);
        }
    }
}
