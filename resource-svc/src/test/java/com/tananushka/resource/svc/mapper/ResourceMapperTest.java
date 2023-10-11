package com.tananushka.resource.svc.mapper;

import com.tananushka.resource.svc.dto.SongRequest;
import com.tananushka.resource.svc.dto.ResourceResponse;
import com.tananushka.resource.svc.entity.Resource;
import org.apache.tika.metadata.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ResourceMapperTest {

    @InjectMocks
    private ResourceMapper resourceMapper;

    @Test
    void testToResponse_WhenGivenResource_ThenReturnResourceResponse() {
        Resource resource = setupResource();

        ResourceResponse response = resourceMapper.toResponse(resource);

        assertEquals(123, response.getId());
    }

    @Test
    void testToRequest_WhenValidMetadata_ThenReturnSongRequest() {
        Metadata metadata = setupValidMetadata();

        SongRequest request = resourceMapper.toRequest(1L, metadata);

        assertEquals(1, request.getId());
        assertEquals("ArtistName", request.getArtist());
        assertEquals("SongTitle", request.getName());
        assertEquals("AlbumName", request.getAlbum());
        assertEquals("2022", request.getYear());
        assertEquals("05:00", request.getDuration());
    }

    @Test
    void testToRequest_WhenDurationIsNull_ThenReturnSongRequestWithoutDuration() {
        Metadata metadata = new Metadata();

        SongRequest request = resourceMapper.toRequest(1L, metadata);

        assertNull(request.getDuration());
    }

    @Test
    void testToRequest_WhenDurationIs125000ms_ThenReturn02_05() {
        Metadata metadata = new Metadata();
        metadata.set("xmpDM:duration", "125000");  // 2 minutes, 5 seconds

        SongRequest request = resourceMapper.toRequest(1L, metadata);

        assertEquals("02:05", request.getDuration());
    }

    @Test
    void testToRequest_WhenDurationIs0ms_ThenReturn00_00() {
        Metadata metadata = new Metadata();
        metadata.set("xmpDM:duration", "0");

        SongRequest request = resourceMapper.toRequest(1L, metadata);

        assertEquals("00:00", request.getDuration());
    }

    @Test
    void testToRequest_WhenInvalidDurationFormat_ThenThrowNumberFormatException() {
        Metadata metadata = new Metadata();
        metadata.set("xmpDM:duration", "invalidDuration");

        assertThrows(NumberFormatException.class, () -> resourceMapper.toRequest(1L, metadata));
    }

    private Resource setupResource() {
        Resource resource = new Resource();
        resource.setId(123L);
        return resource;
    }

    private Metadata setupValidMetadata() {
        Metadata metadata = new Metadata();
        metadata.set("xmpDM:artist", "ArtistName");
        metadata.set("title", "SongTitle");
        metadata.set("xmpDM:album", "AlbumName");
        metadata.set("xmpDM:releaseDate", "2022");
        metadata.set("xmpDM:duration", "300000");
        return metadata;
    }
}
