package com.survey.application.services;

import com.survey.application.dtos.*;
import com.survey.domain.models.*;
import com.survey.domain.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class SurveySettingsServiceImpl implements SurveySettingsService {
    private static final int SINGLETON_ID = 1;
    private static final String DEFAULT_SENSOR_MODE = "no_sensor_data";
    private static final String DEFAULT_COLUMN_SEPARATOR = ",";
    private static final String DEFAULT_DECIMAL_SEPARATOR = ".";

    private final SurveySettingsRepository surveySettingsRepository;
    private final SurveySensorSettingsRepository surveySensorSettingsRepository;
    private final SensorTypeSettingRepository sensorTypeSettingRepository;
    private final SensorParameterDefinitionRepository sensorParameterDefinitionRepository;
    private final SensorTypeRepository sensorTypeRepository;
    private final RespondentSensorAssignmentRepository respondentSensorAssignmentRepository;
    private final IdentityUserRepository identityUserRepository;
    private final SensorMacRepository sensorMacRepository;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final SensorGattProfileService sensorGattProfileService;
    private final SensorDeviceSecretService sensorDeviceSecretService;
    private final StorageService storageService;
    private final InitialSurveyService initialSurveyService;

    public SurveySettingsServiceImpl(
            SurveySettingsRepository surveySettingsRepository,
            SurveySensorSettingsRepository surveySensorSettingsRepository,
            SensorTypeSettingRepository sensorTypeSettingRepository,
            SensorParameterDefinitionRepository sensorParameterDefinitionRepository,
            SensorTypeRepository sensorTypeRepository,
            RespondentSensorAssignmentRepository respondentSensorAssignmentRepository,
            IdentityUserRepository identityUserRepository,
            SensorMacRepository sensorMacRepository,
            ClaimsPrincipalService claimsPrincipalService,
            SensorGattProfileService sensorGattProfileService,
            SensorDeviceSecretService sensorDeviceSecretService,
            StorageService storageService,
            InitialSurveyService initialSurveyService) {
        this.surveySettingsRepository = surveySettingsRepository;
        this.surveySensorSettingsRepository = surveySensorSettingsRepository;
        this.sensorTypeSettingRepository = sensorTypeSettingRepository;
        this.sensorParameterDefinitionRepository = sensorParameterDefinitionRepository;
        this.sensorTypeRepository = sensorTypeRepository;
        this.respondentSensorAssignmentRepository = respondentSensorAssignmentRepository;
        this.identityUserRepository = identityUserRepository;
        this.sensorMacRepository = sensorMacRepository;
        this.claimsPrincipalService = claimsPrincipalService;
        this.sensorGattProfileService = sensorGattProfileService;
        this.sensorDeviceSecretService = sensorDeviceSecretService;
        this.storageService = storageService;
        this.initialSurveyService = initialSurveyService;
    }

    @Override
    public SurveySettingsDto getSettings() {
        return toDto(getOrCreate());
    }

    @Override
    public SurveySettingsDto updateSettings(SurveySettingsDto dto) {
        if (dto.csvColumnSeparator().equals(dto.csvDecimalSeparator())) {
            throw new IllegalArgumentException(
                    "csvColumnSeparator and csvDecimalSeparator must be different.");
        }

        SurveySettings settings = getOrCreate();
        settings.setShowSendingPolicyCalendar(dto.showSendingPolicyCalendar());
        settings.setCsvColumnSeparator(dto.csvColumnSeparator());
        settings.setCsvDecimalSeparator(dto.csvDecimalSeparator());
        return toDto(surveySettingsRepository.save(settings));
    }

    @Override
    public SurveySettingsDto uploadLogo(MultipartFile file) throws IOException {
        SurveySettings settings = getOrCreate();
        String previousLogoPath = settings.getLogoPath();
        String logoPath = storageService.storeSurveySettingsLogo(file);
        if (previousLogoPath != null && !previousLogoPath.equals(logoPath)) {
            storageService.deleteFile(previousLogoPath);
        }
        settings.setLogoPath(logoPath);
        return toDto(surveySettingsRepository.save(settings));
    }

    @Override
    public SurveySettingsDto deleteLogo() {
        SurveySettings settings = getOrCreate();
        if (settings.getLogoPath() != null) {
            storageService.deleteFile(settings.getLogoPath());
            settings.setLogoPath(null);
        }
        return toDto(surveySettingsRepository.save(settings));
    }

    @Override
    @Transactional(readOnly = true)
    public SurveySensorDataSettingsDto getSensorDataSettings() {
        SurveySensorSettings settings = getOrCreateSensorSettings();
        return new SurveySensorDataSettingsDto(
                settings.getMode(),
                sensorTypeSettingRepository.findAllOrdered().stream()
                        .map(this::toDto)
                        .toList(),
                sensorParameterDefinitionRepository.findAllOrderedWithSources().stream()
                        .map(this::toDto)
                        .toList(),
                respondentSensorAssignmentRepository.findAllOrdered().stream()
                        .map(this::toDto)
                        .toList());
    }

    @Override
    public SurveySensorDataSettingsDto updateSensorDataSettings(SurveySensorDataSettingsWriteDto dto) {
        if (initialSurveyService.isPublished()) {
            throw new IllegalStateException(
                    "Sensor data setup is locked: the initial survey has already been published.");
        }
        rejectDestructiveEmptySensorSetup(dto);

        SurveySensorSettings settings = getOrCreateSensorSettings();
        settings.setMode(dto.mode());
        surveySensorSettingsRepository.save(settings);

        Map<String, SensorType> sensorTypes = sensorTypeRepository.findAll().stream()
                .collect(Collectors.toMap(SensorType::getCode, Function.identity()));

        replaceSensorTypeSettings(dto.sensorTypes(), sensorTypes);
        upsertParameterDefinitions(dto.parameters(), sensorTypes);

        return getSensorDataSettings();
    }

    /**
     * Deliberately not guarded by {@code requireSensorSetupUnlocked}: see
     * {@link SurveySensorDataSettingsWriteDto}'s Javadoc for why respondent sensor assignments
     * stay editable after the initial survey is published.
     */
    @Override
    public SurveySensorDataSettingsDto updateAssignments(List<RespondentSensorAssignmentDto> assignments) {
        Map<String, SensorType> sensorTypes = sensorTypeRepository.findAll().stream()
                .collect(Collectors.toMap(SensorType::getCode, Function.identity()));
        replaceAssignments(assignments, sensorTypes);
        return getSensorDataSettings();
    }

    @Override
    @Transactional(readOnly = true)
    public MobileSensorSetupDto getMobileSensorSetup() {
        IdentityUser respondent = claimsPrincipalService.findIdentityUser();
        SurveySensorDataSettingsDto settings = getSensorDataSettings();
        List<RespondentSensorAssignment> assignmentEntities = respondentSensorAssignmentRepository
                .findByRespondentIdAndEnabledTrueOrderByPriorityOrder(respondent.getId());
        List<RespondentSensorAssignmentDto> assignments = assignmentEntities.stream()
                .map(this::toDto)
                .toList();
        Set<UUID> assignedProfileTypes = assignmentEntities.stream()
                .map(RespondentSensorAssignment::getSensorType)
                .filter(type -> "profile".equals(type.getIntegrationMode()))
                .map(SensorType::getId)
                .collect(Collectors.toSet());

        return new MobileSensorSetupDto(
                settings.mode(),
                settings.sensorTypes(),
                settings.parameters(),
                assignments,
                sensorGattProfileService.getPublishedProfilesForMobile(assignedProfileTypes),
                sensorDeviceSecretService.getForRespondent(respondent.getId()));
    }

    private SurveySettings getOrCreate() {
        return surveySettingsRepository.findById(SINGLETON_ID)
                .orElseGet(() -> surveySettingsRepository.save(
                        new SurveySettings(
                                SINGLETON_ID,
                                true,
                                DEFAULT_COLUMN_SEPARATOR,
                                DEFAULT_DECIMAL_SEPARATOR,
                                null)));
    }

    private SurveySensorSettings getOrCreateSensorSettings() {
        return surveySensorSettingsRepository.findById(SINGLETON_ID)
                .orElseGet(() -> surveySensorSettingsRepository.save(
                        new SurveySensorSettings(SINGLETON_ID, DEFAULT_SENSOR_MODE)));
    }

    private void replaceSensorTypeSettings(List<SensorTypeSettingDto> dtos, Map<String, SensorType> sensorTypes) {
        if (dtos == null) {
            return;
        }
        sensorTypeSettingRepository.deleteAll();
        if (dtos.isEmpty()) {
            return;
        }
        List<SensorTypeSetting> settings = dtos.stream()
                .map(dto -> {
                    SensorType sensorType = getSensorType(sensorTypes, dto.sensorTypeCode());
                    SensorTypeSetting setting = new SensorTypeSetting();
                    setting.setSensorType(sensorType);
                    setting.setEnabled(dto.enabled());
                    setting.setConnectionTimeoutSeconds(dto.connectionTimeoutSeconds());
                    setting.setDisplayOrder(dto.displayOrder());
                    return setting;
                })
                .toList();
        sensorTypeSettingRepository.saveAll(settings);
    }

    private void upsertParameterDefinitions(List<SensorParameterDefinitionDto> dtos, Map<String, SensorType> sensorTypes) {
        validateUniqueNameUnitPairs(dtos);
        List<SensorParameterDefinition> existingDefinitions = sensorParameterDefinitionRepository.findAll();

        Set<String> activeCodes = dtos.stream()
                .map(SensorParameterDefinitionDto::code)
                .collect(Collectors.toSet());

        for (SensorParameterDefinition existing : existingDefinitions) {
            if (!activeCodes.contains(existing.getCode())) {
                existing.setActive(false);
                sensorParameterDefinitionRepository.save(existing);
            }
        }

        for (SensorParameterDefinitionDto dto : dtos) {
            SensorParameterDefinition definition = sensorParameterDefinitionRepository.findByCode(dto.code())
                    .orElseGet(SensorParameterDefinition::new);
            definition.setCode(dto.code());
            definition.setName(dto.name());
            definition.setDataType(dto.dataType());
            definition.setUnit(dto.unit());
            definition.setRequired(dto.required());
            definition.setActive(dto.active());
            definition.setDisplayOrder(dto.displayOrder());
            definition.getSources().clear();
            dto.sources().forEach(sourceDto -> {
                SensorParameterSource source = new SensorParameterSource();
                source.setParameterDefinition(definition);
                source.setSensorType(getSensorType(sensorTypes, sourceDto.sensorTypeCode()));
                source.setPriorityOrder(sourceDto.priorityOrder());
                definition.getSources().add(source);
            });
            sensorParameterDefinitionRepository.save(definition);
        }
    }

    private void rejectDestructiveEmptySensorSetup(SurveySensorDataSettingsWriteDto dto) {
        if (dto.sensorTypes() != null
                && dto.sensorTypes().isEmpty()
                && !sensorTypeSettingRepository.findAll().isEmpty()) {
            throw new IllegalArgumentException(
                    "sensorTypes must include the current setup; use individual enabled flags instead of an empty list.");
        }
        if (dto.parameters() != null
                && dto.parameters().isEmpty()
                && sensorParameterDefinitionRepository.findAllOrderedWithSources().stream()
                        .anyMatch(definition -> definition.isActive() && !definition.getSources().isEmpty())) {
            throw new IllegalArgumentException(
                    "parameters must include the current setup; disable individual parameters instead of an empty list.");
        }
    }

    /**
     * A parameter's identity is the (name, unit) pair, not the name alone: two parameters that
     * measure different things in different units (e.g. Flower Care's lux "Light" vs. a plain
     * on/off "Light" flag) must be distinct definitions, mirroring
     * {@code UQ_sensor_parameter_definition_name_unit}.
     */
    private void validateUniqueNameUnitPairs(List<SensorParameterDefinitionDto> dtos) {
        Set<String> seen = new HashSet<>();
        for (SensorParameterDefinitionDto dto : dtos) {
            String key = normalizeForUniqueness(dto.name()) + "\u0000" + normalizeForUniqueness(dto.unit());
            if (!seen.add(key)) {
                throw new IllegalArgumentException(
                        "Parameter name and unit must be unique together: multiple parameters named '"
                                + dto.name() + "' share unit '" + Objects.toString(dto.unit(), "") + "'.");
            }
        }
    }

    private String normalizeForUniqueness(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private void replaceAssignments(List<RespondentSensorAssignmentDto> dtos, Map<String, SensorType> sensorTypes) {
        if (dtos == null) {
            return;
        }
        respondentSensorAssignmentRepository.deleteAll();
        respondentSensorAssignmentRepository.flush();
        sensorMacRepository.clearRespondentAssignments();
        List<RespondentSensorAssignment> assignments = dtos.stream()
                .map(dto -> {
                    IdentityUser respondent = identityUserRepository.findById(dto.respondentId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid respondent ID: " + dto.respondentId()));
                    if (!"Respondent".equals(respondent.getRole())) {
                        throw new IllegalArgumentException("Assignment user is not a respondent: " + dto.respondentId());
                    }
                    SensorType sensorType = getSensorType(sensorTypes, dto.sensorTypeCode());
                    SensorMac sensorMac = dto.sensorMacId() == null
                            ? null
                            : sensorMacRepository.findById(dto.sensorMacId())
                                    .orElseThrow(() -> new IllegalArgumentException("Invalid sensor MAC ID: " + dto.sensorMacId()));
                    if (sensorMac != null && !sensorMac.getSensorTypeId().equals(sensorType.getId())) {
                        throw new IllegalArgumentException("Assigned sensor does not match sensor type " + dto.sensorTypeCode());
                    }
                    if (sensorMac != null) {
                        sensorMac.setRespondentId(respondent.getId());
                        sensorMacRepository.save(sensorMac);
                    }
                    RespondentSensorAssignment assignment = new RespondentSensorAssignment();
                    assignment.setRespondent(respondent);
                    assignment.setSensorType(sensorType);
                    assignment.setSensorMac(sensorMac);
                    assignment.setEnabled(dto.enabled());
                    assignment.setPriorityOrder(dto.priorityOrder());
                    return assignment;
                })
                .toList();
        respondentSensorAssignmentRepository.saveAll(assignments);
    }

    private SensorType getSensorType(Map<String, SensorType> sensorTypes, String code) {
        SensorType sensorType = sensorTypes.get(code);
        if (sensorType == null) {
            throw new IllegalArgumentException("Unknown sensor type code: " + code);
        }
        return sensorType;
    }

    private SensorTypeSettingDto toDto(SensorTypeSetting setting) {
        return new SensorTypeSettingDto(
                setting.getId(),
                setting.getSensorType().getCode(),
                setting.getSensorType().getName(),
                setting.getSensorType().getIntegrationMode(),
                setting.getSensorType().getAdapterKey(),
                setting.isEnabled(),
                setting.getConnectionTimeoutSeconds(),
                setting.getDisplayOrder());
    }

    private SensorParameterDefinitionDto toDto(SensorParameterDefinition definition) {
        return new SensorParameterDefinitionDto(
                definition.getId(),
                definition.getCode(),
                definition.getName(),
                definition.getDataType(),
                definition.getUnit(),
                definition.isRequired(),
                definition.isActive(),
                definition.getDisplayOrder(),
                definition.getSources().stream()
                        .sorted(Comparator.comparingInt(SensorParameterSource::getPriorityOrder))
                        .map(source -> new SensorParameterSourceDto(
                                source.getId(),
                                source.getSensorType().getCode(),
                                source.getPriorityOrder()))
                        .toList());
    }

    private RespondentSensorAssignmentDto toDto(RespondentSensorAssignment assignment) {
        SensorMac sensorMac = assignment.getSensorMac();
        return new RespondentSensorAssignmentDto(
                assignment.getId(),
                assignment.getRespondent().getId(),
                assignment.getRespondent().getUsername(),
                assignment.getSensorType().getCode(),
                assignment.getSensorType().getName(),
                sensorMac != null ? sensorMac.getId() : null,
                sensorMac != null ? sensorMac.getSensorId() : null,
                sensorMac != null ? sensorMac.getSensorMac() : null,
                assignment.isEnabled(),
                assignment.getPriorityOrder());
    }

    private static SurveySettingsDto toDto(SurveySettings settings) {
        return new SurveySettingsDto(
                settings.isShowSendingPolicyCalendar(),
                settings.getCsvColumnSeparator(),
                settings.getCsvDecimalSeparator(),
                settings.getLogoPath());
    }
}
