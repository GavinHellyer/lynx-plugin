package com.ultimafurniture.lynx.util;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

public class ShellUtils {
    public static void execCmd(String cmd) {
        // XLog.i(cmd);
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            Process p = Runtime.getRuntime().exec("sh");
            outputStream = p.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != dataOutputStream) {
                try {
                    dataOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String execShellCmd(String command) {
        // XLog.i(command);
        String result = "";
        try {
            Process process = Runtime.getRuntime().exec(command + "\n");
            DataOutputStream stdin = new DataOutputStream(process.getOutputStream());
            DataInputStream stdout = new DataInputStream(process.getInputStream());
            DataInputStream stderr = new DataInputStream(process.getErrorStream());
            // stdin.writeBytes();
            String line;
            while ((line = stdout.readLine()) != null) {
                result += line + "\n";
            }
            if (result.length() > 0) {
                result = result.substring(0, result.length() - 1);
            }
            while ((line = stderr.readLine()) != null) {
                Log.e("EXEC", line);
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void lte(boolean enable) {
        if (enable) {
            execCmd("echo -e \'at+gtact=2\r\n\' > /dev/ttyUSB3");
        } else {
            execCmd("echo -e \'at+gtact=2,,,101\r\n\' > /dev/ttyUSB3");
        }
    }
}
