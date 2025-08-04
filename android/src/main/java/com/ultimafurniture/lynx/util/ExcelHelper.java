package com.ultimafurniture.lynx.util;

import android.content.Context;

import com.ultimafurniture.lynx.R;
import com.ultimafurniture.lynx.bean.InventoryTagBean;
import com.ultimafurniture.lynx.bean.OperationTagBean;
import com.ultimafurniture.lynx.bean.TemperatureBeanWrapper;
import com.ultimafurniture.lynx.util.XLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * @author naz
 *         Date 2020/9/24
 */
public class ExcelHelper {
    private static WritableFont arial14font = null;

    private static WritableCellFormat arial14format = null;
    private static WritableFont arial10font = null;
    private static WritableCellFormat arial10format = null;
    private static WritableFont arial12font = null;
    private static WritableCellFormat arial12format = null;

    private final static String UTF8_ENCODING = "UTF-8";

    private final static String[] COL_NAME_INVENTORY_TAG = new String[] {
            XLog.sContext.getString(R.string.id),
            XLog.sContext.getString(R.string.epc),
            XLog.sContext.getString(R.string.times),
            XLog.sContext.getString(R.string.rssi),
            XLog.sContext.getString(R.string.carrier_frequency),
            XLog.sContext.getString(R.string.pc)
    };

    private final static String[] COL_NAME_OPERATE_TAG = new String[] {
            XLog.sContext.getString(R.string.id),
            XLog.sContext.getString(R.string.pc),
            XLog.sContext.getString(R.string.crc),
            XLog.sContext.getString(R.string.epc),
            XLog.sContext.getString(R.string.data),
            XLog.sContext.getString(R.string.data_length),
            XLog.sContext.getString(R.string.antenna_number),
            XLog.sContext.getString(R.string.times)
    };

    private final static String[] COL_NAME_JOHAR_TAG = new String[] {
            XLog.sContext.getString(R.string.id),
            XLog.sContext.getString(R.string.pc),
            XLog.sContext.getString(R.string.crc),
            XLog.sContext.getString(R.string.epc),
            XLog.sContext.getString(R.string.temperature),
            XLog.sContext.getString(R.string.antenna_number),
            XLog.sContext.getString(R.string.times)
    };

    private static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);

            arial10font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(jxl.format.Colour.LIGHT_BLUE);

            arial12font = new WritableFont(WritableFont.ARIAL, 12);
            arial12format = new WritableCellFormat(arial12font);
            arial12format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化写Excel工具
     *
     * @param file        文件
     * @param colNameList 列名
     * @return 是否初始化成功
     */
    private static boolean initExcel(File file, ArrayList<String> colNameList) {
        boolean success = true;
        format();
        WritableWorkbook workbook = null;
        try {
            if (!file.exists()) {
                success = file.createNewFile();
            }
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("tags", 0);
            sheet.addCell(new Label(0, 0, file.getAbsolutePath(), arial14format));
            for (int col = 0; col < colNameList.size(); col++) {
                sheet.addCell(new Label(col, 0, colNameList.get(col), arial10format));
            }
            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    public static boolean writeToExcel(File file, List<?> objList, boolean isPhase, boolean enableTemperature) {
        if (objList == null || objList.isEmpty()) {
            return false;
        }
        int tagType;
        ArrayList<String> colNameList = new ArrayList<>();
        if (objList.get(0) instanceof InventoryTagBean) {
            tagType = 0;
            colNameList.addAll(Arrays.asList(COL_NAME_INVENTORY_TAG));
            Context context = XLog.sContext;
            if (isPhase) {
                colNameList.add(context.getString(R.string.phase));
            }
            if (enableTemperature) {
                colNameList.add(context.getString(R.string.temperature));
            }
        } else if (objList.get(0) instanceof OperationTagBean) {
            tagType = 1;
            colNameList.addAll(Arrays.asList(COL_NAME_OPERATE_TAG));
        } else if (objList.get(0) instanceof TemperatureBeanWrapper) {
            tagType = 2;
            colNameList.addAll(Arrays.asList(COL_NAME_JOHAR_TAG));
        } else {
            return false;
        }
        boolean success = initExcel(file, colNameList);
        if (!success) {
            return false;
        }

        WritableWorkbook writeBook = null;
        InputStream in = null;
        try {
            WorkbookSettings setEncode = new WorkbookSettings();
            setEncode.setEncoding(UTF8_ENCODING);
            in = new FileInputStream(file);
            Workbook workbook = Workbook.getWorkbook(in);
            writeBook = Workbook.createWorkbook(file, workbook);
            WritableSheet sheet = writeBook.getSheet(0);
            if (tagType == 0) {
                addInventoryTags(sheet, (List<InventoryTagBean>) objList, isPhase);
            } else if (tagType == 1) {
                addOperateTags(sheet, (List<OperationTagBean>) objList);
            } else {
                addJoharTags(sheet, (List<TemperatureBeanWrapper>) objList);
            }
            writeBook.write();
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (writeBook != null) {
                try {
                    writeBook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    private static void addInventoryTags(WritableSheet sheet, List<InventoryTagBean> tags, boolean isPhase)
            throws WriteException {
        int len = tags.size();
        for (int i = 0; i < len; i++) {
            ArrayList<String> list = new ArrayList<>();
            InventoryTagBean bean = tags.get(i);
            list.add(String.valueOf(i));
            list.add(bean.getEpc());
            list.add(bean.getTimesStr());
            list.add(bean.getRssi());
            list.add(bean.getFreq());
            list.add(bean.getPc());
            if (isPhase) {
                list.add(bean.getPhase());
            }
            for (int j = 0; j < list.size(); j++) {
                sheet.addCell(new Label(j, i + 1, list.get(j), arial12format));
            }
        }
    }

    private static void addOperateTags(WritableSheet sheet, List<OperationTagBean> tags) throws WriteException {
        int len = tags.size();
        for (int i = 0; i < len; i++) {
            ArrayList<String> list = new ArrayList<>();
            OperationTagBean bean = tags.get(i);
            list.add(String.valueOf(len - i));
            list.add(bean.getPc());
            list.add(bean.getCrc());
            list.add(bean.getEpc());
            list.add(bean.getData());
            list.add(String.valueOf(bean.getDataLen()));
            list.add(String.valueOf(bean.getAntId()));
            list.add(bean.getTimes());
            for (int j = 0; j < list.size(); j++) {
                sheet.addCell(new Label(j, i + 1, list.get(j), arial12format));
            }
        }
    }

    private static void addJoharTags(WritableSheet sheet, List<TemperatureBeanWrapper> tags) throws WriteException {
        int len = tags.size();
        for (int i = 0; i < len; i++) {
            ArrayList<String> list = new ArrayList<>();
            TemperatureBeanWrapper bean = tags.get(i);
            list.add(String.valueOf(len - i));
            list.add(bean.getPc());
            list.add(bean.getCrc());
            list.add(bean.getEpc());
            list.add(bean.getTemperature());
            list.add(String.valueOf(bean.getAntId()));
            list.add(bean.getTimes());
            for (int j = 0; j < list.size(); j++) {
                sheet.addCell(new Label(j, i + 1, list.get(j), arial12format));
            }
        }
    }
}
