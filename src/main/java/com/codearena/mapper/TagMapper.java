package com.codearena.mapper;

import com.codearena.dto.request.TagCreateRequest;
import com.codearena.dto.response.TagResponse;
import com.codearena.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {

    TagResponse toResponse(Tag tag);

    // No @Mapping for "id" here: Tag uses plain @Builder (not @SuperBuilder), so
    // Tag.TagBuilder only exposes Tag's own fields — "id" lives on BaseEntity and
    // isn't a settable property on this builder at all. MapStruct's
    // unmappedTargetPolicy = IGNORE means the (nonexistent) mapping is silently
    // skipped, which is exactly what we want for a newly created entity.
    Tag toEntity(TagCreateRequest request);
}
