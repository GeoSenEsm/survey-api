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
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
    void use_linksToExistingUsedParameter() {
        UUID rawId = UUID.randomUUID();
        SensorTypeParameter raw = rawParameter(rawId, "temperature");
        UUID usedId = UUID.randomUUID();
        SensorParameterDefinition used = new SensorParameterDefinition();
        used.setId(usedId);
        used.setCode("temperature");

        when(sensorTypeParameterRepository.findById(rawId)).thenReturn(Optional.of(raw));
        when(sensorParameterDefinitionRepository.findById(usedId)).thenReturn(Optional.of(used));
        when(sensorTypeParameterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SensorTypeParameterDto dto = service.use(sensorType.getId(), rawId,
                new UseSensorTypeParameterDto(usedId, null, null, null));

        assertThat(dto.usedParameterId()).isEqualTo(usedId);
        // Linking to an already-existing used parameter never touches the manual source: that
        // guarantee is only established once, when the used parameter is first created.
        verify(sensorTypeRepository, never()).findByCode("manual");
    }

    @Test
    void use_createsNewUsedParameterWhenNoIdGiven() {
        UUID rawId = UUID.randomUUID();
        SensorTypeParameter raw = rawParameter(rawId, "temperature");
        SensorType manual = new SensorType(UUID.randomUUID(), "manual", "Manual", "manual", null, null);
        AtomicReference<SensorParameterDefinition> savedDefinition = new AtomicReference<>();

        when(sensorTypeParameterRepository.findById(rawId)).thenReturn(Optional.of(raw));
        when(sensorParameterDefinitionRepository.findAll()).thenReturn(List.of());
        when(sensorParameterDefinitionRepository.count()).thenReturn(0L);
        when(sensorParameterDefinitionRepository.save(any())).thenAnswer(invocation -> {
            SensorParameterDefinition saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            savedDefinition.set(saved);
            return saved;
        });
        when(sensorParameterDefinitionRepository.findById(any()))
                .thenAnswer(invocation -> Optional.ofNullable(savedDefinition.get()));
        when(sensorTypeParameterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sensorTypeRepository.findByCode("manual")).thenReturn(Optional.of(manual));
        when(sensorTypeParameterRepository.findBySensorTypeIdAndCode(eq(manual.getId()), any()))
                .thenReturn(Optional.empty());

        SensorTypeParameterDto dto = service.use(sensorType.getId(), rawId,
                new UseSensorTypeParameterDto(null, "Temperature", "decimal", "C"));

        assertThat(dto.usedParameterId()).isNotNull();
        assertThat(dto.usedParameterCode()).isEqualTo("temperature");
        // A brand-new used parameter always gets `manual` wired as a fallback source too.
        verify(sensorTypeParameterRepository).save(argThat(
                saved -> saved.getSensorType() == manual && "temperature".equals(saved.getCode())));
    }

    @Test
    void ensureManualSource_wiresManualWhenMissing() {
        UUID usedId = UUID.randomUUID();
        SensorParameterDefinition used = new SensorParameterDefinition();
        used.setId(usedId);
        used.setCode("temperature");
        used.setName("Temperature");
        used.setDataType("decimal");
        used.setUnit("C");
        SensorType manual = new SensorType(UUID.randomUUID(), "manual", "Manual", "manual", null, null);
        when(sensorParameterDefinitionRepository.findById(usedId)).thenReturn(Optional.of(used));
        when(sensorTypeRepository.findByCode("manual")).thenReturn(Optional.of(manual));
        when(sensorTypeParameterRepository.findBySensorTypeIdAndCode(manual.getId(), "temperature"))
                .thenReturn(Optional.empty());
        when(sensorTypeParameterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.ensureManualSource(usedId);

        verify(sensorTypeParameterRepository).save(argThat(
                saved -> saved.getSensorType() == manual && "temperature".equals(saved.getCode())
                        && saved.getUsedParameter() == used));
    }

    @Test
    void ensureManualSource_noOpsWhenAlreadyWiredToThisUsedParameter() {
        UUID usedId = UUID.randomUUID();
        SensorParameterDefinition used = new SensorParameterDefinition();
        used.setId(usedId);
        used.setCode("temperature");
        SensorType manual = new SensorType(UUID.randomUUID(), "manual", "Manual", "manual", null, null);
        SensorTypeParameter existingManualSource = rawParameter(UUID.randomUUID(), "temperature");
        existingManualSource.setUsedParameter(used);
        when(sensorParameterDefinitionRepository.findById(usedId)).thenReturn(Optional.of(used));
        when(sensorTypeRepository.findByCode("manual")).thenReturn(Optional.of(manual));
        when(sensorTypeParameterRepository.findBySensorTypeIdAndCode(manual.getId(), "temperature"))
                .thenReturn(Optional.of(existingManualSource));

        service.ensureManualSource(usedId);

        verify(sensorTypeParameterRepository, never()).save(any());
    }

    /**
     * Deleting a used parameter only clears sensor_type_parameter.used_parameter_id
     * (ON DELETE SET NULL) rather than removing the raw row, so a later parameter re-created with
     * the same code finds an orphaned manual raw row still sitting there. It must be re-wired to
     * the new used parameter, not mistaken for "manual already guaranteed" and left orphaned —
     * otherwise the new parameter silently ends up with no manual fallback.
     */
    @Test
    void ensureManualSource_rewiresOrphanedManualRowLeftByADeletedParameter() {
        UUID usedId = UUID.randomUUID();
        SensorParameterDefinition used = new SensorParameterDefinition();
        used.setId(usedId);
        used.setCode("temperature");
        used.setName("Temperature");
        used.setDataType("decimal");
        used.setUnit("C");
        SensorType manual = new SensorType(UUID.randomUUID(), "manual", "Manual", "manual", null, null);
        SensorTypeParameter orphanedManualSource = rawParameter(UUID.randomUUID(), "temperature");
        orphanedManualSource.setUsedParameter(null);
        when(sensorParameterDefinitionRepository.findById(usedId)).thenReturn(Optional.of(used));
        when(sensorTypeRepository.findByCode("manual")).thenReturn(Optional.of(manual));
        when(sensorTypeParameterRepository.findBySensorTypeIdAndCode(manual.getId(), "temperature"))
                .thenReturn(Optional.of(orphanedManualSource));
        when(sensorTypeParameterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.ensureManualSource(usedId);

        verify(sensorTypeParameterRepository).save(argThat(
                saved -> saved == orphanedManualSource && saved.getUsedParameter() == used));
    }

    @Test
    void unuse_clearsLink() {
        UUID rawId = UUID.randomUUID();
        SensorTypeParameter raw = rawParameter(rawId, "temperature");
        UUID usedId = UUID.randomUUID();
        SensorParameterDefinition used = new SensorParameterDefinition();
        used.setId(usedId);
        raw.setUsedParameter(used);
        when(sensorTypeParameterRepository.findById(rawId)).thenReturn(Optional.of(raw));
        when(sensorTypeParameterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        // Another source still feeds the same used parameter, so it must survive.
        when(sensorTypeParameterRepository.countByUsedParameterId(usedId)).thenReturn(1L);

        SensorTypeParameterDto dto = service.unuse(sensorType.getId(), rawId);

        assertThat(dto.usedParameterId()).isNull();
        verify(sensorParameterDefinitionRepository, never()).deleteById(any());
    }

    @Test
    void unuse_deletesUsedParameterLeftWithNoRemainingSources() {
        UUID rawId = UUID.randomUUID();
        SensorTypeParameter raw = rawParameter(rawId, "temperature");
        UUID usedId = UUID.randomUUID();
        SensorParameterDefinition used = new SensorParameterDefinition();
        used.setId(usedId);
        raw.setUsedParameter(used);
        when(sensorTypeParameterRepository.findById(rawId)).thenReturn(Optional.of(raw));
        when(sensorTypeParameterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sensorTypeParameterRepository.countByUsedParameterId(usedId)).thenReturn(0L);

        service.unuse(sensorType.getId(), rawId);

        verify(sensorParameterDefinitionRepository).deleteById(usedId);
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
