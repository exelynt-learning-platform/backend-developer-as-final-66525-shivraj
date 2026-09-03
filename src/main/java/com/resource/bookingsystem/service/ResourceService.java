package com.resource.bookingsystem.service;

import com.resource.bookingsystem.dto.ResourceRequest;
import com.resource.bookingsystem.entity.Resource;
import com.resource.bookingsystem.repository.ResourceRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceService.class);
    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional(readOnly = true)
    public Page<Resource> getAllResources(Pageable pageable) {
        return resourceRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Resource getResourceById(Long id) {
        return resourceRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found with id: " + id));
    }

    public Resource createResource(ResourceRequest request) {
        log.info("Creating new resource: {}", request.name());
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
        log.info("Updating resource with id: {}", id);
        Resource resource = getResourceById(id);
        resource.setName(request.name());
        resource.setType(request.type());
        resource.setLocation(request.location());
        resource.setPricePerHour(request.pricePerHour());
        resource.setAvailable(request.available());
        return resourceRepository.save(resource);
    }

    public void deleteResource(Long id) {
        log.info("Deleting resource with id: {}", id);
        Resource resource = getResourceById(id);
        resourceRepository.delete(resource);
    }

    @Transactional(readOnly = true)
    public List<Resource> findAllResources() {
        return resourceRepository.findAll();
    }
}
