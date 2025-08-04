package com.ultimafurniture.lynx.util;

import com.ultimafurniture.lynx.GlobalCfg;
import com.ultimafurniture.lynx.R;
import com.ultimafurniture.lynx.bean.type.LinkType;
import com.payne.reader.bean.config.Cmd;
import com.payne.reader.bean.config.ResultCode;

/**
 * @author naz
 *         Date 2020/6/29
 */
public class StringFormat {

    /**
     * 根据cmd和resultCode解析成字符串
     *
     * @param cmd        Cmd
     * @param resultCode 结果码
     * @return String
     */
    public static String format(byte cmd, byte resultCode) {
        return formatCmd(cmd)
                + formatResultCode(resultCode);
    }

    /**
     * 根据cmd和resultCode将温度标签2结果码解析成字符串
     *
     * @param cmd        Cmd
     * @param resultCode 结果码
     * @return String
     */
    public static String formatTempLabel2(byte cmd, byte resultCode) {
        return formatCmd(cmd) +
                formatTempLabel2ResultCode(resultCode);
    }

    /**
     * 解析cmd
     *
     * @param cmd Command
     * @return String
     */
    private static String formatCmd(byte cmd) {
        switch (cmd) {
            case Cmd.RESET:
                return XLog.sContext.getString(R.string.reset_reader);
            case Cmd.SET_SERIAL_PORT_BAUD_RATE:
                return XLog.sContext.getString(R.string.set_serial_baud_rate);
            case Cmd.GET_FIRMWARE_VERSION:
                return XLog.sContext.getString(R.string.get_reader_firmware_version);
            case Cmd.SET_READER_ADDRESS:
                return XLog.sContext.getString(R.string.set_reader_address);
            case Cmd.SET_WORK_ANTENNA:
                return XLog.sContext.getString(R.string.set_reader_antenna);
            case Cmd.GET_WORK_ANTENNA:
                return XLog.sContext.getString(R.string.get_reader_antenna);
            case Cmd.SET_OUTPUT_POWER:
                return XLog.sContext.getString(R.string.set_reader_output_power);
            case Cmd.GET_OUTPUT_POWER:
                return XLog.sContext.getString(R.string.get_reader_output_power);
            case Cmd.SET_FREQUENCY_REGION:
                return XLog.sContext.getString(R.string.set_reader_frequency_range);
            case Cmd.GET_FREQUENCY_REGION:
                return XLog.sContext.getString(R.string.get_reader_frequency_range);
            case Cmd.SET_BEEPER_MODE:
                return XLog.sContext.getString(R.string.set_beeper_mode);
            case Cmd.GET_READER_TEMPERATURE:
                return XLog.sContext.getString(R.string.get_reader_temperature);
            case Cmd.READ_GPIO_VALUE:
                return XLog.sContext.getString(R.string.get_gpio_value);
            case Cmd.WRITE_GPIO_VALUE:
                return XLog.sContext.getString(R.string.set_gpio_value);
            case Cmd.SET_ANT_CONNECTION_DETECTOR:
                return XLog.sContext.getString(R.string.set_antenna_connection_detector_status);
            case Cmd.GET_ANT_CONNECTION_DETECTOR:
                return XLog.sContext.getString(R.string.get_antenna_connection_detector_status);
            case Cmd.SET_TEMPORARY_OUTPUT_POWER:
                return XLog.sContext.getString(R.string.set_reader_temporary_output_power);
            case Cmd.SET_READER_IDENTIFIER:
                return XLog.sContext.getString(R.string.set_reader_id);
            case Cmd.GET_READER_IDENTIFIER:
                return XLog.sContext.getString(R.string.get_reader_id);
            case Cmd.SET_RF_LINK_PROFILE:
                return XLog.sContext.getString(R.string.set_rf_link);
            case Cmd.GET_RF_LINK_PROFILE:
                return XLog.sContext.getString(R.string.get_rf_link);
            case Cmd.GET_RF_PORT_RETURN_LOSS:
                return XLog.sContext.getString(R.string.get_ant_return_loss);
            case Cmd.INVENTORY:
                return XLog.sContext.getString(R.string.Inventory_label);
            case Cmd.READ_TAG:
                return XLog.sContext.getString(R.string.read_label);
            case Cmd.WRITE_TAG:
                return XLog.sContext.getString(R.string.write_label);
            case Cmd.LOCK_TAG:
                return XLog.sContext.getString(R.string.lock_label);
            case Cmd.KILL_TAG:
                return XLog.sContext.getString(R.string.kill_label);
            case Cmd.FAST_SWITCH_ANT_INVENTORY:
                return XLog.sContext.getString(R.string.fast_switch_ant_inventory);
            case Cmd.CUSTOMIZED_SESSION_TARGET_INVENTORY:
                return XLog.sContext.getString(R.string.customized_session_target_inventory);
            case Cmd.SET_IMPINJ_FAST_TID:
                return XLog.sContext.getString(R.string.set_impinj_fast_tid);
            case Cmd.SET_AND_SAVE_IMPINJ_FAST_TID:
                return XLog.sContext.getString(R.string.set_and_save_impinj_fast_tid);
            case Cmd.GET_IMPINJ_FAST_TID:
                return XLog.sContext.getString(R.string.get_impinj_fast_tid);
            case Cmd.QUERY_READER_STATUS:
                return XLog.sContext.getString(R.string.query_reader_status);
            case Cmd.SET_READER_STATUS:
                return XLog.sContext.getString(R.string.set_reader_status);
            case Cmd.BLOCK_WRITE_TAG:
                return XLog.sContext.getString(R.string.block_write_label);
            case Cmd.SET_ACCESS_EPC_MATCH:
                return XLog.sContext.getString(R.string.set_match_tag);
            case Cmd.GET_ACCESS_EPC_MATCH:
                return XLog.sContext.getString(R.string.get_match_tag);
            case Cmd.OPERATE_TAG_MASK:
                return XLog.sContext.getString(R.string.operate_tag_mask);
            case Cmd.TEMPERATURE_LABEL_COMMAND:
                return XLog.sContext.getString(R.string.operate_label_cmd);
            case ReaderHelper.GET_FIRMWARE_PATCH_VERSION:
                return XLog.sContext.getString(R.string.get_reader_firmware_subversion);
            case (byte) 0xF0:
                if (GlobalCfg.get().getLinkType() == LinkType.LINK_TYPE_BLUETOOTH) {
                    return XLog.sContext.getString(R.string.get_bluetooth_version);
                }
                return XLog.sContext.getString(R.string.set_rf_link);
            case (byte) 0xF1:
                if (GlobalCfg.get().getLinkType() == LinkType.LINK_TYPE_BLUETOOTH) {
                    return XLog.sContext.getString(R.string.get_bluetooth_mac_address);
                }
                return XLog.sContext.getString(R.string.get_rf_link);
            case (byte) 0xF2: {
                return "Q";
            }
            case (byte) 0xF3:
                if (GlobalCfg.get().getLinkType() == LinkType.LINK_TYPE_BLUETOOTH) {
                    return XLog.sContext.getString(R.string.get_interface_board_sn_number);
                }
                return "Q";
            case (byte) 0xF4:
                return XLog.sContext.getString(R.string.set_interface_board_sn_number);
            case ReaderHelper.GET_INTERFACE_BOARD_VERSION_NUMBER:
                return XLog.sContext.getString(R.string.get_interface_board_version_number);
            case ReaderHelper.GET_BATTERY_VOLTAGE:
                return XLog.sContext.getString(R.string.get_battery_voltage);
            case ReaderHelper.SETTING_BUZZER:
                return XLog.sContext.getString(R.string.set_device_buzzer);
            case ReaderHelper.OPEN_CLOSE_MODULE:
                return XLog.sContext.getString(R.string.turn_module_on_or_off);
            case ReaderHelper.BARCODE_RECEIVE:
                return XLog.sContext.getString(R.string.read_barcode);
            default:
                return XLog.sContext.getString(R.string.unknown_operation) + "(" + Cmd.getNameForCmd(cmd) + ")";
        }
    }

