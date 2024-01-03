package com.wxcp.server.price.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @TableName und_vehicle_detail_conf
 */
@TableName(value ="und_vehicle_detail_conf")
@Data
public class UndVehicleDetailConf implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private Long basicId;

    /**
     * 销售代号
     */
    private String salesCode;

    /**
     * 车系
     */
    private String carSeries;

    /**
     * 驱动
     */
    private String driver;

    /**
     * 设计车型号
     */
    private String designCarModel;

    /**
     * 车型
     */
    private String vehicleModel;

    /**
     * 发动机型号
     */
    private String engineType;

    /**
     * 电机功率
     */
    private String motorPower;

    /**
     * 结算价
     */
    private String settlementPrice;

    /**
     * 变速器
     */
    private String variator;

    /**
     * 车桥速比
     */
    private String vehicleBridge;

    /**
     * 车架(mm)
     */
    private String carframe;

    /**
     * 轴距(mm)
     */
    private String wheelBase;

    /**
     * 前悬架
     */
    private String frontSuspension;

    /**
     * 后悬架
     */
    private String rearSuspension;

    /**
     * 轮胎
     */
    private String tyre;

    /**
     * 细分工况
     */
    private String subdivisionCondition;

    /**
     * 动力匹配选装
     */
    private String powerMatchingOption;

    /**
     * 动力电池
     */
    private String powerBattery;

    /**
     * 选装电池系统
     */
    private String optionalBatterySystem;

    /**
     * 免征
     */
    private String exemption;

    /**
     * 营运达标
     */
    private String operatingStandard;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
