package com.codearena.serviceImpl;

import com.codearena.dto.request.TagCreateRequest;
import com.codearena.dto.response.TagResponse;
import com.codearena.entity.Tag;
import com.codearena.exception.DuplicateResourceException;
import com.codearena.mapper.TagMapper;
import com.codearena.repository.TagRepository;
import com.codearena.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    @Transactional
    public TagResponse createTag(TagCreateRequest request) {
        if (tagRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Tag '" + request.getName() + "' already exists");
        }
        Tag saved = tagRepository.save(Tag.builder().name(request.getName().trim()).build());
        return tagMapper.toResponse(saved);
    }

    @Override
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tagMapper::toResponse)
                .sorted(Comparator.comparing(TagResponse::getName))
                .toList();
    }
}
