package com.wxcp.server.price.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @TableName und_vehicle_special_conf
 */
@TableName(value ="und_vehicle_detail_ext")
@Data
public class UndVehicleDetailExt implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private Long detailId;

    /**
     * 公告型号
     */
    private String announcementType;

    /**
     * 上装分类
     */
    private String topSort;

    /**
     * 货箱尺寸
     */
    private String containerSize;

    /**
     * 公告
     */
    private String announcement;

    /**
     * 3C
     */
    private String threeC;

    /**
     * 环保
     */
    private String environmentalProtection;

    /**
     * 车型编码
     */
    private String modelCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
