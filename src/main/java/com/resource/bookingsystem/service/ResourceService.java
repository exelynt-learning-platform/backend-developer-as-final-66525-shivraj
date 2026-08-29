package com.resource.bookingsystem.service;

import com.resource.bookingsystem.dto.ResourceRequest;
import com.resource.bookingsystem.entity.Resource;
import com.resource.bookingsystem.entity.User;
import com.resource.bookingsystem.repository.ResourceRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Page<Resource> getAllResources(Pageable pageable) {
        return resourceRepository.findAll(pageable);
    }

    public Resource getResourceById(Long id) {
        return resourceRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
    }

    public Resource createResource(ResourceRequest request) {
        Resource resource = new Resource(
            request.name(),
            request.type(),
            request.location(),
            request.pricePerHour(),
            request.available()
        );
        return resourceRepository.save(resource);
    }

    public Resource updateResource(Long id, ResourceRequest request) {
        Resource resource = getResourceById(id);
        resource.setName(request.name());
        resource.setType(request.type());
        resource.setLocation(request.location());
        resource.setPricePerHour(request.pricePerHour());
        resource.setAvailable(request.available());
        return resourceRepository.save(resource);
    }

    public void deleteResource(Long id) {
        Resource resource = getResourceById(id);
        resourceRepository.delete(resource);
    }

    public List<Resource> findAllResources() {
        return resourceRepository.findAll();
    }

    public User getCurrentUser(AuthService authService) {
        return authService.getCurrentUser();
    }
}
