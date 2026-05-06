package module.core.user;

import entity.UserEntity;
import java.util.List;
import module.bussiness.cart.CartService;
import module.bussiness.cart.dto.CreateCartDto;
import module.core.user.repository.interfaces.IUserRepository;
import module.core.user.repository.impl.UserRepository;
import module.core.user.dto.CreateUserDto;
import module.core.user.dto.UpdateUserDto;

public class UserService {

    private final IUserRepository userRepository;
    private final CartService cartService;

    public UserService(IUserRepository userRepository, CartService cartService) {
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    public UserService(IUserRepository userRepository) {
        this(userRepository, new CartService());
    }

    public UserService() {
        this(new UserRepository(), new CartService());
    }

    public UserEntity createUser(CreateUserDto dto) {
        UserEntity createdUser = this.userRepository.createUser(dto);

        try {
            CreateCartDto createCartDto = new CreateCartDto(createdUser.getId());
            this.cartService.createCart(createCartDto);
            return createdUser;
        } catch (Exception e) {
            try {
                this.userRepository.delete(createdUser.getId());
            } catch (Exception ignored) {
            }
            throw new RuntimeException("Failed to create cart for user", e);
        }
    }

    public List<UserEntity> getAllUsers() {
        return this.userRepository.findAll();
    }

    public List<UserEntity> getAllUsers(int page, int pageSize) {
        return this.userRepository.findAll(page, pageSize);
    }

    public UserEntity getUserById(String id) {
        return this.userRepository.findById(id);
    }

    public UserEntity getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public boolean updatePasswordById(String id, String hashPassword) {
        return this.userRepository.updatePasswordById(id, hashPassword);
    }

    public boolean activateUserById(String id) {
        return this.userRepository.activateById(id);
    }

    public boolean updateUser(String id, UpdateUserDto dto) {
        return this.userRepository.update(id, dto);
    }

    public boolean deleteUser(String id) {
        return this.userRepository.delete(id);
    }
}
