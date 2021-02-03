package com.cunjun.personal.emos.wx.db.dao;

import com.cunjun.personal.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbCheckinDao {
    int deleteByPrimaryKey(Integer id);

    int insert(TbCheckin record);

    int insertSelective(TbCheckin record);

    TbCheckin selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TbCheckin record);

    int updateByPrimaryKey(TbCheckin record);

    Integer userHasCheckedInBetween(@Param("userId") Integer userId,
                                    @Param("start") String start,
                                    @Param("end") String end);

    Integer selectByUserAndDate(@Param("userId") Integer userId,
                                @Param("today") String today);

    HashMap<String, Object> selectTodayCheckinByUserId(@Param("userId") Integer userId);

    Long selectTotalCheckinDaysByUserId(@Param("userId") Integer userId);

    List<HashMap<String, String>> selectWeeklyCheckinByUserId(@Param("userId") Integer userId,
                                                              @Param("start") String start,
                                                              @Param("end") String end);
}