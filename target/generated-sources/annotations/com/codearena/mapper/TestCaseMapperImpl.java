package com.codearena.mapper;

import com.codearena.dto.request.TestCaseRequest;
import com.codearena.dto.response.TestCaseResponse;
import com.codearena.entity.TestCase;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-03T14:05:02+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.1 (Oracle Corporation)"
)
@Component
public class TestCaseMapperImpl implements TestCaseMapper {

    @Override
    public TestCaseResponse toResponse(TestCase testCase) {
        if ( testCase == null ) {
            return null;
        }

        TestCaseResponse.TestCaseResponseBuilder testCaseResponse = TestCaseResponse.builder();

        testCaseResponse.id( testCase.getId() );
        testCaseResponse.input( testCase.getInput() );
        testCaseResponse.expectedOutput( testCase.getExpectedOutput() );
        testCaseResponse.hidden( testCase.isHidden() );

        return testCaseResponse.build();
    }

    @Override
    public TestCase toEntity(TestCaseRequest request) {
        if ( request == null ) {
            return null;
        }

        TestCase.TestCaseBuilder testCase = TestCase.builder();

        testCase.input( request.getInput() );
        testCase.expectedOutput( request.getExpectedOutput() );
        if ( request.getHidden() != null ) {
            testCase.hidden( request.getHidden() );
        }

        return testCase.build();
    }
}
