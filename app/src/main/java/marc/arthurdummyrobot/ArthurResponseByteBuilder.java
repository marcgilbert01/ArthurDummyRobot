package marc.arthurdummyrobot;

import java.nio.ByteBuffer;

/**
 * Created by marc on 14/05/16.
 */
public class ArthurResponseByteBuilder {


    byte classType;
    byte instructionType;
    byte success;
    byte[] payLoad;


    public byte[] buildPacket() {

        byte[] packet;
        if( payLoad!=null ) {
            packet = new byte[7 + payLoad.length];
        }
        else{
            packet = new byte[7];
        }

        packet[0] = classType;
        packet[1] = instructionType;
        packet[2] = success;

        if( payLoad!=null ) {
            // PAYLOAD LENGTH
            byte[] payloadLengthBytes = ByteBuffer.allocate(4).putInt(payLoad.length).array();
            System.arraycopy(payloadLengthBytes, 0, packet, 3, 4);
            // PAYLOAD
            System.arraycopy(payLoad, 0, packet, 7, payLoad.length);
        }

        return packet;

    }


    public byte[] extractPayload(byte[] packet) {

        classType = packet[0];
        instructionType = packet[1];
        success = packet[2];

        // IF PAYLOAD PRESENT COPY
        if( packet.length>3 ) {
            // PAYLOAD LENGHT
            byte[] payLoadLengthBytes = new byte[4];
            System.arraycopy(packet, 3, payLoadLengthBytes, 0, 4);
            ByteBuffer byteBuffer = ByteBuffer.wrap(payLoadLengthBytes);
            int payLoadLength = byteBuffer.getInt();
            // PAYLOAD
            payLoad = new byte[payLoadLength];
            System.arraycopy(packet, 7, payLoad, 0, payLoadLength);
        }

        return payLoad;
    }


    public byte getClassType() {
        return classType;
    }

    public ArthurResponseByteBuilder setClassType(byte classType) {
        this.classType = classType;
        return this;
    }

    public byte getInstructionType() {
        return instructionType;
    }

    public ArthurResponseByteBuilder setInstructionType(byte instructionType) {
        this.instructionType = instructionType;
        return this;
    }

    public byte getSuccess() {
        return success;
    }

    public ArthurResponseByteBuilder setSuccess(byte success) {
        this.success = success;
        return this;
    }

    public byte[] getPayLoad() {
        return payLoad;
    }

    public ArthurResponseByteBuilder setPayLoad(byte[] payLoad) {
        this.payLoad = payLoad;
        return this;
    }
}
