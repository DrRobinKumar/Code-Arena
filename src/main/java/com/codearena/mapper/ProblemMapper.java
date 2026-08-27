package com.codearena.mapper;

import com.codearena.dto.response.ProblemAdminResponse;
import com.codearena.dto.response.ProblemResponse;
import com.codearena.dto.response.ProblemSummaryResponse;
import com.codearena.dto.response.TestCaseResponse;
import com.codearena.entity.Problem;
import com.codearena.entity.ProblemTag;
import com.codearena.entity.TestCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Three distinct output shapes from the same entity, deliberately kept as
 * separate mapping methods rather than one generic method + post-processing:
 *  - toSummary: list/search rows — no body content at all.
 *  - toPublicResponse: full body, but testCases filtered to visible-only
 *    and no editorial — this is the security boundary for regular users.
 *  - toAdminResponse: everything, unfiltered.
 * uses = TestCaseMapper.class lets MapStruct auto-generate the "testCases"
 * (admin, all) list mapping since the field name matches Problem.testCases;
 * the filtered "visibleTestCases" list needs an explicit default method
 * since it's a derived, not a direct, mapping.
 */
@Mapper(componentModel = "spring", uses = TestCaseMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProblemMapper {

    @Mapping(target = "tags", expression = "java(mapTagNames(problem.getProblemTags()))")
    ProblemSummaryResponse toSummary(Problem problem);

    @Mapping(target = "tags", expression = "java(mapTagNames(problem.getProblemTags()))")
    @Mapping(target = "visibleTestCases", expression = "java(mapVisibleTestCases(problem.getTestCases()))")
    ProblemResponse toPublicResponse(Problem problem);

    @Mapping(target = "tags", expression = "java(mapTagNames(problem.getProblemTags()))")
    ProblemAdminResponse toAdminResponse(Problem problem);

    default List<String> mapTagNames(List<ProblemTag> problemTags) {
        if (problemTags == null) {
            return List.of();
        }
        return problemTags.stream()
                .map(pt -> pt.getTag().getName())
                .sorted()
                .toList();
    }

    default List<TestCaseResponse> mapVisibleTestCases(List<TestCase> testCases) {
        if (testCases == null) {
            return List.of();
        }
        return testCases.stream()
                .filter(tc -> !tc.isHidden())
                .map(tc -> TestCaseResponse.builder()
                        .id(tc.getId())
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .hidden(tc.isHidden())
                        .build())
                .toList();
    }
}
