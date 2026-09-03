package com.resource.bookingsystem.service;

import com.resource.bookingsystem.dto.ResourceRequest;
import com.resource.bookingsystem.entity.Resource;
import com.resource.bookingsystem.entity.ResourceType;
import com.resource.bookingsystem.repository.ResourceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceService resourceService;

    private Resource testResource;

    @BeforeEach
    void setUp() {
        testResource = new Resource("Meeting Room 1", ResourceType.ROOM, "Floor 2", new BigDecimal("50.00"), true);
        testResource.setId(1L);
    }

    @Test
    @DisplayName("Should return paginated resources")
    void testGetAllResources() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(resourceRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testResource)));

        Page<Resource> result = resourceService.getAllResources(pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("Meeting Room 1", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Should return resource by id when exists")
    void testGetResourceByIdSuccess() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));

        Resource result = resourceService.getResourceById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should throw 404 when resource not found")
    void testGetResourceByIdNotFound() {
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> resourceService.getResourceById(99L));
    }

    @Test
    @DisplayName("Should create resource successfully")
    void testCreateResource() {
        ResourceRequest request = new ResourceRequest("Pod A", ResourceType.ROOM, "Floor 1", new BigDecimal("25.00"), true);
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Resource created = resourceService.createResource(request);
        assertNotNull(created);
        assertEquals("Pod A", created.getName());
    }

    @Test
    @DisplayName("Should update resource successfully")
    void testUpdateResource() {
        ResourceRequest updateRequest = new ResourceRequest("Meeting Room 1 Updated", ResourceType.ROOM, "Floor 3", new BigDecimal("75.00"), false);
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Resource updated = resourceService.updateResource(1L, updateRequest);
        assertEquals("Meeting Room 1 Updated", updated.getName());
        assertEquals("Floor 3", updated.getLocation());
        assertEquals(new BigDecimal("75.00"), updated.getPricePerHour());
    }

    @Test
    @DisplayName("Should delete resource successfully")
    void testDeleteResource() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));

        resourceService.deleteResource(1L);
        verify(resourceRepository).delete(testResource);
    }

    @Test
    @DisplayName("Should find all resources list")
    void testFindAllResources() {
        when(resourceRepository.findAll()).thenReturn(List.of(testResource));

        List<Resource> result = resourceService.findAllResources();
        assertEquals(1, result.size());
    }
}
