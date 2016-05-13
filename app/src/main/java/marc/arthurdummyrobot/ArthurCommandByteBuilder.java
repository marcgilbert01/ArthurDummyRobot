package marc.arthurdummyrobot;

import java.nio.ByteBuffer;

/**
 * Created by gilbertm on 13/05/2016.
 */
public class ArthurCommandByteBuilder {

    byte classType;
    byte instructionType;
    byte[] payLoad;

    public byte[] buildPacket() {

        byte[] bytes = new byte[ 6 + payLoad.length ];

        bytes[0] = classType;
        bytes[1] = instructionType;
        // PAYLOAD LENGTH
        byte[] payLoadLength = ByteBuffer.allocate(4).putInt( payLoad.length ).array();
        System.arraycopy( payLoadLength , 0 , bytes , 2 , 4 );
        // PAYLOAD
        System.arraycopy( payLoad , 0 , bytes , 6 , payLoad.length );

        return bytes;
    }

    public byte[] extractPayload(byte[] packet) {

        classType = packet[0];
        instructionType = packet[1];
        //PAYLOAD LENGTH
        byte[] payLoadLength = new byte[4];
        System.arraycopy( packet , 2 , payLoadLength , 0 , 4 );
        int payloadLength = ByteBuffer.wrap(payLoadLength).getInt();
        // PAYLOAD
        System.arraycopy(packet,6,payLoad,0,payloadLength);

        return payLoad;

    }

    public byte getClassType() {
        return classType;
    }

    public ArthurCommandByteBuilder setClassType(byte classType) {
        this.classType = classType;
        return this;
    }

    public byte getInstructionType() {
        return instructionType;
    }

    public ArthurCommandByteBuilder setInstructionType(byte instructionType) {
        this.instructionType = instructionType;
        return this;
    }

    public byte[] getPayLoad() {
        return payLoad;
    }

    public ArthurCommandByteBuilder setPayLoad(byte[] payLoad) {
        this.payLoad = payLoad;
        return this;
    }

}
