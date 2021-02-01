package com.cunjun.personal.emos.wx.db.dao;

import com.cunjun.personal.emos.wx.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TbFaceModelDao {
    int deleteByPrimaryKey(Integer id);

    int insert(TbFaceModel record);

    int insertSelective(TbFaceModel record);

    TbFaceModel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TbFaceModel record);

    int updateByPrimaryKey(TbFaceModel record);

    String selectFaceModelByUserId(@Param("userId") Integer userId);

    int deleteFaceModelByUserId(@Param("userId") Integer userId);
}