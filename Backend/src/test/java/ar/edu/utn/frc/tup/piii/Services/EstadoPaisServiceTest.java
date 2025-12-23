package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaisDto;
import ar.edu.utn.frc.tup.piii.Entities.EstadoPaisEntity;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoPaisRepository;
import ar.edu.utn.frc.tup.piii.Services.ServicesImpl.EstadoPaisServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EstadoPaisServiceTest {
    @Mock
    private EstadoPaisRepository estadoPaisRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EstadoPaisServiceImpl estadoPaisService;

    @Test
    void testGetEstadoPais_Success() {
        Long id = 1L;

        EstadoPaisEntity entity = new EstadoPaisEntity();
        entity.setIdEstadoPais(id);
        entity.setCantidadTropas(5);

        EstadoPaisDto dto = new EstadoPaisDto();
        dto.setId(id);
        dto.setCantidadTropas(5);

        when(estadoPaisRepository.findById(id)).thenReturn(Optional.of(entity));
        when(modelMapper.map(entity, EstadoPaisDto.class)).thenReturn(dto);

        EstadoPaisDto result = estadoPaisService.getEstadoPais(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(5, result.getCantidadTropas());
    }
    
}
