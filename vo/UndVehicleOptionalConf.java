package com.wxcp.server.price.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @TableName und_vehicle_optional_conf
 */
@TableName(value ="und_vehicle_optional_conf")
@Data
public class UndVehicleOptionalConf implements Serializable {
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
     * 选装类型：
1-选装版型
2-选装配置
     */
    private Integer optionalType;

    /**
     * 选装名称
     */
    private String optionalName;

    /**
     * 内容
     */
    private String content;

    /**
     * 价格
     */
    private String price;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
