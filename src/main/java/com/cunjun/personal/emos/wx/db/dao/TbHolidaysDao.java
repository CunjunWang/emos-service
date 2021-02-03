package com.cunjun.personal.emos.wx.db.dao;

import com.cunjun.personal.emos.wx.db.pojo.TbHolidays;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TbHolidaysDao {
    int deleteByPrimaryKey(Integer id);

    int insert(TbHolidays record);

    int insertSelective(TbHolidays record);

    TbHolidays selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TbHolidays record);

    int updateByPrimaryKey(TbHolidays record);

    Integer selectTodayIsHoliday();

    List<String> searchHolidayInRange(@Param("start") String start,
                                      @Param("end") String end);
}