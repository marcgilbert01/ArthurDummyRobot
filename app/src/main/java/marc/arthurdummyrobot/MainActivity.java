package marc.arthurdummyrobot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {


    ImageView imageViewRobot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageViewRobot = (ImageView) findViewById(R.id.imageView);

        final ArthurEnvelopeBuilder arthurEnvelopeBuilder = new ArthurEnvelopeBuilder();
        final ArthurCommandByteBuilder arthurCommandByteBuilder = new ArthurCommandByteBuilder();



        new Thread(){
            @Override
            public void run() {
                super.run();


                try {
                    ServerSocket serverSocket = new ServerSocket( 12345 );
                    Socket socket = serverSocket.accept();
                    InputStream inputStream = socket.getInputStream();

                    while(true) {

                        byte[] packet = new byte[256];
                        inputStream.read(packet);

                        byte[] command = arthurEnvelopeBuilder.extractPayload( packet );

                        arthurCommandByteBuilder.extractPayload(command);

                        byte instructionType = arthurCommandByteBuilder.getInstructionType();

                        if( instructionType==0x01 ){

                            int distance = ByteBuffer.wrap(arthurCommandByteBuilder.payLoad).getInt();
                            move(distance);
                        }











                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }












            }
        }.start();




    }














}
