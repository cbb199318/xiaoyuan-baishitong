package com.campus.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.platform.entity.BeautyFavorite;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BeautyFavoriteMapper extends BaseMapper<BeautyFavorite> {

    @Select("SELECT id, good_id, user_id, created_at, updated_at, deleted FROM beauty_favorite WHERE good_id = #{goodId} AND user_id = #{userId} LIMIT 1")
    BeautyFavorite selectAny(Long goodId, Long userId);

    @Select("SELECT id, good_id, user_id, created_at, updated_at, deleted FROM beauty_favorite WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<BeautyFavorite> selectActiveByUserId(Long userId);

    @Delete("DELETE FROM beauty_favorite WHERE id = #{id}")
    int hardDeleteById(Long id);
}
