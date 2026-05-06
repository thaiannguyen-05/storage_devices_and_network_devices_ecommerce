/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package module.bussiness.cart;

import entity.OrderCartEntity;
import java.util.List;
import module.bussiness.cart.dto.CreateCartDto;
import module.bussiness.cart.dto.DeleteCartDto;
import module.bussiness.cart.dto.GetCartDto;
import module.bussiness.cart.dto.UpdateCartDto;
import module.bussiness.order.repository.interfaces.IOrderCartRepository;
import module.bussiness.order.repository.impl.OrderCartRepository;

public class CartService {

    private final IOrderCartRepository orderCartRepository;

    public CartService(IOrderCartRepository orderCartRepository) {
        this.orderCartRepository = orderCartRepository;
    }

    public CartService() {
        this(new OrderCartRepository());
    }

    public OrderCartEntity createCart(CreateCartDto dto) {
        validateCreateDto(dto);
        return this.orderCartRepository.registerCart(dto);
    }

    public OrderCartEntity registerCart(CreateCartDto dto) {
        return createCart(dto);
    }

    public List<OrderCartEntity> getAllCarts() {
        return this.orderCartRepository.findAll();
    }

    public OrderCartEntity getCartById(String id) {
        validateId(id);
        return this.orderCartRepository.findById(id.trim());
    }

    public OrderCartEntity getCartByUserId(String userId) {
        if (isBlank(userId)) {
            throw new IllegalArgumentException("User id is required");
        }
        return this.orderCartRepository.findByUserId(userId.trim());
    }

    public OrderCartEntity getCart(GetCartDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Cart id is required");
        }
        return getCartById(dto.getId());
    }

    public boolean updateCart(String id, UpdateCartDto dto) {
        validateId(id);
        validateUpdateDto(dto);
        return this.orderCartRepository.update(id.trim(), dto);
    }

    public boolean deleteCart(String id) {
        validateId(id);
        return this.orderCartRepository.delete(id.trim());
    }

    public boolean deleteCart(DeleteCartDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Cart id is required");
        }
        return deleteCart(dto.getId());
    }

    private void validateCreateDto(CreateCartDto dto) {
        if (dto == null || isBlank(dto.getUserId())) {
            throw new IllegalArgumentException("User id is required");
        }
    }

    private void validateUpdateDto(UpdateCartDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Cart data is required");
        }
        if (isBlank(dto.getUserId())) {
            throw new IllegalArgumentException("User id is required");
        }
    }

    private void validateId(String id) {
        if (isBlank(id)) {
            throw new IllegalArgumentException("Cart id is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
