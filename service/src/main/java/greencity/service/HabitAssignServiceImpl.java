package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habit.HabitAssignStatDto;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habit.HabitVO;
import greencity.dto.user.UserVO;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.User;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserAlreadyHasHabitAssignedException;
import greencity.exception.exceptions.UserAlreadyHasMaxNumberOfActiveHabitAssigns;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitRepo;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link HabitAssignService}.
 */
@Service
@AllArgsConstructor
public class HabitAssignServiceImpl implements HabitAssignService {
    private final HabitAssignRepo habitAssignRepo;
    private final HabitRepo habitRepo;
    private final HabitStatisticService habitStatisticService;
    private final HabitStatusService habitStatusService;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitAssignDto getById(Long habitAssignId) {
        return modelMapper.map(habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId)),
            HabitAssignDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public HabitAssignDto assignHabitForUser(Long habitId, UserVO userVO) {
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
        User user = modelMapper.map(userVO, User.class);

        if (habitAssignRepo.findByHabitIdAndUserIdAndSuspendedFalse(habitId, user.getId()).isPresent()) {
            throw new UserAlreadyHasHabitAssignedException(
                ErrorMessage.USER_ALREADY_HAS_ASSIGNED_HABIT + habitId);
        }

        if (habitAssignRepo.countHabitAssignsByUserIdAndSuspendedFalseAndAcquiredFalse(
            user.getId()) >= AppConstant.MAX_NUMBER_OF_HABIT_ASSIGNS_FOR_USER) {
            throw new UserAlreadyHasMaxNumberOfActiveHabitAssigns(
                ErrorMessage.USER_ALREADY_HAS_MAX_NUMBER_OF_HABIT_ASSIGNS
                    + AppConstant.MAX_NUMBER_OF_HABIT_ASSIGNS_FOR_USER);
        }

        if (habitAssignRepo.findByHabitIdAndUserIdAndCreateDate(
            habitId, user.getId(), ZonedDateTime.now()).isPresent()) {
            throw new UserAlreadyHasHabitAssignedException(
                ErrorMessage.USER_SUSPENDED_ASSIGNED_HABIT_FOR_CURRENT_DAY_ALREADY + habitId);
        } else {
            HabitAssign habitAssign = habitAssignRepo.save(
                HabitAssign.builder()
                    .habit(habit)
                    .acquired(false)
                    .createDate(ZonedDateTime.now())
                    .suspended(false)
                    .user(user)
                    .build());

            habitStatusService.saveStatusByHabitAssign(modelMapper.map(habitAssign, HabitAssignVO.class));
            return modelMapper.map(habitAssign, HabitAssignDto.class);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitAssignDto findActiveHabitAssignByUserIdAndHabitId(Long userId, Long habitId) {
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
        return modelMapper.map(habitAssignRepo.findByHabitIdAndUserIdAndSuspendedFalse(habit.getId(), userId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_SUCH_USER_ID_AND_HABIT_ID
                + userId + ", " + habitId)),
            HabitAssignDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> getAllHabitAssignsByUserIdAndAcquiredStatus(Long userId, Boolean acquired) {
        return modelMapper.map(habitAssignRepo.findAllByUserIdAndAcquiredAndSuspendedFalse(userId, acquired),
            new TypeToken<List<HabitAssignDto>>() {
            }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> getAllHabitAssignsByHabitIdAndAcquiredStatus(Long habitId, Boolean acquired) {
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
        return modelMapper.map(habitAssignRepo.findAllByHabitIdAndAcquiredAndSuspendedFalse(habit.getId(), acquired),
            new TypeToken<List<HabitAssignDto>>() {
            }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public HabitAssignDto updateStatusByHabitIdAndUserId(Long habitId, Long userId, HabitAssignStatDto dto) {
        HabitAssign updatable = habitAssignRepo.findByHabitIdAndUserIdAndSuspendedFalse(habitId, userId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_SUCH_USER_ID_AND_HABIT_ID + habitId));

        enhanceStatusesWithDto(dto, updatable);

        return modelMapper.map(habitAssignRepo.save(updatable), HabitAssignDto.class);
    }

    /**
     * Method updates {@link HabitAssign} with {@link HabitAssignStatDto} fields.
     *
     * @param dto       {@link HabitAssignStatDto} instance.
     * @param updatable {@link HabitAssign} instance.
     */
    private void enhanceStatusesWithDto(HabitAssignStatDto dto, HabitAssign updatable) {
        updatable.setAcquired(dto.getAcquired());
        updatable.setSuspended(dto.getSuspended());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void deleteAllHabitAssignsByHabit(HabitVO habit) {
        habitAssignRepo.findAllByHabitId(habit.getId())
            .forEach(habitAssign -> {
                HabitAssignVO habitAssignVO = modelMapper.map(habitAssign, HabitAssignVO.class);
                habitStatusService.deleteStatusByHabitAssign(habitAssignVO);
                habitStatisticService.deleteAllStatsByHabitAssign(habitAssignVO);
                habitAssignRepo.delete(habitAssign);
            });
    }
}