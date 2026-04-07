/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package module.bussiness.cart;

import entity.OrderCartEntity;
import module.bussiness.cart.dto.CreateCartDto;
import module.core.sql.interfaces.IOrderCartRepository;
import module.core.sql.repository.OrderCartRepository;

public class CartService {

    private final IOrderCartRepository orderCartRepository;

    public CartService(IOrderCartRepository orderCartRepository) {
        this.orderCartRepository = orderCartRepository;
    }

    public CartService() {
        this(new OrderCartRepository());
    }

    public OrderCartEntity registerCart(CreateCartDto dto) {
        return this.orderCartRepository.registerCart(dto);
    }
}
