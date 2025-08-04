package com.ultimafurniture.lynx.bean;

import androidx.annotation.NonNull;

import com.payne.reader.bean.receive.OperationTag;

import java.util.Locale;

/**
 * @author naz
 *         Date 2020/4/3
 */
public class TemperatureBeanWrapper {
    private OperationTag tagBean;
    private int times;
    private String realEpc;
    private double temperature;
    private String temperatureStr;
    private static Locale sDefaultLocal = Locale.getDefault();

    public TemperatureBeanWrapper(@NonNull OperationTag bean, double temperature) {
        this.times = 0;
        setTagBean(bean, temperature);
    }

    public void setTagBean(OperationTag tagBean, double tmp) {
        this.times++;
        this.tagBean = tagBean;

        temperature = tmp;
        temperatureStr = String.format(sDefaultLocal, "%.2f℃", temperature);

        String epc = tagBean.getStrEpc();
        if (5 < epc.length()) {
            realEpc = epc.substring(0, 5);
        } else {
            realEpc = epc;
        }
    }

    public String getEpc() {
        return realEpc;
    }

    public String getPc() {
        return tagBean.getStrPc();
    }

    public String getCrc() {
        return tagBean.getStrCrc();
    }

    public String getTemperature() {
        return temperatureStr;
    }

    public int getAntId() {
        return tagBean.getAntId();
    }

    public String getTimes() {
        return String.valueOf(times);
    }

    public boolean isValid(double tmp) {
        return Math.abs(tmp - temperature) < 10;
    }
}
