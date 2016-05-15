package marc.arthurdummyrobot;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {


    final double SPEED = 0.5;  // 1 second to do a meter
    final double SPEED_DEGREES = 0.05;  // 0.10 second to do a degree
    final int SCALE = 5; // 1 meter = 5 dp
    final int PORT = 12345;
    Boolean exit = false;

    ImageView imageViewRobot;
    TextView textViewIp;
    TextView textViewPort;
    Button buttonClose;
    ImageView imageViewPhoto;
    Camera camera;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        handler = new Handler();
        imageViewRobot = (ImageView) findViewById(R.id.imageView);
        imageViewRobot.setX( 125-10 );
        imageViewRobot.setY( 125-10 );
        imageViewRobot.setRotation(90);

        textViewIp   = (TextView) findViewById(R.id.textViewIp);
        textViewPort = (TextView) findViewById(R.id.textViewPort);
        buttonClose  = (Button) findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit = true;
                finish();
            }
        });

        imageViewPhoto = (ImageView) findViewById(R.id.imageViewPhoto);

        final ArthurEnvelopeBuilder arthurEnvelopeBuilder = new ArthurEnvelopeBuilder();
        final ArthurCommandByteBuilder arthurCommandByteBuilder = new ArthurCommandByteBuilder();




        // do we have a camera?
        int cameraId = 0;
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
            } else {
                camera = Camera.open(cameraId);
            }
        }
        takePhoto(null);


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


                ServerSocket serverSocket = null;
                try {

                    serverSocket = new ServerSocket( PORT );


                    while( exit==false ) {

                        final Socket socket = serverSocket.accept();
                        InputStream inputStream   = socket.getInputStream();
                        final OutputStream outputStream = socket.getOutputStream();

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
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onPhotoTaken(byte[] photo) {

                                }


                            });

                        }

                        // ROTATE
                        if( instructionType==0x02 ){

                            int degrees = ByteBuffer.wrap(arthurCommandByteBuilder.payLoad).getInt();
                            rotate( degrees, new ActionCallBack() {
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
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onPhotoTaken(byte[] photo) {

                                }


                            });

                        }



                        // TAKE A PHOTO
                        /*
                        if( instructionType==0x04 ){

                            takePhoto( new ActionCallBack() {
                                @Override
                                public void onActionCompleted() {

                                    ArthurResponseByteBuilder arthurResponseByteBuilder = new ArthurResponseByteBuilder();
                                    arthurResponseByteBuilder.setClassType(arthurCommandByteBuilder.getClassType());
                                    arthurResponseByteBuilder.setInstructionType(arthurCommandByteBuilder.getInstructionType());
                                    byte success = 0x01;
                                    arthurResponseByteBuilder.setSuccess( success );
                                    arthurCommandByteBuilder.setPayLoad(  )


                                    byte[] response = arthurResponseByteBuilder.buildPacket();

                                    arthurEnvelopeBuilder.setPayload( response );
                                    byte[] envelope = arthurEnvelopeBuilder.buildPacket();

                                    try {
                                        outputStream.write( envelope );
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onPhotoTaken(Bitmap bitmap) {

                                }
                            });

                        }

*/











                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();

    }



    private void move(int distance , final ActionCallBack actionCallBack) {


        double distanceInMeters = (double)distance/100D;

        double rotation = imageViewRobot.getRotation();

        Point point = getDestination( rotation , distanceInMeters );

        float currentX = imageViewRobot.getX();
        float currentY = imageViewRobot.getY();

        final ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat( imageViewRobot ,"x", currentX + point.x );
        objectAnimatorX.setDuration((long) (distanceInMeters*SPEED*1000D));



        final ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat( imageViewRobot ,"y", currentY - point.y );
        objectAnimatorY.setDuration((long) (distanceInMeters*SPEED*1000D));
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



    private Point getDestination( double currentOrientation , double distance ){

        Point point = new Point();

        double mDistance = distance;

        double distanceX = Math.sin( Math.toRadians(currentOrientation));
        double distanceY = Math.cos( Math.toRadians(currentOrientation));

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

        public void onPhotoTaken(byte[] photo);

    }





    private void takePhoto(final ActionCallBack actionCallBack){




        camera.takePicture(

                new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {
                        actionCallBack.onPhotoTaken(null);
                    }
                }

                , new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {

                        actionCallBack.onPhotoTaken(data);
                    }
                },
                new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        actionCallBack.onPhotoTaken(data);
                    }
                }
        );






    }



    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK ) {
                Log.d("", "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }


}
