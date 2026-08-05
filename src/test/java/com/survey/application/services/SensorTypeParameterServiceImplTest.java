package com.survey.application.services;

import com.survey.application.dtos.SensorTypeParameterCreateDto;
import com.survey.application.dtos.SensorTypeParameterDto;
import com.survey.application.dtos.SensorTypeParameterEditDto;
import com.survey.application.dtos.UseSensorTypeParameterDto;
import com.survey.domain.models.SensorParameterDefinition;
import com.survey.domain.models.SensorType;
import com.survey.domain.models.SensorTypeParameter;
import com.survey.domain.repository.SensorParameterDefinitionRepository;
import com.survey.domain.repository.SensorTypeParameterRepository;
import com.survey.domain.repository.SensorTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorTypeParameterServiceImplTest {

    @Mock
    private SensorTypeParameterRepository sensorTypeParameterRepository;
    @Mock
    private SensorTypeRepository sensorTypeRepository;
    @Mock
    private SensorParameterDefinitionRepository sensorParameterDefinitionRepository;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private InitialSurveyService initialSurveyService;

    private SensorTypeParameterServiceImpl service;
    private SensorType sensorType;

    @BeforeEach
    void setUp() {
        service = new SensorTypeParameterServiceImpl(
                sensorTypeParameterRepository,
                sensorTypeRepository,
                sensorParameterDefinitionRepository,
                new SensorParameterDefinitionValidator(sensorParameterDefinitionRepository),
                initialSurveyService);
        sensorType = new SensorType(UUID.randomUUID(), "kestrel", "Kestrel", "profile", null, null);
        lenient().when(initialSurveyService.isPublished()).thenReturn(false);
    }

    @Test
    void create_savesNewRawParameter() {
        when(sensorTypeRepository.findById(sensorType.getId())).thenReturn(Optional.of(sensorType));
        when(sensorTypeParameterRepository.existsBySensorTypeIdAndCode(sensorType.getId(), "temperature"))
                .thenReturn(false);
        when(sensorTypeParameterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SensorTypeParameterDto dto = service.create(sensorType.getId(),
                new SensorTypeParameterCreateDto("temperature", "Raw Temp", "decimal", "C"));

        assertThat(dto.code()).isEqualTo("temperature");
        assertThat(dto.sensorTypeCode()).isEqualTo("kestrel");
        assertThat(dto.usedParameterId()).isNull();
    }

    @Test
    void create_rejectsDuplicateCodeForSameSensorType() {
        when(sensorTypeRepository.findById(sensorType.getId())).thenReturn(Optional.of(sensorType));
        when(sensorTypeParameterRepository.existsBySensorTypeIdAndCode(sensorType.getId(), "temperature"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(sensorType.getId(),
                new SensorTypeParameterCreateDto("temperature", "Raw Temp", "decimal", "C")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already declares");

        verify(sensorTypeParameterRepository, never()).save(any());
    }

    @Test
    void create_isRejectedOnceTheInitialSurveyIsPublished() {
        when(initialSurveyService.isPublished()).thenReturn(true);

        assertThatThrownBy(() -> service.create(sensorType.getId(),
                new SensorTypeParameterCreateDto("temperature", "Raw Temp", "decimal", "C")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been published");
    }

    @Test
    void delete_rejectsWhileUsed() {
        UUID id = UUID.randomUUID();
        SensorTypeParameter raw = rawParameter(id, "temperature");
        raw.setUsedParameter(new SensorParameterDefinition());
        when(sensorTypeParameterRepository.findById(id)).thenReturn(Optional.of(raw));

        assertThatThrownBy(() -> service.delete(sensorType.getId(), id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unuse it before deleting");

        verify(sensorTypeParameterRepository, never()).delete(any());
    }

    @Test
    void delete_deletesUnusedRawParameter() {
        UUID id = UUID.randomUUID();
        SensorTypeParameter raw = rawParameter(id, "temperature");
        when(sensorTypeParameterRepository.findById(id)).thenReturn(Optional.of(raw));

        service.delete(sensorType.getId(), id);

        verify(sensorTypeParameterRepository).delete(raw);
    }

    @Test
    void use_linksToExistingUsedParameterWithNextPriority() {
        UUID rawId = UUID.randomUUID();
        SensorTypeParameter raw = rawParameter(rawId, "temperature");
        UUID usedId = UUID.randomUUID();
        SensorParameterDefinition used = new SensorParameterDefinition();
        used.setId(usedId);
        used.setCode("temperature");
        SensorTypeParameter existingSource = rawParameter(UUID.randomUUID(), "temp");
        existingSource.setUsedParameter(used);
        existingSource.setPriorityOrder(0);

        when(sensorTypeParameterRepository.findById(rawId)).thenReturn(Optional.of(raw));
        when(sensorParameterDefinitionRepository.findById(usedId)).thenReturn(Optional.of(used));
        when(sensorTypeParameterRepository.findByUsedParameterIdOrderByPriorityOrder(usedId))
                .thenReturn(List.of(existingSource));
        when(sensorTypeParameterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SensorTypeParameterDto dto = service.use(sensorType.getId(), rawId,
                new UseSensorTypeParameterDto(usedId, null, null, null, false));

        assertThat(dto.usedParameterId()).isEqualTo(usedId);
        assertThat(dto.priorityOrder()).isEqualTo(1);
    }

    @Test
    void use_createsNewUsedParameterWhenNoIdGiven() {
        UUID rawId = UUID.randomUUID();
        SensorTypeParameter raw = rawParameter(rawId, "temperature");
        when(sensorTypeParameterRepository.findById(rawId)).thenReturn(Optional.of(raw));
        when(sensorParameterDefinitionRepository.findAll()).thenReturn(List.of());
        when(sensorParameterDefinitionRepository.count()).thenReturn(0L);
        when(sensorParameterDefinitionRepository.save(any())).thenAnswer(invocation -> {
            SensorParameterDefinition saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(sensorTypeParameterRepository.findByUsedParameterIdOrderByPriorityOrder(any())).thenReturn(List.of());
        when(sensorTypeParameterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SensorTypeParameterDto dto = service.use(sensorType.getId(), rawId,
                new UseSensorTypeParameterDto(null, "Temperature", "decimal", "C", true));

        assertThat(dto.usedParameterId()).isNotNull();
        assertThat(dto.usedParameterCode()).isEqualTo("temperature");
        assertThat(dto.priorityOrder()).isEqualTo(0);
    }

    @Test
    void unuse_clearsLinkAndPriority() {
        UUID rawId = UUID.randomUUID();
        SensorTypeParameter raw = rawParameter(rawId, "temperature");
        SensorParameterDefinition used = new SensorParameterDefinition();
        used.setId(UUID.randomUUID());
        raw.setUsedParameter(used);
        raw.setPriorityOrder(2);
        when(sensorTypeParameterRepository.findById(rawId)).thenReturn(Optional.of(raw));
        when(sensorTypeParameterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SensorTypeParameterDto dto = service.unuse(sensorType.getId(), rawId);

        assertThat(dto.usedParameterId()).isNull();
        assertThat(dto.priorityOrder()).isZero();
    }

    @Test
    void reorderSources_appliesGivenOrder() {
        UUID usedId = UUID.randomUUID();
        SensorParameterDefinition used = new SensorParameterDefinition();
        used.setId(usedId);
        when(sensorParameterDefinitionRepository.findById(usedId)).thenReturn(Optional.of(used));

        SensorTypeParameter first = rawParameter(UUID.randomUUID(), "a");
        first.setUsedParameter(used);
        first.setPriorityOrder(0);
        SensorTypeParameter second = rawParameter(UUID.randomUUID(), "b");
        second.setUsedParameter(used);
        second.setPriorityOrder(1);

        when(sensorTypeParameterRepository.findByUsedParameterIdOrderByPriorityOrder(usedId))
                .thenReturn(List.of(first, second));
        when(sensorTypeParameterRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SensorTypeParameterDto> result = service.reorderSources(usedId, List.of(second.getId(), first.getId()));

        assertThat(second.getPriorityOrder()).isZero();
        assertThat(first.getPriorityOrder()).isEqualTo(1);
        assertThat(result).extracting(SensorTypeParameterDto::id)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    void reorderSources_rejectsMismatchedIdSet() {
        UUID usedId = UUID.randomUUID();
        SensorParameterDefinition used = new SensorParameterDefinition();
        used.setId(usedId);
        when(sensorParameterDefinitionRepository.findById(usedId)).thenReturn(Optional.of(used));

        SensorTypeParameter first = rawParameter(UUID.randomUUID(), "a");
        first.setUsedParameter(used);
        when(sensorTypeParameterRepository.findByUsedParameterIdOrderByPriorityOrder(usedId))
                .thenReturn(List.of(first));

        assertThatThrownBy(() -> service.reorderSources(usedId, List.of(UUID.randomUUID())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly the sources");

        verify(sensorTypeParameterRepository, never()).saveAll(any());
    }

    @Test
    void list_throwsWhenSensorTypeMissing() {
        UUID missing = UUID.randomUUID();
        when(sensorTypeRepository.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.list(missing)).isInstanceOf(NoSuchElementException.class);
    }

    private SensorTypeParameter rawParameter(UUID id, String code) {
        SensorTypeParameter parameter = new SensorTypeParameter();
        parameter.setId(id);
        parameter.setSensorType(sensorType);
        parameter.setCode(code);
        parameter.setName(code);
        parameter.setDataType("decimal");
        return parameter;
    }
}
