package com.wxcp.server.price.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @TableName und_vehicle_basic_conf
 */
@TableName(value ="und_vehicle_basic_conf")
@Data
public class UndVehicleBasicConf implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String sheetName;

    /**
     * 应用场景
     */
    private String scenarios;

    /**
     * 燃料类型
     */
    private String fuelType;

    /**
     * 车型特征
     */
    private String vehicleCharacter;

    /**
     * 车距
     */
    private String haulDistance;

    /**
     * 速度
     */
    private String speed;

    /**
     * 细分市场
     */
    private String marketSegment;

    /**
     * 版型
     */
    private String stereotype;

    /**
     * 版本
     */
    private String version;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}
