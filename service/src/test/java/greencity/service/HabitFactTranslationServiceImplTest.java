package greencity.service;

import static greencity.ModelUtils.getFactTranslation;
import static greencity.ModelUtils.getLanguageTranslationDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import greencity.ModelUtils;
import greencity.dto.habitfact.HabitFactDtoResponse;
import greencity.dto.habitfact.HabitFactPostDto;
import greencity.dto.habitfact.HabitFactTranslationVO;
import greencity.dto.habitfact.HabitFactVO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.user.HabitIdRequestDto;
import greencity.entity.HabitFact;
import greencity.entity.HabitFactTranslation;
import greencity.enums.FactOfDayStatus;
import greencity.repository.HabitFactTranslationRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class HabitFactTranslationServiceImplTest {
    @Mock
    HabitFactTranslationRepo habitFactTranslationRepo;
    @Mock
    HabitFactService habitFactService;
    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    HabitFactTranslationServiceImpl habitFactTranslationService;

    @Test
    void saveHabitFactAndFactTranslation() {
        HabitFact habitFact = ModelUtils.getHabitFact();
        HabitFactVO habitFactVO = ModelUtils.getHabitFactVO();
        HabitFactPostDto habitFactPostDto = new HabitFactPostDto();
        HabitIdRequestDto habitIdRequestDto = new HabitIdRequestDto();
        HabitFactDtoResponse habitFactDtoResponse = new HabitFactDtoResponse();
        habitIdRequestDto.setId(1L);
        habitFactPostDto.setHabit(habitIdRequestDto);
        habitFactPostDto.setTranslations(Collections.singletonList(ModelUtils.getLanguageTranslationDTO()));
        List<HabitFactTranslation> habitFactTranslations = Collections.singletonList(ModelUtils.getFactTranslation());
        List<HabitFactTranslationVO> habitFactTranslationVOS =
            Collections.singletonList(ModelUtils.getFactTranslationVO());
        when(habitFactService.save(habitFactPostDto)).thenReturn(habitFactVO);
        when(modelMapper.map(habitFactVO, HabitFact.class)).thenReturn(habitFact);
        when(modelMapper.map(habitFactPostDto.getTranslations(), new TypeToken<List<HabitFactTranslation>>() {
        }.getType())).thenReturn(habitFactTranslations);
        when(modelMapper.map(ModelUtils.getFactTranslationVO(), HabitFactTranslation.class))
            .thenReturn(ModelUtils.getFactTranslation());
        when(habitFactTranslationRepo.saveAll(habitFactTranslations)).thenReturn(habitFactTranslations);
        when(modelMapper.map(ModelUtils.getFactTranslation(), HabitFactTranslationVO.class))
            .thenReturn(ModelUtils.getFactTranslationVO());
        when(modelMapper.map(habitFactVO, HabitFactDtoResponse.class)).thenReturn(habitFactDtoResponse);
        habitFactVO.setTranslations(habitFactTranslationService.saveHabitFactTranslation(habitFactTranslationVOS));

        assertEquals(habitFactDtoResponse,
            habitFactTranslationService.saveHabitFactAndFactTranslation(habitFactPostDto));
    }

    @Test
    void saveHabitFactTranslation() {
        List<HabitFactTranslation> habitFactTranslations = Collections.singletonList(ModelUtils.getFactTranslation());
        List<HabitFactTranslationVO> habitFactTranslationVOS =
            Collections.singletonList(ModelUtils.getFactTranslationVO());
        when(modelMapper.map(ModelUtils.getFactTranslationVO(), HabitFactTranslation.class))
            .thenReturn(ModelUtils.getFactTranslation());
        when(habitFactTranslationRepo.saveAll(habitFactTranslations)).thenReturn(habitFactTranslations);
        when(modelMapper.map(ModelUtils.getFactTranslation(), HabitFactTranslationVO.class))
            .thenReturn(ModelUtils.getFactTranslationVO());

        assertEquals(habitFactTranslationVOS,
            habitFactTranslationService.saveHabitFactTranslation(habitFactTranslationVOS));
    }

    @Test
    void getHabitFactOfTheDay() {
        HabitFactTranslation res = getFactTranslation();
        when(habitFactTranslationRepo.findAllByFactOfDayStatusAndLanguageId(FactOfDayStatus.CURRENT, 1L))
            .thenReturn(res);
        when(modelMapper.map(res, LanguageTranslationDTO.class)).thenReturn(getLanguageTranslationDTO());
        assertEquals(getLanguageTranslationDTO(), habitFactTranslationService.getHabitFactOfTheDay(1L));
    }
}