package com.campus.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.platform.entity.JobsFavorite;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JobsFavoriteMapper extends BaseMapper<JobsFavorite> {

    @Select("SELECT id, post_id, user_id, created_at, updated_at, deleted FROM jobs_favorite WHERE post_id = #{postId} AND user_id = #{userId} LIMIT 1")
    JobsFavorite selectAny(Long postId, Long userId);

    @Select("SELECT id, post_id, user_id, created_at, updated_at, deleted FROM jobs_favorite WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<JobsFavorite> selectActiveByUserId(Long userId);

    @Delete("DELETE FROM jobs_favorite WHERE id = #{id}")
    int hardDeleteById(Long id);
}
