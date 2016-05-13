package marc.arthurdummyrobot;

import java.nio.ByteBuffer;

/**
 * Created by gilbertm on 13/05/2016.
 */
public class ArthurEnvelopeBuilder {

    static final int SEQUENCE_NUMBER_FIELD_SIZE   = 4;
    static final int LENGTH_OF_PAYLOAD_FIELD_SIZE = 4;
    int sequenceNumber = -1;
    byte[] payload;


    public byte[] buildPacket() {

        byte[] envelopeBytes = new byte[ SEQUENCE_NUMBER_FIELD_SIZE + LENGTH_OF_PAYLOAD_FIELD_SIZE + payload.length ];
        // ENVELOPE SEQUENCE NUMBER
        byte[] sequenceNumberBytes = ByteBuffer.allocate(SEQUENCE_NUMBER_FIELD_SIZE).putInt( sequenceNumber ).array();
        System.arraycopy( sequenceNumberBytes , 0 , envelopeBytes , 0, SEQUENCE_NUMBER_FIELD_SIZE );
        // LENGTH OF PAYLOAD
        byte[] lengthOfPayload = ByteBuffer.allocate(LENGTH_OF_PAYLOAD_FIELD_SIZE).putInt( payload.length ).array();
        System.arraycopy( lengthOfPayload , 0 , envelopeBytes , SEQUENCE_NUMBER_FIELD_SIZE , LENGTH_OF_PAYLOAD_FIELD_SIZE);
        // PAYLOAD (COMMAND)
        System.arraycopy(payload,0,envelopeBytes, SEQUENCE_NUMBER_FIELD_SIZE+ LENGTH_OF_PAYLOAD_FIELD_SIZE, payload.length );

        return envelopeBytes;
    }


    public byte[] extractPayload(byte[] packet) {

        // ENVELOPE SEQUENCE NUMBER
        byte[] sequenceNumberBytes = new byte[SEQUENCE_NUMBER_FIELD_SIZE];
        System.arraycopy(packet,0,sequenceNumberBytes,0,SEQUENCE_NUMBER_FIELD_SIZE);
        sequenceNumber = ByteBuffer.wrap(sequenceNumberBytes).getInt();
        // LENGTH OF PAYLOAD
        byte[] lengthOfPayload = new byte[LENGTH_OF_PAYLOAD_FIELD_SIZE];
        System.arraycopy(packet,SEQUENCE_NUMBER_FIELD_SIZE,lengthOfPayload,0, LENGTH_OF_PAYLOAD_FIELD_SIZE);
        int payloadLength = ByteBuffer.wrap(lengthOfPayload).getInt();
        // PAYLOAD
        payload = new byte[payloadLength];
        System.arraycopy(packet,SEQUENCE_NUMBER_FIELD_SIZE+LENGTH_OF_PAYLOAD_FIELD_SIZE,payload,0,payloadLength);

        return payload;
    }


    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public ArthurEnvelopeBuilder setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    public byte[] getPayload() {
        return payload;
    }

    public ArthurEnvelopeBuilder setPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }

}
