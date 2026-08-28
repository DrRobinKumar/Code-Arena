package com.codearena.mapper;

import com.codearena.dto.request.TestCaseRequest;
import com.codearena.dto.response.TestCaseResponse;
import com.codearena.entity.TestCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TestCaseMapper {

    TestCaseResponse toResponse(TestCase testCase);

    // No @Mapping for "id": TestCase uses plain @Builder, so TestCaseBuilder only
    // exposes TestCase's own fields — "id" lives on BaseEntity and isn't reachable
    // through this builder. "problem" IS a real property here (declared directly
    // on TestCase), so it's explicitly ignored — it's set afterward via
    // Problem.replaceTestCases(), not from the incoming request.
    @Mapping(target = "problem", ignore = true)
    TestCase toEntity(TestCaseRequest request);
}
