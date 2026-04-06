package com.seuprojeto.ecommerce.role;

import com.seuprojeto.ecommerce.exception.BusinessException;
import com.seuprojeto.ecommerce.exception.ResourceNotFoundException;
import com.seuprojeto.ecommerce.role.dto.RoleRequestDTO;
import com.seuprojeto.ecommerce.role.dto.RoleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository repository;

    public RoleResponseDTO create(RoleRequestDTO dto) {

        if (repository.findByName(dto.name()).isPresent()) {
            throw new BusinessException("Role já existe");
        }

        Role role = new Role();
        role.setName(dto.name());

        return toResponse(repository.save(role));
    }

    public List<RoleResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RoleResponseDTO findById(Long id) {
        Role role = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role não encontrada")
                );

        return toResponse(role);
    }

    public RoleResponseDTO update(Long id, RoleRequestDTO dto) {

        Role role = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role não encontrada")
                );

        role.setName(dto.name());

        return toResponse(repository.save(role));
    }

    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Role não encontrada");
        }

        repository.deleteById(id);
    }

    private RoleResponseDTO toResponse(Role role) {
        return new RoleResponseDTO(
                role.getId(),
                role.getName()
        );
    }
}
