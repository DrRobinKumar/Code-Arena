package com.codearena.service;

import com.codearena.dto.request.TagCreateRequest;
import com.codearena.dto.response.TagResponse;

import java.util.List;

public interface TagService {

    TagResponse createTag(TagCreateRequest request);

    List<TagResponse> getAllTags();
}
