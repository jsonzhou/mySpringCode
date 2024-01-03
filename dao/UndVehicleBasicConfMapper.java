package com.wxcp.server.price.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxcp.server.price.vo.UndVehicleBasicConf;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

/**
* @author xuyuxiang
* @description 针对表【und_vehicle_basic_conf】的数据库操作Mapper
* @createDate 2023-07-20 16:56:38
* @Entity com.ict.business.domain.UndVehicleBasicConf
*/
@Component
public interface UndVehicleBasicConfMapper extends BaseMapper<UndVehicleBasicConf> {

    @Update("truncate table und_vehicle_basic_conf")
    public void truncate();

}




