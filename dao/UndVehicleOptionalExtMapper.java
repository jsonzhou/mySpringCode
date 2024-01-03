package com.wxcp.server.price.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxcp.server.price.vo.UndVehicleOptionalExt;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

/**
* @author xuyuxiang
* @description 针对表【und_vehicle_optional_ext】的数据库操作Mapper
* @createDate 2023-08-07 11:52:22
* @Entity com.ict.business.domain.UndVehicleOptionalExt
*/
@Component
public interface UndVehicleOptionalExtMapper extends BaseMapper<UndVehicleOptionalExt> {

    @Update("truncate table und_vehicle_optional_ext")
    public void truncate();

}




