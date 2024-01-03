package com.wxcp.server.price.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxcp.server.price.vo.UndVehicleDetailExt;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

/**
* @author xuyuxiang
* @description 针对表【und_vehicle_special_conf】的数据库操作Mapper
* @createDate 2023-08-07 11:52:22
* @Entity com.ict.business.domain.UndVehicleSpecialConf
*/
@Component
public interface UndVehicleDetailExtMapper extends BaseMapper<UndVehicleDetailExt> {

    @Update("truncate table und_vehicle_detail_ext")
    public void truncate();

}