    /**
     * 解析ResultCode
     *
     * @param resultCode 结果码
     * @return String
     */
    private static String formatResultCode(byte resultCode) {
        switch (resultCode) {
            case ResultCode.SUCCESS:
                return XLog.sContext.getString(R.string.success);
            case ResultCode.FAIL:
                return XLog.sContext.getString(R.string.failed);
            case ResultCode.MCU_RESET_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.mcu_reset_error);
            case ResultCode.CW_ON_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.cw_on_error);
            case ResultCode.ANTENNA_MISSING_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.antenna_missing_error);
            case ResultCode.WRITE_FLASH_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.write_flash_error);
            case ResultCode.READ_FLASH_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.read_flash_error);
            case ResultCode.SET_OUTPUT_POWER_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.set_output_power_error);
            case ResultCode.TAG_INVENTORY_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.tag_inventory_error);
            case ResultCode.TAG_READ_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.tag_read_error);
            case ResultCode.TAG_WRITE_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.tag_write_error);
            case ResultCode.TAG_LOCK_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.tag_lock_error);
            case ResultCode.TAG_KILL_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.tag_kill_error);
            case ResultCode.NO_TAG_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.no_tag_error);
            case ResultCode.INVENTORY_OK_BUT_ACCESS_FAIL:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.inventory_ok_but_access_fail);
            case ResultCode.BUFFER_IS_EMPTY_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.buffer_is_empty_error);
            case ResultCode.NXP_CUSTOM_COMMAND_FAIL:
                return XLog.sContext.getString(R.string.fail_with_comma) + XLog.sContext.getString(R.string.nxp_fail);
            case ResultCode.ACCESS_OR_PASSWORD_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.access_or_password_error);
            case ResultCode.PARAMETER_INVALID:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid);
            case ResultCode.PARAMETER_INVALID_WORDCNT_TOO_LONG:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid_wordcnt_too_long);
            case ResultCode.PARAMETER_INVALID_MEMBANK_OUT_OF_RANGE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid_membank_out_of_range);
            case ResultCode.PARAMETER_INVALID_LOCK_REGION_OUT_OF_RANGE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid_lock_region_out_of_range);
            case ResultCode.PARAMETER_INVALID_LOCK_ACTION_OUT_OF_RANGE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid_lock_action_out_of_range);
            case ResultCode.PARAMETER_READER_ADDRESS_INVALID:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_reader_address_invalid);
            case ResultCode.PARAMETER_INVALID_ANTENNA_ID_OUT_OF_RANGE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid_antenna_id_out_of_range);
            case ResultCode.PARAMETER_INVALID_OUTPUT_POWER_OUT_OF_RANGE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid_output_power_out_of_range);
            case ResultCode.PARAMETER_INVALID_FREQUENCY_REGION_OUT_OF_RANGE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid_frequency_region_out_of_range);
            case ResultCode.PARAMETER_INVALID_BAUDRATE_OUT_OF_RANGE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid_baudrate_out_of_range);
            case ResultCode.PARAMETER_BEEPER_MODE_OUT_OF_RANGE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_beeper_mode_out_of_range);
            case ResultCode.PARAMETER_EPC_MATCH_LEN_TOO_LONG:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_epc_match_len_too_long);
            case ResultCode.PARAMETER_EPC_MATCH_LEN_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_epc_match_len_error);
            case ResultCode.PARAMETER_INVALID_EPC_MATCH_MODE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid_epc_match_mode);
            case ResultCode.PARAMETER_INVALID_FREQUENCY_RANGE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid_frequency_range);
            case ResultCode.FAIL_TO_GET_RN16_FROM_TAG:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.fail_to_get_rn16_from_tag);
            case ResultCode.PARAMETER_INVALID_DRM_MODE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.parameter_invalid_drm_mode);
            case ResultCode.PLL_LOCK_FAIL:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.pll_lock_fail);
            case ResultCode.RF_CHIP_FAIL_TO_RESPONSE:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.rf_chip_fail_to_response);
            case ResultCode.FAIL_TO_ACHIEVE_DESIRED_OUTPUT_POWER:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.fail_to_achieve_desired_output_power);
            case ResultCode.COPYRIGHT_AUTHENTICATION_FAIL:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.copyright_authentication_fail);
            case ResultCode.SPECTRUM_REGULATION_ERROR:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.spectrum_regulation_error);
            case ResultCode.OUTPUT_POWER_TOO_LOW:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.output_power_too_low);
            case ResultCode.FAIL_TO_GET_RF_PORT_RETURN_LOSS:
                return XLog.sContext.getString(R.string.fail_with_comma)
                        + XLog.sContext.getString(R.string.measure_return_loss_fail);
            default:
                return XLog.sContext.getString(R.string.error);
        }
    }

    /**
     * 解析温度标签2的ResultCode
     *
     * @param resultCode 结果码
     * @return String
     */
    public static String formatTempLabel2ResultCode(byte resultCode) {
        if (resultCode == (byte) 0x3c) {
            return XLog.sContext.getString(R.string.fail_with_comma)
                    + XLog.sContext.getString(R.string.temp_label2_3c_error);
        } else if (resultCode == (byte) 0x3d) {
            return XLog.sContext.getString(R.string.fail_with_comma)
                    + XLog.sContext.getString(R.string.temp_label2_3d_error);
        } else {
            return formatResultCode(resultCode);
        }
    }
}
