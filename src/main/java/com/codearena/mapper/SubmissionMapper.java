package com.codearena.mapper;

import com.codearena.dto.response.SubmissionDetailResponse;
import com.codearena.dto.response.SubmissionResponse;
import com.codearena.entity.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubmissionMapper {

    @Mapping(target = "problemTitle", source = "problem.title")
    @Mapping(target = "problemSlug", source = "problem.slug")
    @Mapping(target = "username", source = "user.username")
    SubmissionResponse toResponse(Submission submission);

    @Mapping(target = "problemTitle", source = "problem.title")
    @Mapping(target = "problemSlug", source = "problem.slug")
    @Mapping(target = "username", source = "user.username")
    SubmissionDetailResponse toDetailResponse(Submission submission);
}
