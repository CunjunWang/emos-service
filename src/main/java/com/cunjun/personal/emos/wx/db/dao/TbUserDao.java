package com.cunjun.personal.emos.wx.db.dao;

import com.cunjun.personal.emos.wx.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.Set;

@Mapper
public interface TbUserDao {
    int deleteByPrimaryKey(Integer id);

    int insert(TbUser record);

    int insertSelective(TbUser record);

    TbUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TbUser record);

    int updateByPrimaryKey(TbUser record);

    boolean hasRootUser();

    TbUser selectValidUserById(@Param("userId") Integer userId);

    Integer searchIdByOpenId(@Param("openId") String openId);

    Set<String> searchUserPermissions(@Param("userId") Integer userId);

    HashMap<String, String> searchNameAndDept(@Param("userId") Integer userId);

    String searchUserHireDate(@Param("userId") Integer userId);

    HashMap<String, String> searchUserSummary(@Param("userId") Integer userId);
}