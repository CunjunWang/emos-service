package com.cunjun.personal.emos.wx.db.dao;

import com.cunjun.personal.emos.wx.db.pojo.TbWorkday;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TbWorkdayDao {
    int deleteByPrimaryKey(Integer id);

    int insert(TbWorkday record);

    int insertSelective(TbWorkday record);

    TbWorkday selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TbWorkday record);

    int updateByPrimaryKey(TbWorkday record);

    Integer selectTodayIsWorkday();

    List<String> searchWorkdayInRange(@Param("start") String start,
                                      @Param("end") String end);
}