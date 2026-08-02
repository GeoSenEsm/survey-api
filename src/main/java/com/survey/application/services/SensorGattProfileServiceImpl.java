package com.survey.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.application.dtos.GattProfileValidationDto;
import com.survey.application.dtos.SensorGattProfileDto;
import com.survey.application.dtos.SensorGattProfileWriteDto;
import com.survey.application.dtos.SensorProfileCapabilitiesDto;
import com.survey.application.dtos.SensorTypeCreateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.domain.models.SensorGattProfile;
import com.survey.domain.models.SensorType;
import com.survey.domain.models.SensorTypeSetting;
import com.survey.domain.repository.SensorGattProfileRepository;
import com.survey.domain.repository.SensorTypeRepository;
import com.survey.domain.repository.SensorTypeSettingRepository;
import com.survey.domain.repository.SensorParameterDefinitionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class SensorGattProfileServiceImpl implements SensorGattProfileService {
    private static final String DRAFT = "draft";
    private static final String PUBLISHED = "published";
    private static final String ARCHIVED = "archived";
    private static final List<String> SUPPORTED_ADAPTER_KEYS = List.of("xiaomi", "kestrel");

    private final SensorGattProfileRepository profileRepository;
    private final SensorTypeRepository sensorTypeRepository;
    private final SensorTypeSettingRepository sensorTypeSettingRepository;
    private final GattProfileValidator validator;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;
    private final SensorParameterDefinitionRepository parameterDefinitionRepository;
    private final GattProfileMobileTranslator mobileTranslator;

    public SensorGattProfileServiceImpl(
            SensorGattProfileRepository profileRepository,
            SensorTypeRepository sensorTypeRepository,
            SensorTypeSettingRepository sensorTypeSettingRepository,
            GattProfileValidator validator,
            ObjectMapper objectMapper,
            ModelMapper modelMapper,
            SensorParameterDefinitionRepository parameterDefinitionRepository,
            GattProfileMobileTranslator mobileTranslator) {
        this.profileRepository = profileRepository;
        this.sensorTypeRepository = sensorTypeRepository;
        this.sensorTypeSettingRepository = sensorTypeSettingRepository;
        this.validator = validator;
        this.objectMapper = objectMapper;
        this.modelMapper = modelMapper;
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.mobileTranslator = mobileTranslator;
    }

    @Override
    @Transactional(readOnly = true)
    public SensorProfileCapabilitiesDto capabilities() {
        return new SensorProfileCapabilitiesDto(
                List.of(1),
                "1.0.0",
                SUPPORTED_ADAPTER_KEYS,
                List.of("gatt_sequence", "ble_advertisement"),
                List.of("write", "delay", "acquire"),
                List.of("xiaomi_mibeacon_v4_v5"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorGattProfileDto> listRevisions(UUID sensorTypeId) {
        requireSensorType(sensorTypeId);
        return profileRepository.findBySensorTypeIdOrderByRevisionDesc(sensorTypeId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SensorGattProfileDto get(UUID profileId) {
        return toDto(requireProfile(profileId));
    }

    @Override
    public SensorGattProfileDto createDraft(UUID sensorTypeId, SensorGattProfileWriteDto dto) {
        SensorType sensorType = requireSensorType(sensorTypeId);
        if (!"profile".equals(sensorType.getIntegrationMode())) {
            throw new IllegalArgumentException("Only profile sensor types can have BLE profiles.");
        }
        List<SensorGattProfile> revisions = profileRepository.findBySensorTypeIdOrderByRevisionDesc(sensorTypeId);
        int nextRevision = revisions.isEmpty() ? 1 : revisions.get(0).getRevision() + 1;

        SensorGattProfile profile = new SensorGattProfile();
        profile.setSensorType(sensorType);
        profile.setRevision(nextRevision);
        profile.setStatus(DRAFT);
        profile.setReadOnly(false);
        applySpec(profile, dto);
        return toDto(profileRepository.save(profile));
    }

    @Override
    public SensorGattProfileDto updateDraft(UUID profileId, SensorGattProfileWriteDto dto) {
        SensorGattProfile profile = requireMutableDraft(profileId);
        applySpec(profile, dto);
        return toDto(profileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public GattProfileValidationDto validate(UUID profileId) {
        return validator.validate(parse(requireProfile(profileId).getSpecJson()));
    }

    @Override
    public SensorGattProfileDto publish(UUID profileId) {
        SensorGattProfile profile = requireMutableDraft(profileId);
        GattProfileValidationDto validation = validator.validate(parse(profile.getSpecJson()));
        requireValid(validation);
        requireMappedParameters(parse(profile.getSpecJson()));
        archivePublished(profile.getSensorType().getId());
        OffsetDateTime now = now();
        profile.setStatus(PUBLISHED);
        profile.setPublishedAt(now);
        profile.setUpdatedAt(now);
        profile.setSpecHash(validation.canonicalHash());
        return toDto(profileRepository.save(profile));
    }

    @Override
    public SensorGattProfileDto rollback(UUID sensorTypeId, int revision) {
        SensorGattProfile source = profileRepository.findBySensorTypeIdAndRevision(sensorTypeId, revision)
                .orElseThrow(() -> new NoSuchElementException("Profile revision " + revision + " was not found."));
        GattProfileValidationDto validation = validator.validate(parse(source.getSpecJson()));
        requireValid(validation);
        requireMappedParameters(parse(source.getSpecJson()));

        List<SensorGattProfile> revisions = profileRepository.findBySensorTypeIdOrderByRevisionDesc(sensorTypeId);
        int nextRevision = revisions.get(0).getRevision() + 1;
        archivePublished(sensorTypeId);

        OffsetDateTime now = now();
        SensorGattProfile rollback = new SensorGattProfile();
        rollback.setSensorType(source.getSensorType());
        rollback.setRevision(nextRevision);
        rollback.setStatus(PUBLISHED);
        rollback.setSchemaVersion(source.getSchemaVersion());
        rollback.setSpecJson(source.getSpecJson());
        rollback.setSpecHash(source.getSpecHash());
        rollback.setMinEngineVersion(source.getMinEngineVersion());
        rollback.setReadOnly(false);
        rollback.setUpdatedAt(now);
        rollback.setPublishedAt(now);
        return toDto(profileRepository.save(rollback));
    }

    @Override
    public SensorTypeDtoOut createSensorType(SensorTypeCreateDto dto) {
        if (sensorTypeRepository.findByCode(dto.code()).isPresent()) {
            throw new IllegalArgumentException("Sensor type code already exists: " + dto.code());
        }
        if (!"native".equals(dto.integrationMode()) && dto.adapterKey() != null && !dto.adapterKey().isBlank()) {
            throw new IllegalArgumentException("adapterKey is only valid for native integrations.");
        }
        if ("native".equals(dto.integrationMode()) && (dto.adapterKey() == null || dto.adapterKey().isBlank())) {
            throw new IllegalArgumentException("Native integrations require adapterKey.");
        }
        if ("native".equals(dto.integrationMode()) && !SUPPORTED_ADAPTER_KEYS.contains(dto.adapterKey())) {
            throw new IllegalArgumentException("Unsupported native adapterKey: " + dto.adapterKey());
        }

        SensorType sensorType = new SensorType();
        sensorType.setId(UUID.randomUUID());
        sensorType.setCode(dto.code());
        sensorType.setName(dto.name());
        sensorType.setIntegrationMode(dto.integrationMode());
        sensorType.setAdapterKey(emptyToNull(dto.adapterKey()));
        SensorType saved = sensorTypeRepository.save(sensorType);

        SensorTypeSetting setting = new SensorTypeSetting();
        setting.setSensorType(saved);
        setting.setEnabled(false);
        setting.setConnectionTimeoutSeconds(30);
        setting.setDisplayOrder(99);
        sensorTypeSettingRepository.save(setting);
        return modelMapper.map(saved, SensorTypeDtoOut.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorGattProfileDto> getPublishedProfiles(Set<UUID> sensorTypeIds) {
        if (sensorTypeIds.isEmpty()) {
            return List.of();
        }
        return profileRepository.findBySensorTypeIdInAndStatus(sensorTypeIds, PUBLISHED).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JsonNode> getPublishedProfilesForMobile(Set<UUID> sensorTypeIds) {
        if (sensorTypeIds.isEmpty()) {
            return List.of();
        }
        return profileRepository.findBySensorTypeIdInAndStatus(sensorTypeIds, PUBLISHED).stream()
                .map(entity -> mobileTranslator.translate(entity).spec())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorTypeDtoOut> listProfileSensorTypes() {
        return sensorTypeRepository.findAll().stream()
                .filter(type -> "profile".equals(type.getIntegrationMode()))
                .map(type -> modelMapper.map(type, SensorTypeDtoOut.class))
                .toList();
    }

    private void applySpec(SensorGattProfile profile, SensorGattProfileWriteDto dto) {
        GattProfileValidationDto validation = validator.validate(dto.spec());
        profile.setSchemaVersion(1);
        profile.setSpecJson(validator.canonicalJson(dto.spec()));
        profile.setSpecHash(validation.canonicalHash());
        profile.setMinEngineVersion(dto.minEngineVersion());
        profile.setUpdatedAt(now());
    }

    private void requireMappedParameters(JsonNode spec) {
        Set<String> parameters = new java.util.HashSet<>();
        spec.path("operations").forEach(operation ->
                operation.path("decoders").forEach(decoder ->
                        parameters.add(decoder.path("parameter").asText())));
        spec.path("advertisement").path("objects").forEach(object ->
                parameters.add(object.path("parameter").asText()));
        parameters.forEach(parameter -> parameterDefinitionRepository.findByCode(parameter)
                .filter(definition -> definition.isActive())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Profile parameter is not an active sensor parameter: " + parameter)));
    }

    private void archivePublished(UUID sensorTypeId) {
        profileRepository.findBySensorTypeIdAndStatus(sensorTypeId, PUBLISHED).ifPresent(current -> {
            current.setStatus(ARCHIVED);
            current.setPublishedAt(null);
            current.setUpdatedAt(now());
            profileRepository.saveAndFlush(current);
        });
    }

    private SensorGattProfile requireMutableDraft(UUID id) {
        SensorGattProfile profile = requireProfile(id);
        if (!DRAFT.equals(profile.getStatus()) || profile.isReadOnly()) {
            throw new IllegalStateException("Only writable drafts can be changed or published.");
        }
        return profile;
    }

    private SensorGattProfile requireProfile(UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("GATT profile was not found: " + id));
    }

    private SensorType requireSensorType(UUID id) {
        return sensorTypeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sensor type was not found: " + id));
    }

    private SensorGattProfileDto toDto(SensorGattProfile profile) {
        return new SensorGattProfileDto(
                profile.getId(),
                profile.getSensorType().getId(),
                profile.getSensorType().getCode(),
                profile.getRevision(),
                profile.getStatus(),
                profile.getSchemaVersion(),
                parse(profile.getSpecJson()),
                profile.getSpecHash(),
                profile.getMinEngineVersion(),
                profile.isReadOnly(),
                profile.getCreatedAt(),
                profile.getUpdatedAt(),
                profile.getPublishedAt(),
                profile.getRowVersion());
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored GATT profile JSON is invalid.", exception);
        }
    }

    private static void requireValid(GattProfileValidationDto validation) {
        if (!validation.valid()) {
            throw new IllegalArgumentException("Invalid GATT profile: " + String.join("; ", validation.errors()));
        }
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
