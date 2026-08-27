package com.codearena.mapper;

import com.codearena.dto.response.ExampleResponse;
import com.codearena.dto.response.ProblemAdminResponse;
import com.codearena.dto.response.ProblemResponse;
import com.codearena.dto.response.ProblemSummaryResponse;
import com.codearena.entity.Problem;
import com.codearena.entity.ProblemExample;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-03T14:05:02+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.1 (Oracle Corporation)"
)
@Component
public class ProblemMapperImpl implements ProblemMapper {

    @Override
    public ProblemSummaryResponse toSummary(Problem problem) {
        if ( problem == null ) {
            return null;
        }

        ProblemSummaryResponse.ProblemSummaryResponseBuilder problemSummaryResponse = ProblemSummaryResponse.builder();

        problemSummaryResponse.id( problem.getId() );
        problemSummaryResponse.title( problem.getTitle() );
        problemSummaryResponse.slug( problem.getSlug() );
        problemSummaryResponse.difficulty( problem.getDifficulty() );
        problemSummaryResponse.createdAt( problem.getCreatedAt() );

        problemSummaryResponse.tags( mapTagNames(problem.getProblemTags()) );

        return problemSummaryResponse.build();
    }

    @Override
    public ProblemResponse toPublicResponse(Problem problem) {
        if ( problem == null ) {
            return null;
        }

        ProblemResponse.ProblemResponseBuilder problemResponse = ProblemResponse.builder();

        problemResponse.id( problem.getId() );
        problemResponse.title( problem.getTitle() );
        problemResponse.slug( problem.getSlug() );
        problemResponse.difficulty( problem.getDifficulty() );
        problemResponse.description( problem.getDescription() );
        problemResponse.constraints( problem.getConstraints() );
        problemResponse.inputFormat( problem.getInputFormat() );
        problemResponse.outputFormat( problem.getOutputFormat() );
        problemResponse.timeLimitMs( problem.getTimeLimitMs() );
        problemResponse.memoryLimitKb( problem.getMemoryLimitKb() );
        List<String> list = problem.getHints();
        if ( list != null ) {
            problemResponse.hints( new ArrayList<String>( list ) );
        }
        problemResponse.examples( problemExampleListToExampleResponseList( problem.getExamples() ) );

        problemResponse.tags( mapTagNames(problem.getProblemTags()) );
        problemResponse.visibleTestCases( mapVisibleTestCases(problem.getTestCases()) );

        return problemResponse.build();
    }

    @Override
    public ProblemAdminResponse toAdminResponse(Problem problem) {
        if ( problem == null ) {
            return null;
        }

        ProblemAdminResponse.ProblemAdminResponseBuilder problemAdminResponse = ProblemAdminResponse.builder();

        problemAdminResponse.id( problem.getId() );
        problemAdminResponse.title( problem.getTitle() );
        problemAdminResponse.slug( problem.getSlug() );
        problemAdminResponse.difficulty( problem.getDifficulty() );
        problemAdminResponse.description( problem.getDescription() );
        problemAdminResponse.constraints( problem.getConstraints() );
        problemAdminResponse.inputFormat( problem.getInputFormat() );
        problemAdminResponse.outputFormat( problem.getOutputFormat() );
        problemAdminResponse.editorial( problem.getEditorial() );
        problemAdminResponse.timeLimitMs( problem.getTimeLimitMs() );
        problemAdminResponse.memoryLimitKb( problem.getMemoryLimitKb() );
        List<String> list = problem.getHints();
        if ( list != null ) {
            problemAdminResponse.hints( new ArrayList<String>( list ) );
        }
        problemAdminResponse.examples( problemExampleListToExampleResponseList( problem.getExamples() ) );
        problemAdminResponse.testCases( mapVisibleTestCases( problem.getTestCases() ) );
        problemAdminResponse.createdAt( problem.getCreatedAt() );
        problemAdminResponse.updatedAt( problem.getUpdatedAt() );

        problemAdminResponse.tags( mapTagNames(problem.getProblemTags()) );

        return problemAdminResponse.build();
    }

    protected ExampleResponse problemExampleToExampleResponse(ProblemExample problemExample) {
        if ( problemExample == null ) {
            return null;
        }

        ExampleResponse.ExampleResponseBuilder exampleResponse = ExampleResponse.builder();

        exampleResponse.input( problemExample.getInput() );
        exampleResponse.output( problemExample.getOutput() );
        exampleResponse.explanation( problemExample.getExplanation() );

        return exampleResponse.build();
    }

    protected List<ExampleResponse> problemExampleListToExampleResponseList(List<ProblemExample> list) {
        if ( list == null ) {
            return null;
        }

        List<ExampleResponse> list1 = new ArrayList<ExampleResponse>( list.size() );
        for ( ProblemExample problemExample : list ) {
            list1.add( problemExampleToExampleResponse( problemExample ) );
        }

        return list1;
    }
}
