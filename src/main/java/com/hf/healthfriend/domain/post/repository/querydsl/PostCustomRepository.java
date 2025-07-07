package com.hf.healthfriend.domain.post.repository.querydsl;

import com.hf.healthfriend.domain.member.constant.FitnessLevel;
import com.hf.healthfriend.domain.post.constant.PostCategory;
import com.hf.healthfriend.domain.post.dto.response.PostListObject;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostCustomRepository {
    List<PostListObject> getList(FitnessLevel fitnessLevel, PostCategory postCategory, String keyword, Pageable pageable);
    List<PostListObject> getPopularList(List<Long> postIdList, FitnessLevel fitnessLevel, String keyword);
    Long getSearchedPostListSize(FitnessLevel fitnessLevel, PostCategory postCategory, String keyword);
    Long getTotalPageSize(int size);
}
