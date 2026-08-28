package com.codearena.mapper;

import com.codearena.dto.request.TagCreateRequest;
import com.codearena.dto.response.TagResponse;
import com.codearena.entity.Tag;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-03T14:05:02+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.1 (Oracle Corporation)"
)
@Component
public class TagMapperImpl implements TagMapper {

    @Override
    public TagResponse toResponse(Tag tag) {
        if ( tag == null ) {
            return null;
        }

        TagResponse.TagResponseBuilder tagResponse = TagResponse.builder();

        tagResponse.id( tag.getId() );
        tagResponse.name( tag.getName() );

        return tagResponse.build();
    }

    @Override
    public Tag toEntity(TagCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Tag.TagBuilder tag = Tag.builder();

        tag.name( request.getName() );

        return tag.build();
    }
}
