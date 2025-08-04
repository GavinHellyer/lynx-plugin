package com.ultimafurniture.lynx.util;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.naz.serial.port.ModuleManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 描述：上下电操作
 * <p>
 * <p>
 * Create on 2023-03-16
 */
public class PowerUtils {
    private static final String DEVICE = Build.MANUFACTURER + Build.MODEL;

    public static boolean powerOn() {
        // LCPowerUtils.power("1");
        // PL3PowerUtils.powerOn();
        // return true;
        return RPowerOn();
    }

    public static boolean powerOff() {
        // LCPowerUtils.power("0");
        // PL3PowerUtils.powerOff();
        // return true;

        ModuleManager moduleManager = ModuleManager.newInstance();
        boolean status = moduleManager.setUHFStatus(false);
        moduleManager.release();
        return status;
    }

    private static boolean RPowerOn() {
        ModuleManager moduleManager = ModuleManager.newInstance();
        moduleManager.setUHFStatus(true);
        SystemClock.sleep(200);
        moduleManager.setUHFStatus(false);
        SystemClock.sleep(200);
        return moduleManager.setUHFStatus(true);
    }

    private static class LCPowerUtils {
        private static String s1 = "/proc/gpiocontrol/set_id";
        private static String s2 = "/proc/gpiocontrol/set_uhf";

        private static void power(String state) {
            try {
                FileWriter localFileWriterOn = new FileWriter(s2);
                localFileWriterOn.write(state);
                localFileWriterOn.close();
                Log.e("PowerUtil", "power=" + state + " Path=" + s2);
                FileWriter RaidPower = new FileWriter(s1);
                RaidPower.write(state);
                RaidPower.close();
                Log.e("PowerUtil", "power=" + state + " Path=" + s1);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class PL3PowerUtils {
        // 攀凌3上电
        // private static final String vol33 = "/sys/class/rt5_gpio/gpio_uhf_ext_v33";
        // private static final String vol5 = "/sys/class/rt5_gpio/gpio_uhf_ext_v5";
        // private static final String power = "/sys/class/rt5_gpio/gpio_uhf_power";
        // public static void powerOn() {
        // Log.d("TAG", "pl_power_on");
        // try {
        // writeUhfFile(power, "1".getBytes());
        // Thread.sleep(20);
        // writeUhfFile(vol33, "1".getBytes());
        // Thread.sleep(20);
        // writeUhfFile(vol5, "1".getBytes());
        // Thread.sleep(10);
        // } catch (Exception ex) {
        // }
        // }
        // public static void powerOff() {
        // Log.d("TAG", "pl_power_off");
        // try {
        // writeUhfFile(vol5, "0".getBytes());
        // Thread.sleep(10);
        // writeUhfFile(vol33, "0".getBytes());
        // Thread.sleep(10);
        // writeUhfFile(power, "1".getBytes());
        // Thread.sleep(10);
        // } catch (Exception ex) {
        // }
        // }

        // Android 13
        private static final String vol33 = "/sys/devices/platform/module_power/rfid_en_3v3";
        private static final String vol5 = "/sys/devices/platform/module_power/rfid_print_5v";
        private static final String power = "/sys/devices/platform/module_power/vdd_io_host_en";
        // private static final String rst =
        // "/sys/devices/platform/module_power/rfid_rst_3v3";
        private static final String uart_switch = "/sys/devices/platform/module_power/uart_switch_1v8";

        private static void powerOn() {
            try {
                writeUhfFile(uart_switch, "0".getBytes());
                Thread.sleep(20);
                writeUhfFile(power, "1".getBytes());
                Thread.sleep(20);
                writeUhfFile(vol33, "1".getBytes());
                Thread.sleep(20);
                writeUhfFile(vol5, "1".getBytes());
                Thread.sleep(10);
            } catch (Exception ex) {
            }
        }

        private static void powerOff() {
            try {
                writeUhfFile(vol5, "0".getBytes());
                Thread.sleep(10);
                writeUhfFile(vol33, "0".getBytes());
                Thread.sleep(10);
                writeUhfFile(power, "1".getBytes());
                Thread.sleep(10);
            } catch (Exception ex) {
            }
        }

        private static void writeUhfFile(String path, byte[] value) {
            File f = new File(path);
            try (FileOutputStream fos = new FileOutputStream(f);) {
                fos.write(value);
                fos.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("TAG", "-----writeUhfFile-----e1=" + e.getMessage());
            } catch (IOException e) {
                Log.d("TAG", "-----writeUhfFile-----e2=" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static boolean getPermission(File device) {
        XLog.i("device = " + device);
        if (!device.canRead() || !device.canWrite()) {
            try {
                String cmdSuPath = "/system/bin/su";
                // String cmdSuPath = "/system/xbin/su";
                Process su = Runtime.getRuntime().exec(cmdSuPath);
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\nexit\n";
                su.getOutputStream().write(cmd.getBytes());
                if (su.waitFor() != 0 || !device.canRead() || !device.canWrite()) {
                    System.out.println("SecurityException!");
                    return false;
                }
            } catch (Exception var8) {
                var8.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
