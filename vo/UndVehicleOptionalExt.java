package com.wxcp.server.price.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @TableName und_vehicle_optional_ext
 */
@TableName(value ="und_vehicle_optional_ext")
@Data
public class UndVehicleOptionalExt implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private Long optionalId;

    /**
     *
     */
    private String extKey;

    /**
     *
     */
    private String extValue;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
