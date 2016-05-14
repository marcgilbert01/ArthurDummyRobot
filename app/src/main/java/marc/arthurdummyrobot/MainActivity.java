package marc.arthurdummyrobot;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {


    final double SPEED = 1;  // 1 second to do a meter
    final double SPEED_DEGREES = 0.05;  // 0.10 second to do a degree
    final int SCALE = 5; // 1 meter = 5 dp
    final int PORT = 12345;


    ImageView imageViewRobot;
    TextView textViewIp;
    TextView textViewPort;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        handler = new Handler();
        imageViewRobot = (ImageView) findViewById(R.id.imageView);
        imageViewRobot.setX( 125-10 );
        imageViewRobot.setY( 125-10 );

        textViewIp   = (TextView) findViewById(R.id.textViewIp);
        textViewPort = (TextView) findViewById(R.id.textViewPort);


        final ArthurEnvelopeBuilder arthurEnvelopeBuilder = new ArthurEnvelopeBuilder();
        final ArthurCommandByteBuilder arthurCommandByteBuilder = new ArthurCommandByteBuilder();



        new Thread(){
            @Override
            public void run() {
                super.run();

/*
                move(10, new ActionCallBack() {
                    @Override
                    public void onActionCompleted() {

                        rotate(-135, new ActionCallBack() {
                            @Override
                            public void onActionCompleted() {

                                move(20, new ActionCallBack() {
                                    @Override
                                    public void onActionCompleted() {

                                        imageViewRobot.setBackgroundColor(Color.BLUE);

                                    }
                                });

                            }
                        });

                    }
                });
*/


                WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                int ip = wifiInfo.getIpAddress();
                final String ipAddress = Formatter.formatIpAddress(ip);

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        textViewPort.setText( "Port : "+PORT);
                        textViewIp.setText( "Ip : "+ipAddress);
                    }
                });


                try {

                    ServerSocket serverSocket = new ServerSocket( PORT );
                    Socket socket = serverSocket.accept();
                    InputStream inputStream   = socket.getInputStream();
                    final OutputStream outputStream = socket.getOutputStream();

                    while(true) {

                        byte[] packet = new byte[256];
                        inputStream.read(packet);

                        byte[] command = arthurEnvelopeBuilder.extractPayload( packet );

                        arthurCommandByteBuilder.extractPayload(command);

                        byte instructionType = arthurCommandByteBuilder.getInstructionType();



                        // MOVE
                        if( instructionType==0x01 ){

                            int distance = ByteBuffer.wrap(arthurCommandByteBuilder.payLoad).getInt();
                            move(distance, new ActionCallBack() {
                                @Override
                                public void onActionCompleted() {

                                    ArthurResponseByteBuilder arthurResponseByteBuilder = new ArthurResponseByteBuilder();
                                    arthurResponseByteBuilder.setClassType(arthurCommandByteBuilder.getClassType());
                                    arthurResponseByteBuilder.setInstructionType(arthurCommandByteBuilder.getInstructionType());
                                    byte success = 0x01;
                                    arthurResponseByteBuilder.setSuccess( success );
                                    byte[] response = arthurResponseByteBuilder.buildPacket();


                                    arthurEnvelopeBuilder.setPayload( response );
                                    byte[] envelope = arthurEnvelopeBuilder.buildPacket();


                                    try {
                                        outputStream.write( envelope );
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        }




                        // ROTATE







                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }



    private void move(int distance , final ActionCallBack actionCallBack) {


        double rotation = imageViewRobot.getRotation();

        Point point = getDestination( rotation , distance );

        float currentX = imageViewRobot.getX();
        float currentY = imageViewRobot.getY();

        final ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat( imageViewRobot ,"x", currentX + point.x );
        objectAnimatorX.setDuration((long) (distance*SPEED*1000D));



        final ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat( imageViewRobot ,"y", currentY - point.y );
        objectAnimatorY.setDuration((long) (distance  * SPEED *      1000));
        objectAnimatorX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                actionCallBack.onActionCompleted();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        handler.post(new Runnable() {
            @Override
            public void run() {

                objectAnimatorX.start();
                objectAnimatorY.start();

            }
        });

    }



    private void rotate(double degrees, final ActionCallBack actionCallBack){


        double currentdegrees = imageViewRobot.getRotation();

        double newDegrees = currentdegrees+degrees;

        final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat( imageViewRobot ,"rotation", (float)newDegrees );
        objectAnimator.setDuration( (long) (Math.abs(degrees)*SPEED_DEGREES*1000)  );
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                actionCallBack.onActionCompleted();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        handler.post(new Runnable() {
            @Override
            public void run() {

                objectAnimator.start();
            }
        });



    }



    private Point getDestination( double degrees , double distance ){

        Point point = new Point();

        double mDistance = distance;

        double distanceX = Math.sin( Math.toRadians(degrees));
        double distanceY = Math.cos( Math.toRadians(degrees));

        distanceX = distanceX*mDistance;
        distanceY = distanceY*mDistance;

        point.x = (int) distanceX;
        point.y = (int) distanceY;

        point.y = point.y*SCALE;
        point.x = point.x*SCALE;

        return point;
    }





    interface ActionCallBack{

        public void onActionCompleted();

    }



}
