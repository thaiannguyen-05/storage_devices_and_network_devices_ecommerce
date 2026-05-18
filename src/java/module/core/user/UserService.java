package module.core.user;

import entity.UserEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import module.core.auth.PasswordService;
import module.core.common.BaseResponse;
import module.core.config.AppConfig;
import module.core.outbox.OutBoxService;
import module.core.outbox.TypeEvent;
import module.core.user.dto.CreateUserDto;
import module.core.user.dto.UpdateUserDto;
import module.core.user.repository.impl.UserRepository;
import module.core.user.response_dto.CreateUserResponseDto;
import module.core.user.response_dto.DeleteUserResponseDto;
import module.core.user.response_dto.GetUserResponseDto;
import module.core.user.response_dto.ListUserResponseDto;
import module.core.user.response_dto.UpdateUserResponseDto;

public class UserService {
    private final UserRepository userRepository = new UserRepository();
    private final PasswordService passwordService = new PasswordService();
    private final OutBoxService outBoxService = new OutBoxService();

    public CreateUserResponseDto createUser(CreateUserDto dto) {
        CreateUserResponseDto response = new CreateUserResponseDto();
        if (isBlank(dto.getName()) || isBlank(dto.getEmail()) || isBlank(dto.getPassword())) {
            fail(response, "Name, email and password are required");
            return response;
        }
        if (userRepository.findByEmail(dto.getEmail()) != null) {
            fail(response, "Email already exists");
            return response;
        }
        String id = UUID.randomUUID().toString();
        UserEntity user = new UserEntity(id, dto.getName(), parseDate(dto.getDateOfBirth()),
                passwordService.hash(dto.getPassword()), defaultValue(dto.getStatus(), "ACTIVE"),
                defaultValue(dto.getRole(), "USER"), dto.getEmail(), LocalDateTime.now(), LocalDateTime.now());
        userRepository.insert(user);
        userRepository.createCartForUser(id);
        outBoxService.publishEvent("USER_CREATED", TypeEvent.USER_CREATED, id);
        response.setSuccess(true);
        response.setSuccessMessage("User created");
        response.setUserId(id);
        return response;
    }

    public GetUserResponseDto getUserById(String id) {
        GetUserResponseDto response = new GetUserResponseDto();
        response.setUser(userRepository.findById(id));
        response.setSuccess(response.getUser() != null);
        if (!response.isSuccess()) {
            response.setErrorMessage("User not found");
        }
        return response;
    }

    public UpdateUserResponseDto updateUser(UpdateUserDto dto) {
        UpdateUserResponseDto response = new UpdateUserResponseDto();
        UserEntity user = userRepository.findById(dto.getId());
        if (user == null) {
            fail(response, "User not found");
            return response;
        }
        user.setName(defaultValue(dto.getName(), user.getName()));
        user.setEmail(defaultValue(dto.getEmail(), user.getEmail()));
        user.setRole(defaultValue(dto.getRole(), user.getRole()));
        user.setStatus(defaultValue(dto.getStatus(), user.getStatus()));
        if (!isBlank(dto.getDateOfBirth())) {
            user.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        }
        userRepository.update(user);
        response.setSuccess(true);
        response.setSuccessMessage("User updated");
        return response;
    }

    public ListUserResponseDto listUsers(int page, int size) {
        int safeSize = size <= 0 ? AppConfig.PAGE_SIZE : size;
        int offset = Math.max(0, page - 1) * safeSize;
        ListUserResponseDto response = new ListUserResponseDto();
        response.setUsers(userRepository.findAll(offset, safeSize));
        response.setTotal(userRepository.countAll());
        response.setSuccess(true);
        return response;
    }

    public DeleteUserResponseDto deleteUser(String id) {
        DeleteUserResponseDto response = new DeleteUserResponseDto();
        userRepository.delete(id);
        response.setSuccess(true);
        response.setSuccessMessage("User deleted");
        return response;
    }

    public UpdateUserResponseDto changeStatus(String id, String status) {
        UpdateUserResponseDto response = new UpdateUserResponseDto();
        userRepository.updateStatus(id, status);
        response.setSuccess(true);
        response.setSuccessMessage("Status updated");
        return response;
    }

    private LocalDate parseDate(String value) {
        return isBlank(value) ? LocalDate.of(2000, 1, 1) : LocalDate.parse(value);
    }

    private String defaultValue(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void fail(BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }
}
