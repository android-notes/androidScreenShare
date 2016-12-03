package sharescreen.wanjian.com.server;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


/**
 * Created by wanjian on 2016/11/20.
 */

public class ClientHandler extends Handler {
    private BufferedOutputStream outputStream;
    private final int MSG = 1;


    private void writeInt(OutputStream outputStream, int v) throws IOException {
        outputStream.write(v >> 24);
        outputStream.write(v >> 16);
        outputStream.write(v >> 8);
        outputStream.write(v);
    }

    public void sendData(byte[] datas) {
        removeMessages(MSG);
        Message message = obtainMessage();
        message.what = MSG;
        message.obj = datas;
        sendMessage(message);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (outputStream != null) {
            try {
                byte[] data = (byte[]) msg.obj;
                Log.d("RecorderManager", "length : " + data.length);
                long s = System.currentTimeMillis();
                outputStream.write(RecorderManager.VERSION);
                writeInt(outputStream, data.length);
                outputStream.write(data);
                outputStream.flush();
                Log.d("RecorderManager", "write : " + (System.currentTimeMillis() - s));
            } catch (IOException e) {
                try {
                    outputStream.close();
                } catch (IOException e1) {
                }
                outputStream = null;
            }
        }
    }

    public ClientHandler(Socket socket) {
        try {
            outputStream = new BufferedOutputStream(socket.getOutputStream(), 1024 * 200);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void close() {
        post(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream.close();
                } catch (Exception e) {
                }
                getLooper().quit();
            }
        });

    }
}
