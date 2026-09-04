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
    private final SensorTypeParameterService sensorTypeParameterService;
    private final RespondentSensorAssignmentRepository respondentSensorAssignmentRepository;
    private final SensorDataRepository sensorDataRepository;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final SensorGattProfileService sensorGattProfileService;
    private final StorageService storageService;
    private final InitialSurveyService initialSurveyService;
    private final SensorParameterDefinitionValidator parameterDefinitionValidator;

    public SurveySettingsServiceImpl(
            SurveySettingsRepository surveySettingsRepository,
            SurveySensorSettingsRepository surveySensorSettingsRepository,
            SensorTypeSettingRepository sensorTypeSettingRepository,
            SensorParameterDefinitionRepository sensorParameterDefinitionRepository,
            SensorTypeRepository sensorTypeRepository,
            SensorTypeParameterService sensorTypeParameterService,
            RespondentSensorAssignmentRepository respondentSensorAssignmentRepository,
            SensorDataRepository sensorDataRepository,
            ClaimsPrincipalService claimsPrincipalService,
            SensorGattProfileService sensorGattProfileService,
            StorageService storageService,
            InitialSurveyService initialSurveyService,
            SensorParameterDefinitionValidator parameterDefinitionValidator) {
        this.surveySettingsRepository = surveySettingsRepository;
        this.surveySensorSettingsRepository = surveySensorSettingsRepository;
        this.sensorTypeSettingRepository = sensorTypeSettingRepository;
        this.sensorParameterDefinitionRepository = sensorParameterDefinitionRepository;
        this.sensorTypeRepository = sensorTypeRepository;
        this.sensorTypeParameterService = sensorTypeParameterService;
        this.respondentSensorAssignmentRepository = respondentSensorAssignmentRepository;
        this.sensorDataRepository = sensorDataRepository;
        this.claimsPrincipalService = claimsPrincipalService;
        this.sensorGattProfileService = sensorGattProfileService;
        this.storageService = storageService;
        this.initialSurveyService = initialSurveyService;
        this.parameterDefinitionValidator = parameterDefinitionValidator;
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
                        .toList());
    }

    @Override
    public SurveySensorDataSettingsDto updateSensorDataSettings(SurveySensorDataSettingsWriteDto dto) {
        initialSurveyService.requireNotPublished();
        requireNoCollectedSensorData();
        rejectDestructiveEmptySensorTypesSetup(dto);

        SurveySensorSettings settings = getOrCreateSensorSettings();
        settings.setMode(dto.mode());
        surveySensorSettingsRepository.save(settings);

        Map<String, SensorType> sensorTypes = sensorTypeRepository.findAll().stream()
                .collect(Collectors.toMap(SensorType::getCode, Function.identity()));
        List<String> previousSensorTypeCodes = sensorTypeSettingRepository.findAll().stream()
                .map(setting -> setting.getSensorType().getCode())
                .toList();

        replaceSensorTypeSettings(dto.sensorTypes(), sensorTypes);
        detachDisabledSensorTypeSources(dto.sensorTypes(), previousSensorTypeCodes, sensorTypes);

        return getSensorDataSettings();
    }

    @Override
    public SensorParameterDefinitionDto createSensorParameterDefinition(SensorParameterDefinitionCreateDto dto) {
        initialSurveyService.requireNotPublished();
        if (sensorParameterDefinitionRepository.findByCode(dto.code()).isPresent()) {
            throw new IllegalArgumentException("Sensor parameter code already exists: " + dto.code());
        }
        parameterDefinitionValidator.assertNameUnitAvailable(dto.name(), dto.unit(), null);

        SensorParameterDefinition definition = new SensorParameterDefinition();
        definition.setCode(dto.code());
        definition.setName(dto.name());
        definition.setDataType(dto.dataType());
        definition.setUnit(dto.unit());
        definition.setDisplayOrder((int) sensorParameterDefinitionRepository.count());
        SensorParameterDefinition saved = sensorParameterDefinitionRepository.save(definition);
        sensorTypeParameterService.ensureManualSource(saved.getId());
        // Re-fetch so the response includes the manual source just wired above: `saved`'s
        // in-memory `rawParameters` collection was loaded (empty) before that row existed.
        return toDto(sensorParameterDefinitionRepository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public SensorParameterDefinitionDto updateSensorParameterDefinition(UUID id, SensorParameterDefinitionEditDto dto) {
        initialSurveyService.requireNotPublished();
        SensorParameterDefinition definition = sensorParameterDefinitionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sensor parameter was not found: " + id));
        parameterDefinitionValidator.assertNameUnitAvailable(dto.name(), dto.unit(), id);

        definition.setName(dto.name());
        definition.setDataType(dto.dataType());
        definition.setUnit(dto.unit());
        definition.setDisplayOrder(dto.displayOrder());
        return toDto(sensorParameterDefinitionRepository.save(definition));
    }

    /**
     * Hard-deletes a used parameter — there is no soft-hide flag, a parameter is either on the
     * list or gone. Wired raw sources are automatically unwired by
     * {@code sensor_type_parameter.used_parameter_id}'s {@code ON DELETE SET NULL}. Deliberately
     * does not pre-check for existing {@code sensor_data_parameter_value} rows: that FK has no
     * cascade, so the delete fails fast with a {@link org.springframework.dao.DataIntegrityViolationException}
     * (already mapped to 409 by {@code GlobalExceptionHandler}) rather than silently destroying
     * collected sensor readings.
     */
    @Override
    public void deleteSensorParameterDefinition(UUID id) {
        initialSurveyService.requireNotPublished();
        SensorParameterDefinition definition = sensorParameterDefinitionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sensor parameter was not found: " + id));
        sensorParameterDefinitionRepository.delete(definition);
    }

    @Override
    @Transactional(readOnly = true)
    public MobileSensorSetupDto getMobileSensorSetup() {
        IdentityUser respondent = claimsPrincipalService.findIdentityUser();
        SurveySensorDataSettingsDto settings = getSensorDataSettings();
        List<RespondentSensorAssignment> assignmentEntities = respondentSensorAssignmentRepository
                .findByRespondentId(respondent.getId());
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
                sensorGattProfileService.getPublishedProfilesForMobile(assignedProfileTypes));
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
        sensorTypeSettingRepository.flush();
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

    /**
     * On top of {@link InitialSurveyService#requireNotPublished()}: disabling a sensor type here
     * also detaches (and can delete) its parameter links, so once any sensor reading has actually
     * been collected this whole endpoint locks rather than risk orphaning or destroying that data.
     */
    private void requireNoCollectedSensorData() {
        if (sensorDataRepository.count() > 0) {
            throw new IllegalStateException(
                    "Sensor data settings cannot be changed: sensor data has already been collected.");
        }
    }

    /**
     * A sensor type that isn't left enabled can no longer feed any parameter, so every raw source
     * it has wired is unlinked exactly like the manual "unuse" action
     * ({@link SensorTypeParameterService#unuse}) — the raw catalog row itself is left alone,
     * re-enabling the integration requires re-wiring it by hand. This covers both a type
     * explicitly sent with {@code enabled=false} and a type simply omitted from {@code dtos}:
     * {@link #replaceSensorTypeSettings} deletes every existing {@code sensor_type_setting} row
     * and only recreates the ones present in {@code dtos}, so a previously configured type that's
     * omitted ends up just as unconfigured as an explicitly disabled one and must be detached the
     * same way — hence {@code previousSensorTypeCodes}, captured before that delete. Deliberately
     * scoped to previously-configured-or-submitted codes only (not every {@code SensorType} row),
     * since {@code manual}/{@code none} never get a {@code sensor_type_setting} row or appear in
     * {@code dtos} — iterating all sensor types would treat them as "disabled" on every save and
     * strip every manual fallback source. Reusing {@code unuse} (rather than unlinking directly
     * here) is what makes a used parameter left with zero remaining sources get deleted: that
     * cleanup lives in one place so it fires no matter which path removed the last source.
     */
    private void detachDisabledSensorTypeSources(
            List<SensorTypeSettingDto> dtos, List<String> previousSensorTypeCodes, Map<String, SensorType> sensorTypes) {
        if (dtos == null) {
            return;
        }
        Set<String> stillEnabledCodes = dtos.stream()
                .filter(SensorTypeSettingDto::enabled)
                .map(SensorTypeSettingDto::sensorTypeCode)
                .collect(Collectors.toSet());
        Set<String> candidateCodes = new HashSet<>(previousSensorTypeCodes);
        dtos.forEach(dto -> candidateCodes.add(dto.sensorTypeCode()));
        for (String code : candidateCodes) {
            if (stillEnabledCodes.contains(code)) {
                continue;
            }
            SensorType sensorType = getSensorType(sensorTypes, code);
            sensorTypeParameterService.list(sensorType.getId()).stream()
                    .filter(raw -> raw.usedParameterId() != null)
                    .forEach(raw -> sensorTypeParameterService.unuse(sensorType.getId(), raw.id()));
        }
    }

    private void rejectDestructiveEmptySensorTypesSetup(SurveySensorDataSettingsWriteDto dto) {
        if (dto.sensorTypes() != null
                && dto.sensorTypes().isEmpty()
                && !sensorTypeSettingRepository.findAll().isEmpty()) {
            throw new IllegalArgumentException(
                    "sensorTypes must include the current setup; use individual enabled flags instead of an empty list.");
        }
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
                definition.getDisplayOrder(),
                definition.getRawParameters().stream()
                        .map(source -> new SensorParameterSourceDto(
                                source.getId(),
                                source.getSensorType().getCode(),
                                source.getCode()))
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
                sensorMac != null ? sensorMac.getSensorMac() : null);
    }

    private static SurveySettingsDto toDto(SurveySettings settings) {
        return new SurveySettingsDto(
                settings.isShowSendingPolicyCalendar(),
                settings.getCsvColumnSeparator(),
                settings.getCsvDecimalSeparator(),
                settings.getLogoPath());
    }
}
